import { Component } from '@angular/core';

import { ControlWidget } from 'ngx-schema-form';

@Component({
  selector: 'app-float-widget',
  template: `<div class="widget form-group">
	<label [attr.for]="id" class="horizontal control-label">{{ schema.title }}</label>
  <span *ngIf="schema.description" class="formHelp">{{schema.description}}</span>
	<input [attr.readonly]="schema.readOnly?true:null" [name]="name" class="text-widget float-widget form-control" [formControl]="control"
	type="number" step="any" [attr.min]="schema.minimum" [attr.max]="schema.maximum" [attr.placeholder]="schema.placeholder"
	[attr.maxLength]="schema.maxLength || null" [attr.minLength]="schema.minLength || null">
</div>`
})
export class BsFloatWidgetComponent extends ControlWidget {}
