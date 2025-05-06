import {Component} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone: true
})
export class AppComponent {
  private router: Router = new Router();
  title = 'yac';


  navigateToDashboard() {
    this.router.navigate(['dashboard'])
  }
}
