import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { ConfigFile, FileHttpService } from '@mona/core/http/file-http.service';
import { AgentTemplate } from '@mona/template/template.service';
import { map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class TemplateHttpService {

  private readonly _templateFileName = 'template.json';

  constructor(private fileHttp: FileHttpService) {}

  public getTemplate(): Observable<AgentTemplate> {
    return this.fileHttp.getFileData(this._templateFileName).pipe(
      map(str => <AgentTemplate>JSON.parse(str))
    );
  }

  public uploadTemplate(newTemplate: AgentTemplate): Observable<boolean> {
    const templateFile: ConfigFile = {
      name: this._templateFileName,
      targetPath: `/var/telegraf/${this._templateFileName}`,
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

    return this.fileHttp.uploadConfigFile(templateFile, JSON.stringify(newTemplate));
  }

}
