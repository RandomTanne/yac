import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  imports: [
    RouterLink,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './signup.component.html',
  standalone: true,
  styleUrl: './signup.component.scss'
})
export class SignupComponent {
  signupForm = new FormGroup({
    password: new FormControl('', [Validators.required, Validators.minLength(12)])
  })

  constructor(private authService: AuthService) {}

  onSubmit() {
    this.authService.signup({ password: this.signupForm.controls.password.value! }).subscribe();
  }
}
