import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ChatService} from '../../services/chat.service';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {ChatMessage} from '../../../types';

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './chat.component.html',
  standalone: true,
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit {
  targetUsername: string | null = null;
  messages: ChatMessage[] = [];

  chatForm = new FormGroup({
    message: new FormControl<string>("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  constructor(private router: Router, private route: ActivatedRoute, private chatService: ChatService) { }

  ngOnInit() {
    this.targetUsername = this.route.snapshot.paramMap.get('username');
    if(this.targetUsername == null) {
      this.router.navigate(["/"]);
    }
  }

  sendMessage() {
    const message = this.chatForm.controls.message.value;
    this.chatService.sendMessage(this.chatForm.getRawValue()).subscribe({
      next: () => {
        this.messages.push({ownMessage: true, message: message});
      }
    });
  }
}
