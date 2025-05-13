import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'app-form-error',
  imports: [],
  templateUrl: './form-error.component.html',
  standalone: true,
  styleUrl: './form-error.component.css',
})
export class FormErrorComponent {
  @Input() control: AbstractControl | null = null;

  @Input()
  error = '';

  @Input()
  message = '';
}
