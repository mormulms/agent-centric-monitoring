import { DefaultWidgetRegistry } from 'ngx-schema-form';

import { BsButtonWidgetComponent } from '@mona/widgets/bs-button-widget.component';
import { BsArrayWidgetComponent } from '@mona/widgets/bs-array-widget.component';
import { BsCheckboxWidgetComponent } from '@mona/widgets/bs-checkbox-widget.component';
import { BsFloatWidgetComponent } from '@mona/widgets/bs-float-widget.component';
import { BsStringWidgetComponent } from '@mona/widgets/bs-string-widget.component';

export class CustomWidgetRegistry extends DefaultWidgetRegistry {

  constructor() {
    super();

    this.register('array', BsArrayWidgetComponent);
    this.register('button', BsButtonWidgetComponent);
    this.register('checkbox', BsCheckboxWidgetComponent);
    this.register('float', BsFloatWidgetComponent);
    this.register('string', BsStringWidgetComponent);
  }

}
