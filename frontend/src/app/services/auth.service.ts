import { HttpClient } from '@angular/common/http';
import {catchError, map, Observable, of} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) {}

  login(userDetails: { username: string; password: string }): Observable<boolean> {
    return this.http.post<{jwt: string, expiration: string}>('http://localhost:8080/api/auth/signin', userDetails, { responseType: 'json' })
      .pipe(
        map(response => {
          localStorage.setItem('JWT_Token', response.jwt);
          localStorage.setItem('JWT_Expiration', response.expiration);
          return true;
        }),
        catchError(() => {
          return of(false);
        })
      );
  }

  signup(userDetails: { password: string }): Observable<boolean> {
    return this.http.post('http://localhost:8080/api/auth/signup', userDetails, { responseType: 'text' })
      .pipe(
        map(response => {
          localStorage.setItem('ASSIGNED_USERNAME', response);
          return true;
        }),
        catchError(error => {
          return of(error);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('JWT_Token');
    localStorage.removeItem('JWT_Expiration');
  }

  isAuthenticated(): boolean {
    const jwt_token = localStorage.getItem('JWT_Token');
    const jwt_expiration = localStorage.getItem('JWT_Expiration');
    if(jwt_token !== null && jwt_expiration !== null && +jwt_expiration > Date.now()) {
      return true;
    }

    localStorage.removeItem('JWT_Token');
    localStorage.removeItem('JWT_Expiration');
    return false;
  }
}
