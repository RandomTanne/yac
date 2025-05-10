import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignupComponent } from './signup.component';
import { AuthService } from '../../services/auth.service';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from '../../app.routes';

describe('SignupComponent', () => {
  let component: SignupComponent;
  let fixture: ComponentFixture<SignupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignupComponent],
      providers: [AuthService, provideHttpClient(), provideRouter(routes)],
    }).compileComponents();

    fixture = TestBed.createComponent(SignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
