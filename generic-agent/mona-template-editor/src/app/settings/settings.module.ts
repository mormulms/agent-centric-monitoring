import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { SettingsComponent } from '@mona/settings/settings.component';
import { SettingsRoutingModule } from '@mona/settings/settings-routing.module';

@NgModule({
  imports: [
    CommonModule,
    SettingsRoutingModule,
    ReactiveFormsModule,
  ],
  declarations: [SettingsComponent]
})
export class SettingsModule { }
