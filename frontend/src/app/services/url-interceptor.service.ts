import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

export const urlInterceptor: HttpInterceptorFn = (req, next) => {
  const absoluteUrlReq = req.clone({
    url: `${environment.protocol}://${environment.apiUrl}/${req.url}`,
  });
  return next(absoluteUrlReq);
};
