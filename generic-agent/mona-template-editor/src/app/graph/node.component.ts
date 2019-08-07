import { Component, Input } from '@angular/core';

import { Node } from '@mona/template/template.service';

@Component({
  /* tslint:disable-next-line:component-selector */
  selector: '[app-node]',
  templateUrl: './node.component.html'
})
export class NodeComponent {

  @Input() node: Node;

  public height = 40;

}
