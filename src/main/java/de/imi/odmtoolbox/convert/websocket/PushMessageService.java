package de.imi.odmtoolbox.convert.websocket;

import de.imi.odmtoolbox.convert.model.OutputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PushMessageService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private OutputMessage messages = new OutputMessage();

    public void sendMessages() {
        simpMessagingTemplate.convertAndSend("/update/messages", messages);
        this.messages = new OutputMessage();
    }

    public void addMessage(String message) {
        messages.addLine(message);
    }
}
