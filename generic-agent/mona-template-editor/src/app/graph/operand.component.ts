import { Component, Input } from '@angular/core';

export interface Operand {
  _type: string;
  ILiteralOperand: { value: number };
  IMetricOperand: { name: string };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-operand]',
  template: `
    <ng-container *ngIf="operand.ILiteralOperand">
      {{ operand.ILiteralOperand.value }}
    </ng-container>
    <ng-container *ngIf="operand.IMetricOperand">
      {{ '"' + operand.IMetricOperand.name + '"' }}
    </ng-container>
  `
})
export class OperandComponent {
  @Input() operand: Operand;
}
