import { Routes } from '@angular/router';
import { LoginComponent  } from './components/login/login.component';
import { SignupComponent  } from './components/signup/signup.component';
import { LoginGuard } from './services/auth-guard.service';


export const routes: Routes = [
  { path: "login", component: LoginComponent, canActivate: [LoginGuard] },
  { path: "signup", component: SignupComponent, canActivate: [LoginGuard] },
];
