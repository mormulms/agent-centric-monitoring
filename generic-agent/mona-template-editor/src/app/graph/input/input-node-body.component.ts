import { Component, Input } from '@angular/core';

export interface InputNode extends Node {
  config: InputNodeConfig;
}

export interface InputNodeConfig {
  interval: {
    value: number;
    timeUnit: 's' | 'm' | 'h' | 'd';
  };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-input-node-body]',
  template: `
    <svg:tspan x="4" dy="1.2em">
      Interval {{inputNode.config.interval.value}} {{inputNode.config.interval.timeUnit}}
    </svg:tspan>
  `
})
export class InputNodeBodyComponent {
  @Input() inputNode: InputNode;
}
