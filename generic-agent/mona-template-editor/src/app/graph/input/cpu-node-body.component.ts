import { Component, Input } from '@angular/core';

import { InputNodeConfig } from '@mona/graph/input/input-node-body.component';

export interface CPUNode {
  config: CPUConfig;
}

export interface CPUConfig extends InputNodeConfig {
  percpu: boolean;
  totalcpu: boolean;
  collect_cpu_time: boolean;
  report_active: boolean;
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-cpu-node-body]',
  template: `
    <ng-container *ngIf="cpuNode && cpuNode.config">
      <svg:tspan x="4" dy="1.2em">
        Per CPU: {{cpuNode.config.percpu ? 1 : 0}}
      </svg:tspan>
      <svg:tspan x="4" dy="1.2em">
        Total system CPU stats: {{cpuNode.config.totalcpu ? 1 : 0}}
      </svg:tspan>
      <svg:tspan x="4" dy="1.2em">
        Raw CPU time: {{cpuNode.config.collect_cpu_time ? 1 : 0}}
      </svg:tspan>
      <svg:tspan x="4" dy="1.2em">
        Sum of non-idle states: {{cpuNode.config.report_active ? 1 : 0}}
      </svg:tspan>
    </ng-container>
  `
})
export class CpuNodeBodyComponent {
  @Input() cpuNode: CPUNode;
}
