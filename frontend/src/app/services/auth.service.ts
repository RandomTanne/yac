import { HttpClient } from '@angular/common/http';
import {catchError, map, Observable, of} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient) {}

  isLoggedIn = false;

  login(userDetails: { username: string; password: string }): Observable<boolean> {
    return this.http.post('http://localhost:8080/api/auth/signin', userDetails, { responseType: 'text' })
      .pipe(
        map(response => {
          localStorage.setItem('JWT_Token', response);
          this.isLoggedIn = true;
          return true;
        }),
        catchError(error => {
          console.log(error);
          this.isLoggedIn = false;
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
          console.log(error);
          this.isLoggedIn = false;
          return of(false);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('JWT_Token');
    this.isLoggedIn = false;
  }

  isAuthenticated(): boolean {
    return this.isLoggedIn;
  }
}
