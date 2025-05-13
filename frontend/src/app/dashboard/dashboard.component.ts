import { Component } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  imports: [ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  standalone: true,
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  requestChatForm = new FormGroup({
    targetUsername: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });
}
