import { Component } from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {NgClass} from '@angular/common';
import {FormErrorComponent} from '../../form-error/form-error.component';

@Component({
  selector: 'app-signup',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    NgClass,
    FormErrorComponent
  ],
  templateUrl: './signup.component.html',
  standalone: true,
  styleUrl: './signup.component.scss'
})

export class SignupComponent {
  constructor(private authService: AuthService, private router: Router) {}

  signupForm = new FormGroup({
    password: new FormControl<string>('', { nonNullable: true, validators: [Validators.required, Validators.minLength(12)] })
  })

  loginSuccessful = true;

  onSubmit() {
    this.authService.signup(this.signupForm.getRawValue()).subscribe(response => {
      this.loginSuccessful = response;
    });
  }

  protected readonly localStorage = localStorage;
}
