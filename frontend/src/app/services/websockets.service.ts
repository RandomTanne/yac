import { Injectable } from "@angular/core";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { Observable } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class WebsocketsService {
  private socket$: WebSocketSubject<any>;

  constructor() {
    this.socket$ = webSocket("ws://localhost:8080/sockets/chatrequests");
  }

  sendMessage(message: any) {
    this.socket$.next(message);
  }

  getMessages(): Observable<any> {
    return this.socket$.asObservable();
  }

  closeConnection() {
    this.socket$.complete();
  }
}
