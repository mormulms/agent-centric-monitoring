import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { CoreModule } from '@mona/core/core.module';
import { FileHttpService } from '@mona/core/http/file-http.service';

@Injectable({ providedIn: CoreModule })
export class ConfigFileHttpService {

  constructor(private fileHttp: FileHttpService) {}

  public uploadConfig(configFileName: string, data: string): Observable<boolean> {
    const configFile = {
      name: configFileName,
      targetPath: `/etc/telegraf/${configFileName}`,
      owner: 'telegraf',
      group: 'telegraf',
      fileMode: '644',
      checksum: '',
      dependentServices: ['telegraf'],
      templates: ['telegraf-host'],
      isReloadable: true,
      isTemplate: false,
      isDirectory: false
    };

    return this.fileHttp.uploadConfigFile(configFile, data);
  }

}
