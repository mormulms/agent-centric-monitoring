import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthTokenProviderService {

  private _token: string;

  constructor() {
    const savedToken = localStorage.getItem('token');

    if (savedToken) {
      this._token = savedToken;
    }
  }

  get token() {
    return this._token;
  }

  set token(value: string) {
    localStorage.setItem('token', value);
    this._token = value;
  }
}
