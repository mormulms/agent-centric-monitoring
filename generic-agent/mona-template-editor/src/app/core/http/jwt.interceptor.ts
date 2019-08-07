import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';

import { AuthTokenProviderService } from '@mona/core/auth-token-provider.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authTokenProvider: AuthTokenProviderService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const requestedUrl = req.urlWithParams;
    if (requestedUrl.match(/^api\/.*/) && requestedUrl !== 'api/auth') {
      const clone = req.clone({ headers: req.headers.set('Authorization', `Bearer ${this.authTokenProvider.token}`)
      });

      return next.handle(clone);
    }

    return next.handle(req);
  }

}
