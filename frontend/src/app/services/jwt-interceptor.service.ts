import { HttpInterceptorFn } from '@angular/common/http';
import {environment} from '../../environments/environment';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('JWT_Token');
  if (token) {
    const authReq = req.clone({
      url: `${fetch(environment.protocol)}://${fetch(environment.apiUrl)}/${req.url}`,
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(authReq);
  }
  return next(req);
};
