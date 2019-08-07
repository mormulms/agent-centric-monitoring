import { Component, Input } from '@angular/core';

export interface ComparisonNode extends Node {
  type: string;
  config: ComparisonNodeConfig;
}

interface ComparisonNodeConfig {
  comparison: string;
  fields: string[];
  comparisonValue: {
    ILiteralOperand?: LiteralOperand;
    IMetricOperand?: MetricOperand;
  };
}

interface LiteralOperand {
  value: number;
}

interface MetricOperand {
  name: string;
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-comparison-node-body]',
  template: `
    <ng-container *ngIf="comparisonNode.config.fields && comparisonNode.config.fields.length > 0; else noFields"
                  xmlns:svg="http://www.w3.org/1999/html">
      <svg:tspan x="4" dy="1.2em">
        Check if fields:
      </svg:tspan>
      <svg:tspan *ngFor="let field of comparisonNode.config.fields" x="8" dy="1.2em">
        - "{{field}}"
      </svg:tspan>
    </ng-container>

    <ng-template #noFields xmlns:svg="http://www.w3.org/1999/html">
      <svg:tspan x="4" dy="1.2em">No fields</svg:tspan>
    </ng-template>

    <ng-container xmlns:svg="http://www.w3.org/1999/html">
      <svg:tspan x="4" dy="1.2em">are {{(comparisonNode.config.comparison === 'greaterThan') ? '&gt;' : '&lt;' }}</svg:tspan>
      <svg:tspan app-operand [operand]="comparisonNode.config.comparisonValue"/>
    </ng-container>
`
})
export class ComparisonNodeBodyComponent {
  @Input() comparisonNode: ComparisonNode;
}
