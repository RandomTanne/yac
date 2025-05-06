import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
@Component({
  selector: 'app-login',
  imports: [
    RouterLink,
    FormsModule
  ],
  templateUrl: './login.component.html',
  standalone: true,
  styleUrl: './login.component.scss'
})

export class LoginComponent {
  username: string = '';
  password: string = '';

  constructor(private authService: AuthService) {}
  onSubmit() {
    this.authService.login({username: this.username, password: this.password}).subscribe();
  }
}
