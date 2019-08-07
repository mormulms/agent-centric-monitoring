import { Component, AfterViewInit } from '@angular/core';

import { faPlus, faTimes } from '@fortawesome/free-solid-svg-icons';
import { ArrayLayoutWidget } from 'ngx-schema-form';

@Component({
  selector: 'app-array-widget',
  template: `
<div class="widget form-group">
	<label [attr.for]="id" class="horizontal control-label">
		{{ schema.title }}
	</label>
	<span *ngIf="schema.description" class="formHelp">{{schema.description}}</span>
	<div *ngFor="let itemProperty of formProperty.properties; let i=index;" class="array-items">
		<sf-form-element [formProperty]="itemProperty"></sf-form-element>
    <button (click)="removeItem(i)" class="btn btn-danger btn-sm array-remove-button"
      [title]="'Remove this ' + parseItemName(schema.items.$ref) ">
			<fa-icon [icon]="faTimes"></fa-icon>
		</button>
	</div>
  <button (click)="addItem()" class="btn btn-success array-add-button">
    <fa-icon [icon]="faPlus"></fa-icon>
		<span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add {{parseItemName(schema.items.$ref)}}
	</button>
</div>`,
  styles: [`
      div.array-items {
        margin-bottom: 0.5rem;
      }
  `]
})
export class BsArrayWidgetComponent extends ArrayLayoutWidget implements AfterViewInit {

  faPlus = faPlus;
  faTimes = faTimes;

  ngAfterViewInit(): void {
    super.ngAfterViewInit();
  }

  addItem() {
    this.formProperty.addItem();
  }

  removeItem(index: number) {
    this.formProperty.removeItem(index);
  }

  parseItemName(refStr: string): string {
    const match = /^#\/definitions\/(.+)$/.exec(refStr);
    if (match && match[1]) {
      return match[1].toLowerCase();
    }
    return 'item';
  }
}
