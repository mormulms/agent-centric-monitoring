import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { CoreModule } from '@mona/core/core.module';

export interface SchemaDef {
  title?: string;
  type: string;
  properties: SchemaProps;
  widget?: string;
}

export interface AgentTemplateSchema extends SchemaDef {
  $schema?: string;
  $id: string;
  definitions: {
    [key: string]: SchemaDef
  };
}

export interface SchemaProps {
  [key: string]: any;
}

@Injectable({
  providedIn: CoreModule
})
export class SchemaHttpService {

  private _basePath = 'assets';

  constructor(private http: HttpClient) { }

  getSchema(): Observable<AgentTemplateSchema> {
    return this.http.get<AgentTemplateSchema>(`${this._basePath}/schema.json`)
    .pipe(
      map((s) => {
        // delete some fields
        const { title: _delete, ...schema} = s;
        return schema;
      }),
      map((schema) => {
        schema.definitions = Object.entries(schema.definitions)
        .reduce((definitions, [key, definition]) => {
          if (!definition) {
            return definitions;
          }

          let properties = {...definition.properties};
          if (properties.name) {
            const name = {...properties.name, title: 'Name'};
            properties = {...properties, name};
          }

          if (properties.type) {
            const type = {...properties.type, title: 'Type'};
            properties = {...properties, type};
          }

          if (properties.config) {
            const config = {...properties.config, title: 'Configuration'};
            properties = {...properties, config};
          }

          return {...definitions, [key]: {...definition, properties}};
        }, {});

        // add some titles
        schema.definitions.TemplateConfig.widget = 'hidden';
        schema.definitions.Input.properties._type.title = 'Input type';
        schema.definitions.GenericInput.properties.name.title = 'Name';
        schema.definitions.Processor.properties._type.title = 'Processor type';
        schema.definitions.GenericProcessor.properties.name.title = 'Name';

        schema.definitions.ComparisonProcessor.properties.name.title = 'Name';
        schema.definitions.ComparisonProcessor.properties.type.title = 'Type';
        schema.definitions.ComparisonProcessor.properties.config.title = 'Configuration';

        schema.definitions.CalculationProcessor.properties.name.title = 'Name';
        schema.definitions.GenericAggregator.properties.name.title = 'Name';
        schema.definitions.StatsAggregator.properties.name.title = 'Name';

        schema.properties.$schema.widget = 'hidden';
        schema.properties.name.title = 'Name';
        schema.properties.inputs.title = 'Inputs';
        schema.properties.processors.title = 'Processors';
        schema.properties.aggregators.title = 'Aggregators';
        schema.properties.outputs.title = 'Outputs';

        const formSchema = {...schema};

        console.log({formSchema});

        return formSchema;
      })
    );
  }
}
