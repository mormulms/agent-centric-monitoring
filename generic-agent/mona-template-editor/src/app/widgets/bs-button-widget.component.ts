import { Component } from '@angular/core';

@Component({
  selector: 'app-button-widget',
  template: `
    <button class="btn btn-primary" (click)="button.action($event)">
      {{button.label}}
    </button>
  `
})
export class BsButtonWidgetComponent {
  public button;
  public formProperty;
}
