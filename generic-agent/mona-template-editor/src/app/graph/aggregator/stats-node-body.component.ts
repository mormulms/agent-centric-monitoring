import { Component, Input } from '@angular/core';

export interface StatsNode {
  config: {
    stats: string[];
    dropOriginal: boolean
  };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-stats-node-body]',
  template: `
    <ng-container *ngIf="statsNode && statsNode.config" xmlns:svg="http://www.w3.org/1999/html">
      <svg:tspan x="4" dy="1.2em">
        Drop original: {{(statsNode.config.dropOriginal) ? "true" : "false"}}
      </svg:tspan>

      <ng-container *ngIf="statsNode.config.stats && statsNode.config.stats.length > 0; else noStats">
        <svg:tspan x="4" dy="1.2em">Statistics:</svg:tspan>
        <svg:tspan *ngFor="let stat of statsNode.config.stats" x="8" dy="1.2em">
          - '{{stat}}'
        </svg:tspan>
      </ng-container>
      <ng-template #noStats>
        <svg:tspan x="4" dy="1.2em">No statistics</svg:tspan>
      </ng-template>
    </ng-container>
  `
})
export class StatsNodeBodyComponent {
  @Input() statsNode: StatsNode;
}
