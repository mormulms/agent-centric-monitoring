import { Injectable } from '@angular/core';

import * as stringify from '@iarna/toml/stringify';

import { AgentTemplate, Aggregator, Input, NextRef, Node, Output, Processor, TemplateNode } from '@mona/template/template.service';

interface TagMultiMap {
  [id: string]: TagList;
}
type TagList = string[];

interface Config {
  tags: { id: string}[];
  tagpass: { id: string[] }[];
}

interface Operands {
  LHSLiteral?: LiteralOperand;
  LHSMetric?: MetricOperand;
  RHSLiteral?: LiteralOperand;
  RHSMetric?: MetricOperand;
}

type Operand = LiteralWrapper | MetricWrapper;

interface LiteralWrapper {
  LiteralOperand: LiteralOperand;
}

interface MetricWrapper {
  MetricOperand: MetricOperand;
}

interface LiteralOperand {
  value: number;
}

interface MetricOperand {
  name: string;
}

interface ComparisonConfig extends Config {
  comparisonType: string;
  fields: string[];
  LiteralOperand?: LiteralOperand;
  MetricOperand?: MetricOperand;
}

interface CalculationConfig extends Config {
  result: {
    name: string;
    drop_original: boolean;
    operator: string;
    LHSLiteral?: LiteralOperand;
    LHSMetric?: MetricOperand;
    RHSLiteral?: LiteralOperand;
    RHSMetric?: MetricOperand;
  };
}

interface RangeConfig extends Config {
  fields: string[];
  lowerInclusive: boolean;
  upperInclusive: boolean;
  outside: boolean;
  upperBound: any;
  lowerBound: any;
}

@Injectable({ providedIn: 'root' })
export class ConfigProviderService {

  private static getInfluxDBConfig({ database = 'telegraf', urls = ['http://127.0.0.1:8089'], username = '$INFLUX_USER',
                                     password = '$INFLUX_PW', ...otherConfig} = {}): any {
    return {database, urls, username, password, ...otherConfig};
  }

  private static getFileConfig({ files = ['stdout'], data_format = 'influx', ...otherConfig } = {}): any {
    return {files, data_format, ...otherConfig};
  }

  private static getOperand(oWrapper): Operand {
    const operand = oWrapper[oWrapper._type];
    if (oWrapper._type === 'ILiteralOperand') {
      const value = (typeof operand.value === 'number') ? operand.value.toFixed(1) : '0.0';
      return { LiteralOperand: {value} };
    } else {
      return { MetricOperand: operand };
    }
  }

  private static getComparisonConfig({ comparisonType = '>', fields = [], comparisonValue = {},
                                       ...furtherConfig} = {}): Partial<ComparisonConfig> {
    const comparison = ConfigProviderService.getOperand(comparisonValue);
    return {comparisonType, fields, ...comparison};
  }

  private static getRangeConfig({ fields = [], lowerBound = {}, lowerInclusive = true, upperBound = {}, upperInclusive = true,
                                  outside = false, ...furtherConfigs } = {}): Partial<RangeConfig> {
    const lb = ConfigProviderService.getOperand(lowerBound);
    const ub = ConfigProviderService.getOperand(upperBound);

    return {fields, lowerBound: lb, lowerInclusive, upperBound: ub, upperInclusive, outside, ...furtherConfigs};
  }

  private static getCalcConfig({name = 'result', drop_original = false, operator = 'MULTIPLY', result = { lhs: {}, rhs: {}},
                                 ...furtherConfig} = {}): Partial<CalculationConfig> {

    let operands: Operands = {};
    if (result && result.lhs && result.rhs) {
      const lhs = ConfigProviderService.getOperand(result.lhs);
      const rhs = ConfigProviderService.getOperand(result.rhs);

      if (lhs && lhs['LiteralOperand']) {
        operands = {...operands, LHSLiteral: lhs['LiteralOperand']};
      } else if (lhs['MetricOperand']) {
        operands = {...operands, LHSMetric: lhs['MetricOperand']};
      }

      if (rhs && rhs['LiteralOperand']) {
        operands = {...operands, RHSLiteral: rhs['LiteralOperand']};
      } else if (rhs['MetricOperand']) {
        operands = {...operands, RHSMetric: rhs['MetricOperand']};
      }
    }
    return { result: {name, drop_original, operator, ...operands, ...furtherConfig}};
  }

