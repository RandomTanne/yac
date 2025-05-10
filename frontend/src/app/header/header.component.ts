import {Component, OnInit} from '@angular/core';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.component.html',
  standalone: true,
  styleUrl: './header.component.scss'
})
export class HeaderComponent implements OnInit {
  constructor(private authService: AuthService) {
  }

  username: null | string = null;

  ngOnInit() {
    this.username = this.authService.getUsername();
  }

  logout() {
    this.authService.logout();
  }
}
