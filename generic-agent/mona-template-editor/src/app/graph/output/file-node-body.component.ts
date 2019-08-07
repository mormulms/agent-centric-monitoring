import { Component, Input } from '@angular/core';

export interface FileNode {
  config: {
    file: string[],
    data_format: string
  };
}

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-file-node-body]',
  template: `
  <ng-container *ngIf="fileNode && fileNode.config" xmlns:svg="http://www.w3.org/1999/html">
    <ng-container *ngIf="fileNode.config.file; else noFiles">
      <svg:tspan x="4" dy="1.2em">
        Files to write to:
      </svg:tspan>
      <svg:tspan *ngFor="let file of fileNode.config.file" x="8" dy="1.2em">
        - '{{file}}'
      </svg:tspan>
    </ng-container>
    <ng-template #noFiles>
      <svg:tspan x="4" dy="1.2em">No files</svg:tspan>
    </ng-template>

    <svg:tspan x="4" dy="1.2em">
      using {{fileNode.config.data_format}} format
    </svg:tspan>
  </ng-container>
  `
})
export class FileNodeBodyComponent {
  @Input() fileNode: FileNode;
}
