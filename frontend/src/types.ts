export interface WebsocketMessage {
  type: string;
  payload: string;
}

export interface ChatMessage {
  ownMessage: boolean;
  message: string;
}
