import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CcTokenProviderService {

  private _cctoken: string;

  constructor() {
    const savedCCToken = localStorage.getItem('cctoken');

    if (savedCCToken) {
      this.ccToken = savedCCToken;
    }
  }

  get ccToken() {
    return this._cctoken;
  }

  set ccToken(value: string) {
    localStorage.setItem('cctoken', value);
    this._cctoken = value;
  }
}
