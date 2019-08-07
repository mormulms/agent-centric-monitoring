import { Component, Input } from '@angular/core';

export interface AggregatorNode {
  config: AggregatorNodeConfig;
}

interface AggregatorNodeConfig {
  period: {
    value: number;
    timeUnit: 's' | 'm' | 'h' | 'd';
  };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-aggregator-node-body]',
  template: `
    <svg:tspan x="4" dy="1.2em">
      Period {{aggregatorNode.config.period.value}} {{aggregatorNode.config.period.timeUnit}}
    </svg:tspan>
  `
})
export class AggregatorNodeBodyComponent {
  @Input() aggregatorNode: AggregatorNode;
}
