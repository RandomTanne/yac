import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  constructor(private http: HttpClient) {}

  requestChat(targetUsername: { targetUsername: string }): Observable<string> {
    return this.http.post(
      'http://localhost:8080/api/chat/request',
      targetUsername,
      {
        responseType: 'text',
        withCredentials: true,
      },
    );
  }

  getChatRequests(): Observable<string[]> {
    return this.http.get<string[]>('http://localhost:8080/api/chat/requested', {
      withCredentials: true,
    });
  }

  cancelChatRequests(): Observable<string> {
    return this.http.post('http://localhost:8080/api/chat/cancel', null, {
      responseType: 'text',
      withCredentials: true,
    });
  }

  sendMessage(messageDTO: { targetUsername: string, message: string }): Observable<string> {
    return this.http.post('http://localhost:8080/api/chat/send', messageDTO, {
      responseType: 'text',
      withCredentials: true,
    });
  }

  acceptChat(targetUsername: { targetUsername: string }): Observable<string> {
    return this.http.post(
      'http://localhost:8080/api/chat/accept',
      targetUsername,
      {
        responseType: 'text',
        withCredentials: true,
      },
    );
  }
}
