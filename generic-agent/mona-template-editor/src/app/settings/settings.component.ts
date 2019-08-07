import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { CcTokenProviderService } from '@mona/core/cc-token-provider.service';

interface SettingsFormValue {
  ccToken: string;
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent {

  public settingsForm;

  constructor(private fb: FormBuilder,
              private ccTokenProvider: CcTokenProviderService) {
    const currentToken = this.ccTokenProvider.ccToken || '';

    this.settingsForm = this.fb.group({
      ccToken: [currentToken, Validators.required]
    });
  }

  public saveSettings(formValue: SettingsFormValue) {
    if (!formValue || !formValue.ccToken || formValue.ccToken.length < 1) {
      return;
    }

    this.ccTokenProvider.ccToken = formValue.ccToken;
  }

}
