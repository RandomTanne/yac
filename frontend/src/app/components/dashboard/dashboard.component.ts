import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";

@Component({
    selector: "dashboard",
    styleUrl: "dashboard.component.scss",
    templateUrl: "dashboard.component.html",
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true
})
export class DashboardComponent {

    uuidControl = new FormControl('', [Validators.required])

    submitIfValid(): void {
        console.log(this.uuidControl.getRawValue());
    }
}
