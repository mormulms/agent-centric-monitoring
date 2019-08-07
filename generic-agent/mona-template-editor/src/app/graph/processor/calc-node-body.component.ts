import { Component, Input } from '@angular/core';

export interface CalcNode {
  config: CalcNodeConfig;
}

interface CalcNodeConfig {
  result: {
    name: string;
    dropOriginal: boolean;
    lhs: any;
    rhs: any;
    operation: string;
  };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-calc-node-body]',
  template: `
  <ng-container *ngIf="calcNode.config.result" xmlns:svg="http://www.w3.org/1999/html">
    <svg:tspan x="4" dy="1.2em">Name: "{{calcNode.config.result.name}}"</svg:tspan>
    <svg:tspan x="4" dy="1.2em">Drop original: {{(calcNode.config.result.dropOriginal) ? "true" : "false"}}</svg:tspan>
    <svg:tspan x="4" dy="1.2em">Result: </svg:tspan>
    <svg:tspan x="4" dy="1.2em" app-operand [operand]="calcNode.config.result.lhs"/>
    <svg:tspan>{{ calcNode.config.result.operation }}</svg:tspan>
    <svg:tspan app-operand [operand]="calcNode.config.result.rhs"/>
  </ng-container>
  `
})
export class CalcNodeBodyComponent {
  @Input() calcNode: CalcNode;
}
