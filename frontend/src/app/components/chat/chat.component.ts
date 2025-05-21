import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ChatMessage, WebsocketMessage } from '../../../types';
import { WebsocketsService } from '../../services/websockets.service';
import { Subscription } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import {
  atou8a,
  decryptAES,
  deriveAESKey,
  encryptAES,
  exportPublicKey,
  generateECDHKeyPair,
  importPublicKey,
  u8atoa,
} from '../../../crypto';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss',
})
export class ChatComponent implements OnInit, OnDestroy {
  targetUsername: string | null = null;
  messages: ChatMessage[] = [];

  ecKeyPair: CryptoKeyPair | null = null;
  aesKey: CryptoKey | null = null;
  peerPublicKey: CryptoKey | null = null;

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
    private toastrService: ToastrService,
  ) {}

  async ngOnInit() {
    this.targetUsername = this.route.snapshot.paramMap.get('username');
    if (this.targetUsername == null) {
      this.router.navigate(['/']);
      return;
    }

    this.messageSubscription = this.websocketsService
      .getMessages()
      .subscribe((message) => {
        this.handleWebsocketMessage(message);
      });

    this.ecKeyPair = await generateECDHKeyPair();
    const pubkey = await exportPublicKey(this.ecKeyPair);
    this.sendRawMessage(JSON.stringify({ type: 'pubkey', pubkey }));
  }

  ngOnDestroy() {
    this.messageSubscription?.unsubscribe();
    this.chatService.cancelChatRequests().subscribe({
      error: () => { return; }
    });
  }

  private sendRawMessage(payload: string, plaintext: string | null = null) {
    this.chatService
      .sendMessage({
        message: payload,
        targetUsername: this.targetUsername!,
      })
      .subscribe({
        next: () => {
          if (plaintext) {
            this.messages.push({ ownMessage: true, message: plaintext });
          }
          this.chatForm.controls.message.reset();
        },
        error: (err) => {
          this.toastrService.error(err.error);
        },
      });
  }

  async handleWebsocketMessage(message: WebsocketMessage) {
    if (message.type === 'cancel') {
      this.toastrService.info('The other user has left the chat');
      return;
    }

    try {
      const parsed = JSON.parse(message.payload);

      if (parsed.type === 'pubkey' && parsed.pubkey && !this.aesKey) {
        this.peerPublicKey = await importPublicKey(parsed.pubkey);
        this.aesKey = await deriveAESKey(
          this.ecKeyPair!.privateKey,
          this.peerPublicKey,
        );
        return;
      }

      if (parsed.type === 'encrypted' && parsed.ciphertext && parsed.iv) {
        const ciphertext = atou8a(parsed.ciphertext);
        const iv = atou8a(parsed.iv);
        const decrypted = await decryptAES(this.aesKey!, ciphertext, iv);
        const decoded = new TextDecoder().decode(decrypted);

        this.messages.push({ ownMessage: false, message: decoded });
        return;
      }
    } catch (e) {
      // TODO: sth better
      this.toastrService.error(String(e));
      this.messages.push({ ownMessage: false, message: message.payload });
    }
  }

  async sendMessage() {
    const plaintext = this.chatForm.controls.message.value;
    if (!this.aesKey) {
      this.toastrService.warning('Key exchange not completed yet');
      return;
    }

    const encoded = new TextEncoder().encode(plaintext);
    const { ciphertext, iv } = await encryptAES(this.aesKey, encoded);

    const payload = JSON.stringify({
      type: 'encrypted',
      ciphertext: u8atoa(new Uint8Array(ciphertext)),
      iv: u8atoa(iv),
    });

    this.sendRawMessage(payload, plaintext);
  }

  leaveChat() {
    this.router.navigate(['/']);
  }
}
