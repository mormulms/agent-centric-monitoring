import { Component, Input } from '@angular/core';

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-node-body]',
  templateUrl: './node-body.component.html',
  styleUrls: ['./node-body.component.scss']
})
export class NodeBodyComponent {

  @Input() node: any;

}
