import { Component, Input } from '@angular/core';

export interface LogicNode {
  config: LogicNodeConfig;
}

interface LogicNodeConfig {
  operands: any[];
  logicOperator: string;
  negateResult: boolean;
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-logic-node-body]',
  template: `
    <svg:tspan x="4" dy="1.2em">
      Operands: [
    </svg:tspan>
    <svg:tspan *ngFor="let operand of logicNode.config.operands" x="8" dy="1.2em">
      {{(operand.ILiteralOperand) ? operand.ILiteralOperand.value : '"' + operand.IMetricOperand.name + '"'}}
    </svg:tspan>
    <svg:tspan x="4" dy="1.2em">]</svg:tspan>
    <svg:tspan x="4" dy="1.2em">
      Combination: {{logicNode.config.logicOperator}}
    </svg:tspan>
    <svg:tspan x="4" dy="1.2em">
      Negate: {{(logicNode.config.negateResult) ? 'yes' : 'no'}}
    </svg:tspan>
  `
})
export class LogicNodeBodyComponent {
  @Input() logicNode: LogicNode;
}
