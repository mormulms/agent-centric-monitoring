import { Component, Input } from '@angular/core';

export class RangeNode extends Node {
  config: RangeNodeConfig;
}

interface RangeNodeConfig {
  fields: string[];
  outside: boolean;
  lowerInclusive: boolean;
  lowerBound: any;
  upperInclusive: boolean;
  upperBound: any;
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-range-node-body]',
  template: `
    <ng-container *ngIf="rangeNode.config" xmlns:svg="http://www.w3.org/1999/html">
      <ng-container *ngIf="rangeNode.config.fields && rangeNode.config.fields.length > 0; else noFields">
        <svg:tspan x="4" dy="1.2em">
          Check if fields:
        </svg:tspan>
        <svg:tspan *ngFor="let field of rangeNode.config.fields" x="8" dy="1.2em">
          - "{{ field }}"
        </svg:tspan>
      </ng-container>

      <ng-template #noFields>
        <svg:tspan x="4" dy="1.2em">
          No fields
        </svg:tspan>
      </ng-template>

      <svg:tspan x="4" dy="1.2em">are {{ (rangeNode.config.outside) ? 'outside of ': 'within ' }}</svg:tspan>
      <svg:tspan>{{ (rangeNode.config.lowerInclusive) ? '[' : '(' }}</svg:tspan>
      <svg:tspan app-operand [operand]="rangeNode.config.lowerBound"/>
      ,
      <svg:tspan app-operand [operand]="rangeNode.config.upperBound"/>
      <svg:tspan>{{ (rangeNode.config.upperInclusive) ? ']' : ')' }}</svg:tspan>
    </ng-container>
  `
})
export class RangeNodeBodyComponent {
  @Input() rangeNode: RangeNode;
}
