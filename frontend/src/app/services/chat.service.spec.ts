import { TestBed } from '@angular/core/testing';

import { ChatService } from './chat.service';
import {provideHttpClient} from '@angular/common/http';

describe('ChatService', () => {
  let service: ChatService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(ChatService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
