package de.imi.odmtoolbox.controller;

import de.imi.odmtoolbox.convert.model.Status;
import de.imi.odmtoolbox.convert.model.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UpdateMessageController {

    @MessageMapping(value = "/status")
    @SendTo("/update/messages")
    public OutputMessage greeting(Status status) throws Exception {
        OutputMessage message = new OutputMessage();
        message.addLine(HtmlUtils.htmlEscape(status.getStatus()));
        message.addLine(HtmlUtils.htmlEscape("weitere message"));
        return message;
    }
}
