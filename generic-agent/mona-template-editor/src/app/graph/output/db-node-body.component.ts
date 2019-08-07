import { Component, Input } from '@angular/core';

export interface DBNode {
  config: {
    database: string;
    urls: string[];
    username: string;
    password: string;
  };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-db-node-body]',
  template: `
    <ng-container *ngIf="dbNode && dbNode.config" xmlns:svg="http://www.w3.org/1999/html">
      <svg:tspan x="4" dy="1.2em">
        DB name: '{{dbNode.config.database}}'
      </svg:tspan>

      <ng-container *ngIf="dbNode.config.urls && dbNode.config.urls.length > 0; else noUrls">
        <svg:tspan x="4" dy="1.2em">
          DB URLs:
        </svg:tspan>
        <svg:tspan *ngFor="let url of dbNode.config.urls" x="8" dy="1.2em">
          - '{{url}}'
        </svg:tspan>
      </ng-container>
      <ng-template #noUrls>
        <svg:tspan x="4" dy="1.2em">No URLs</svg:tspan>
      </ng-template>

      <svg:tspan x="4" dy="1.2em">
        User: '{{dbNode.config.username}}'
      </svg:tspan>
      <svg:tspan x="4" dy="1.2em">
        Pw: '{{dbNode.config.password}}'
      </svg:tspan>
    </ng-container>
  `
})
export class DbNodeBodyComponent {
  @Input() dbNode: DBNode;
}
