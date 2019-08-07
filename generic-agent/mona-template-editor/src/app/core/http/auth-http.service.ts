import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthHttpService {

  private _basePathURL = 'api/auth';

  constructor(private http: HttpClient) { }

  public login(token): Observable<string> {
    return this.http.put<string>(this._basePathURL, {token});
  }

  public refresh(currentJWT: string): Observable<string> {
    return this.http.put<string>(`${this._basePathURL}/refresh`, currentJWT);
  }

  public logout(): Observable<boolean> {
    return this.http.put<boolean>(`${this._basePathURL}/logout`, {});
  }

}
