import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable } from 'rxjs';
import { WebsocketMessage } from '../../types';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WebsocketsService {
  private socket$: WebSocketSubject<WebsocketMessage>;

  constructor() {
    this.socket$ = webSocket(`ws://${environment.apiUrl}/sockets/chatrequests`);
  }

  getMessages(): Observable<WebsocketMessage> {
    return this.socket$.asObservable();
  }
}
