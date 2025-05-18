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
import { NgClass } from '@angular/common';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: "app-dashboard",
  imports: [ReactiveFormsModule, NgClass],
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

  requestedChat: string | null = null;
  chatRequests: string[] = [];
  private messageSubscription!: Subscription;

  constructor(private websocketsService: WebsocketsService, private chatService: ChatService, private toastrService: ToastrService) {}

  ngOnInit() {
    this.messageSubscription = this.websocketsService.getMessages().subscribe(
      (message) => {
        this.handleWebsocketMessage(message);
      }
    );
    this.updateChatRequests();
  }

  ngOnDestroy() {
    this.websocketsService.closeConnection();
    this.messageSubscription.unsubscribe();
  }

  handleWebsocketMessage(message: WebsocketMessage) {
    switch (message.type) {
      case "request": {
        this.updateChatRequests();
        break;
      }
      case "cancel": {
        this.updateChatRequests();
        break;
      }
      case "accept": {
        break;
      }
    }
  }

  updateChatRequests() {
    this.chatService.getChatRequests().subscribe( {
      next: requests => {
        this.chatRequests = requests;
      },
      error: () => {
        this.toastrService.error("Something went wrong while fetching chats requested from you");
      }
    })
  }

  requestChat() {
    const requestedChat = this.requestChatForm.controls.targetUsername.getRawValue();
    this.chatService.requestChat(this.requestChatForm.getRawValue()).subscribe({
      next: response => {
        this.toastrService.success(response);
        this.requestedChat = requestedChat;
      },
      error: err => {
        this.toastrService.error(err.error);
      }
    })
  }

  cancelChatRequests() {
    this.chatService.cancelChatRequests().subscribe({
      next: response => {
        this.toastrService.success(response);
        this.requestedChat = null;
      },
      error: () => {
        this.toastrService.error("Something went wrong while canceling chat requests");
      }
    })
  }
}
