import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  if (request.url.startsWith('/api/')) {
    const headers = auth.csrfToken && !['GET', 'HEAD', 'OPTIONS'].includes(request.method)
      ? request.headers.set('X-CSRF-TOKEN', auth.csrfToken)
      : request.headers;
    request = request.clone({ headers, withCredentials: true });
  }
  return next(request);
};
