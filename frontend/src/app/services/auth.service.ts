import { HttpClient } from '@angular/common/http';
import {catchError, map, Observable, of} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) {}

  login(userDetails: { username: string; password: string }): Observable<boolean> {
    return this.http.post('http://localhost:8080/api/auth/signin', userDetails, { responseType: 'text' })
      .pipe(
        map(response => {
          localStorage.setItem('JWT_Token', response);
          return true;
        }),
        catchError(error => {
          return of(error);
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
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('JWT_Token')
  }
}
