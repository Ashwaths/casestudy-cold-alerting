import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';

interface AuthSession {
  username: string;
}

interface CsrfTokenResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  currentUser: string | null = null;
  csrfToken: string | null = null;

  get isAuthenticated(): boolean {
    return this.currentUser !== null;
  }

  restoreSession(): Observable<string | null> {
    return this.http.get<AuthSession>('/api/v1/auth/session').pipe(
      tap(session => this.currentUser = session.username),
      switchMap(session => this.http.get<CsrfTokenResponse>('/api/v1/auth/csrf').pipe(
        tap(token => this.csrfToken = token.token),
        map(() => session.username)
      )),
      catchError(error => {
        this.currentUser = null;
        this.csrfToken = null;
        return error.status === 401 ? of(null) : throwError(() => error);
      })
    );
  }

  login(): void {
    window.location.assign('/oauth2/authorization/azure');
  }

  logout(): Observable<void> {
    return this.http.post<void>('/api/v1/auth/logout', null).pipe(
      tap(() => {
        this.currentUser = null;
        this.csrfToken = null;
      })
    );
  }
}
