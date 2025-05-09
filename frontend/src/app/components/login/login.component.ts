import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {NgClass} from '@angular/common';
import {FormErrorComponent} from '../../form-error/form-error.component';

@Component({
  selector: 'app-login',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    NgClass,
    FormErrorComponent
  ],
  templateUrl: './login.component.html',
  standalone: true,
  styleUrl: './login.component.scss'
})

export class LoginComponent {
  constructor(private authService: AuthService) {}

  loginForm = new FormGroup({
    username: new FormControl<string>('', { nonNullable: true, validators: Validators.required }),
    password: new FormControl<string>('', { nonNullable: true, validators: Validators.required })
  })

  onSubmit() {
    this.authService.login(this.loginForm.getRawValue()).subscribe();
  }
}
