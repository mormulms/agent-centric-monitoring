import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

export interface ConfigFile {
  '@class'?: string;
  name: string;
  targetPath: string;
  owner: string;
  group: string;
  fileMode: string;
  isTemplate: boolean;
  isReloadable: boolean;
  isDirectory: boolean;
  checksum: string;
  dependentServices: string[];
  templates: string[];
}

@Injectable({ providedIn: 'root' })
export class FileHttpService {

  private readonly _basePath = 'api';

  constructor(private http: HttpClient) {}

  public getFileData(fileName: string): Observable<string> {
    return this.http.get<string>(`${this._basePath}/file/${fileName}/data`);
  }

  public uploadConfigFile(file: ConfigFile, data: string): Observable<boolean> {
    if (!file || !file.name) {
      throw new Error('Unable to upload invalid file!');
    }

    return this.saveFile(file).pipe(
      tap(() => console.log(`Updated entry for file '${file.name}'`)),
      switchMap(() => this.saveFileData(file.name, data)),
      tap(() => console.log(`Updated data for file '${file.name}'`))
    );
  }

  private saveFile(file: ConfigFile): Observable<boolean> {
    return this.http.put<boolean>(`${this._basePath}/file`, {...file, '@class': 'de.cinovo.cloudconductor.api.model.ConfigFile'});
  }

  private saveFileData(fileName: string, data: string): Observable<boolean> {
    return this.http.put<boolean>(`${this._basePath}/file/${fileName}/data`, data);
  }
}