  private static getBasicStatsConfig({ period = '30s',
                                       dropOriginal = true,
                                       stats = [ 'mean' ],
                                       ...otherConfig } = {}): any {
    // const basicStatsConfig = (otherConfig) ? otherConfig : {};
    return {period, drop_original: dropOriginal, stats};
  }

  private static mergeDictionaries(pluginDict, [pluginType, pluginConfigArray]) {
    // merge dictionaries
    if (pluginDict[pluginType]) {
      return {...pluginDict, [pluginType]: [...pluginDict[pluginType], ...pluginConfigArray]};
    }

    // add new plugin type to dictionary
    return {...pluginDict, [pluginType]: pluginConfigArray};
  }

  private getProcessors(template: AgentTemplate, tagMultiMap: TagMultiMap): any {
    return template.processors.map((p: Processor) => <Node>p[p._type])
      .filter(n => n.type && n.type.length > 0 && n.config)
      .map((n: Node) => {
        let processorConf = {};
        switch (n.type) {
          case 'range':
            processorConf = ConfigProviderService.getRangeConfig(n.config);
            break;

          case 'calculation':
            processorConf = ConfigProviderService.getCalcConfig(n.config);
            break;

          case 'comparison':
            processorConf = ConfigProviderService.getComparisonConfig(n.config);
            break;

          default:
            throw new Error(`Unknown processor type '${n.type}'`);
        }

        const tagpass = tagMultiMap[n.id].reduce((acc: any, tag: string) => ({...acc, [tag]: ['*']}), {});
        const tags = { [n.id]: '1' };

        processorConf = [{...processorConf, tagpass, tags}];

        return { [n.type]: processorConf };
      })
    .reduce((acc, current) => ({...acc, ...current}), {});
  }

  constructor() { }

  public generate(template: AgentTemplate): string {
    const tagMultiMap = this.walkPipelines(template);

    let tomlConfig = {};

    // TODO get this from template config
    const agent = {
      interval: '10s',
      round_interval: true,
    };
    tomlConfig = {...tomlConfig, agent};

    const inputs = this.getInputs(template);
    if (Object.keys(inputs).length > 0) {
      tomlConfig = {...tomlConfig, inputs};
    }

    const aggregators = this.getAggregators(template, tagMultiMap);
    if (Object.keys(aggregators).length > 0) {
      tomlConfig = {...tomlConfig, aggregators};
    }
    const processors = this.getProcessors(template, tagMultiMap);
    if (Object.keys(processors).length > 0) {
      tomlConfig = {...tomlConfig, processors};
    }

    const outputs = this.getOutputs(template, tagMultiMap);
    if (Object.keys(outputs).length > 0) {
      tomlConfig = {...tomlConfig, outputs};
    }

    console.log({tomlConfig});
    return stringify(tomlConfig);
  }

  private walkPipelines(template: AgentTemplate): TagMultiMap {
    return template.inputs.map(i => <Node>i[i._type])
      .map(inputNode => this.walkPipeline(inputNode, [], template))
      .reduce((map, current) => {
        Object.entries(current).forEach(([id, tags]) => {
          if (map[id]) {
            // merge tag lists, avoid duplicates
            map[id] = [...map[id], ...tags.filter(tag => !map[id].includes(tag))];
          } else {
            map[id] = [...tags];
          }
        });

        return map;
      }, {});
  }

