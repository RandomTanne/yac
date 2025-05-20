import {Component, OnDestroy, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {ChatMessage, WebsocketMessage} from '../../../types';
import {WebsocketsService} from '../../services/websockets.service';
import {Subscription} from 'rxjs';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-chat',
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './chat.component.html',
  standalone: true,
  styleUrl: './chat.component.scss',
})
export class ChatComponent implements OnInit, OnDestroy {
  targetUsername: string | null = null;
  messages: ChatMessage[] = [];
  private messageSubscription!: Subscription;

  chatForm = new FormGroup({
    message: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private chatService: ChatService,
    private websocketsService: WebsocketsService,
    private toastrService: ToastrService
  ) {}

  ngOnInit() {
    this.targetUsername = this.route.snapshot.paramMap.get('username');
    if (this.targetUsername == null) {
      this.router.navigate(['/']);
    }

    this.messageSubscription = this.websocketsService
      .getMessages()
      .subscribe((message) => {
        this.handleWebsocketMessage(message);
      });
  }

  ngOnDestroy() {
    this.messageSubscription.unsubscribe();
    this.chatService.cancelChatRequests().subscribe();
  }

  handleWebsocketMessage(message: WebsocketMessage) {
    switch (message.type) {
      case 'message': {
        this.messages.push({ownMessage: false, message: message.payload});
        break;
      }
      case 'cancel': {
        this.toastrService.info("The other user has left the chat");
      }
    }
  }

  sendMessage() {
    const message = this.chatForm.controls.message.value;
    this.chatService.sendMessage({message: this.chatForm.controls.message.value, targetUsername: this.targetUsername!}).subscribe({
      next: () => {
        this.messages.push({ ownMessage: true, message: message });
      },
      error: err => {
        this.toastrService.error(err.error);
      }
    });
  }
}
