import {Component, OnDestroy, OnInit} from "@angular/core";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { WebsocketsService } from "../../services/websockets.service";
import { Subscription } from 'rxjs';
import { WebsocketMessage } from '../../../types';
import { ChatService } from '../../services/chat.service';
import { FormErrorComponent } from '../form-error/form-error.component';
import { NgClass } from '@angular/common';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: "app-dashboard",
  imports: [ReactiveFormsModule, FormErrorComponent, NgClass],
  templateUrl: "./dashboard.component.html",
  standalone: true,
  styleUrl: "./dashboard.component.scss",
})
export class DashboardComponent implements OnInit, OnDestroy {
  requestChatForm = new FormGroup({
    targetUsername: new FormControl<string>("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  chatRequests: string[] = [];
  private messageSubscription!: Subscription;

  constructor(private websocketsService: WebsocketsService, private chatService: ChatService, private toastrService: ToastrService) {}

  ngOnInit() {
    this.messageSubscription = this.websocketsService.getMessages().subscribe(
      (message) => {
        this.handleWebsocketMessage(message);
      }
    );
  }

  ngOnDestroy() {
    this.websocketsService.closeConnection();
    this.messageSubscription.unsubscribe();
  }

  handleWebsocketMessage(message: WebsocketMessage) {
    switch (message.type) {
      case "request": {
        this.chatRequests.push(message.payload)
        break;
      }
      case "accept": {
        break;
      }
    }
  }

  requestChat() {
    this.chatService.requestChat(this.requestChatForm.getRawValue()).subscribe({
      next: response => {
        this.toastrService.success(response);
      },
      error: err => {
        this.toastrService.error(err.error);
      }
    })
  }
}
