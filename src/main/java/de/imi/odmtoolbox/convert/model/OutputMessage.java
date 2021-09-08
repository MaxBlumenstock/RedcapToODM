package de.imi.odmtoolbox.convert.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class OutputMessage {

    private final List<String> content = new ArrayList<>();
    public void addLine(String message) {
        content.add(message);
    }
}