  private walkPipeline(currentNode: Node, tags: string[], template): TagMultiMap {
    if (!currentNode.next) {
      return {[currentNode.id]: tags};
    }

    return currentNode.next.map(n => <NextRef>n[n._type])
    .reduce((pipelines, currentRef: NextRef) => {
      const nextNode = this.findNode(currentRef, template);
      const newTags = [currentNode.id];
      const childs = (nextNode) ? this.walkPipeline(nextNode, newTags, template) : {};
      const pipelineMap = {[currentNode.id]: tags, ...childs};
      return {...pipelines, ...pipelineMap};
    }, {});
  }

  private findNode(ref: NextRef, template: AgentTemplate): Node {
    if (!ref || !ref.value || !ref.value.type || !ref.value.id) {
      throw new Error('Invalid node ref!');
    }

    let templateNodes: TemplateNode[];
    switch (ref.value.type) {
      case 'aggregator':
        templateNodes = template.aggregators;
        break;

      case 'processor':
        templateNodes = template.processors;
        break;

      case 'output':
        templateNodes = template.outputs;
        break;
    }

    return templateNodes
      .map((tn: TemplateNode) => <Node>tn[tn._type])
      .find((n: Node) => n.id === ref.value.id);
  }

  private getInputs(template: AgentTemplate): any {
    return template.inputs.map((i: Input) => <Node>i[i._type])
      .filter((n: Node) => n.config && n.config.interval && n.config.interval.value && n.config.interval.timeUnit)
      .map((n: Node) => {
        const {interval, ...config} = n.config;
        const intervalConfig = `${interval.value}${interval.timeUnit}`;
        const tags =  { [n.id]: '1' };
        return { [n.type]: [{ interval: intervalConfig, ...config, tags}] };
      })
      .reduce((acc, current) => {
        return {...acc, ...current};
      }, {});
  }

  private getOutputs(template: AgentTemplate, tagMultiMap: TagMultiMap): any {
    return template.outputs.map((o: Output) => <Node>o[o._type])
      .filter(n => n.type)
      .map((n: Node) => {
        let outputConfig = {};

        switch (n.type) {
          case 'file':
            outputConfig = ConfigProviderService.getFileConfig();
            break;

          case 'influxdb':
            outputConfig = ConfigProviderService.getInfluxDBConfig(n.config);
            break;

          // TODO implement more types of output nodes

          default:
            throw new Error(`Unknown output node type '${n.type}'!`);
        }

        const tagpass = tagMultiMap[n.id].reduce((acc: any, tag: string) => ({...acc, [tag]: ['*']}), {});

        return { [n.type]: [{...outputConfig, tagpass}] };
      }).reduce((acc, current) => Object.entries(current).reduce(ConfigProviderService.mergeDictionaries, acc), {});
  }

  private getAggregators(template: AgentTemplate, tagMultiMap: TagMultiMap): any {
    return template.aggregators.map((a: Aggregator) => <Node>a[a._type])
    .filter(n => n.type && n.config && n.id)
    .map((n: Node) => {
      let config  = n.config;

      let period = '30s';
      if (config.period && config.period.value && config.period.timeUnit) {
        period = `${config.period.value}${config.period.timeUnit}`;
      }

      config = {...n.config, period};

      let aggregatorConfig = {};
      switch (n.type) {
        case 'basicstats':
          aggregatorConfig = ConfigProviderService.getBasicStatsConfig(config);
          break;

        default:
          throw new Error(`Unknown aggregator type '${n.type}'!`);
      }

      const tagpass = tagMultiMap[n.id].reduce((acc: any, tag: string) => ({...acc, [tag]: ['*']}), {});
      const tags = { [n.id]: '1' };

      aggregatorConfig = [{...aggregatorConfig, tagpass, tags}];

      return { [n.type]: aggregatorConfig };
    })
    .reduce((acc, current) => {
      return {...acc, ...current};
    }, {});
  }


}
