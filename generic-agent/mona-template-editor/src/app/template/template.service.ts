import { Injectable } from '@angular/core';

import { ReplaySubject, Subject } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

import { TemplateHttpService } from '@mona/core/http/template-http.service';
import { AuthHttpService } from '@mona/core/http/auth-http.service';
import { AuthTokenProviderService } from '@mona/core/auth-token-provider.service';
import { CcTokenProviderService } from '@mona/core/cc-token-provider.service';

export interface AgentTemplate {
  $schema?: string;
  id: string;
  name: string;
  inputs: Input[];
  processors: Processor[];
  aggregators: Aggregator[];
  outputs: Output[];
}

export interface TemplateNode {
  _type: string;
  [key: string]: string | Node;
}

/* tslint:disable:no-empty-interface */
export interface Input extends TemplateNode {}
export interface Processor extends TemplateNode {}
export interface Aggregator extends TemplateNode {}
export interface Processor extends TemplateNode {}
export interface Output extends TemplateNode {}

export interface Node {
  id: string;
  name: string;
  nodeType: string;
  type: string;
  label: string;
  next: NextWrapper[];
  selected?: boolean;
  width?: number;
  height?: number;
  options?: NodeOptions;
  config?: any;
}

export interface NodeOptions {
  color: string;
  transform: string;
}

export interface NextWrapper {
  _type: string;
  [key: string]: string | NextRef;
}

export interface NextRef {
  $type: 'ref';
  value: {
    type: string;
    id: string;
  };
}

@Injectable({ providedIn: 'root' })
export class TemplateService {

  private template: Subject<AgentTemplate> = new ReplaySubject();
  public template$ = this.template.asObservable();

  constructor(private authHttpService: AuthHttpService,
              private ccTokenProvider: CcTokenProviderService,
              private authTokenProvider: AuthTokenProviderService,
              private templateHttp: TemplateHttpService) {
    const savedTemplate = localStorage.getItem('template');
    if (savedTemplate && savedTemplate.length > 0) {
      this.template.next(JSON.parse(savedTemplate));
    } else {
      this.downloadTemplate();
    }
  }

  private downloadTemplate() {
    this.authHttpService.login(this.ccTokenProvider.ccToken).pipe(
      tap((jwt: string) => this.authTokenProvider.token = jwt),
      switchMap(() => this.templateHttp.getTemplate()),
      tap((retrievedTemplate) => {
        console.log({retrievedTemplate});
        if (!retrievedTemplate) {
          return;
        }
        this.template.next(retrievedTemplate);
      }),
      switchMap(() => this.authHttpService.logout())
    ).subscribe(
      () => {},
      (err) => console.error(err)
    );
  }

  public saveTemplate(templateToSave: AgentTemplate) {
    console.log({templateToSave});
    localStorage.setItem('template', JSON.stringify(templateToSave));
    this.template.next(templateToSave);
  }
}
