import { Component, Input } from '@angular/core';

import { InputNodeConfig } from '@mona/graph/input/input-node-body.component';

interface ProcstatNode {
  config: ProcstatNodeConfig;
}

interface ProcstatNodeConfig extends InputNodeConfig {
  systemd_unit: string;
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-procstat-node-body]',
  template: `
    <ng-container *ngIf="procstatNode && procstatNode.config" xmlns:svg="http://www.w3.org/1999/html">
      <svg:tspan x="4" dy="1.2em">
        Systemd Unit:
      </svg:tspan>
      <svg:tspan x="4" dy="1.2em">
        '{{procstatNode.config.systemd_unit}}'
      </svg:tspan>
    </ng-container>
  `
})
export class ProcstatNodeBodyComponent {
  @Input() procstatNode: ProcstatNode;
}
