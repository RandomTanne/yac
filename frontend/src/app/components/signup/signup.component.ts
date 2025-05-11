import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { NgClass } from '@angular/common';
import { FormErrorComponent } from '../form-error/form-error.component';

@Component({
  selector: 'app-signup',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    NgClass,
    FormErrorComponent,
  ],
  templateUrl: './signup.component.html',
  standalone: true,
  styleUrl: './signup.component.scss',
})
export class SignupComponent {
  constructor(private authService: AuthService) {}

  signupForm = new FormGroup({
    password: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(12)],
    }),
  });

  signupSuccessful = true;

  onSubmit() {
    this.authService
      .signup(this.signupForm.getRawValue())
      .subscribe((response) => {
        this.signupSuccessful = response;
      });
  }

  protected readonly localStorage = localStorage;
}
