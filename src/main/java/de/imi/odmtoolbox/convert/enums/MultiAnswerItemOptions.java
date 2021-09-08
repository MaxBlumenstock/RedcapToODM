package de.imi.odmtoolbox.convert.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MultiAnswerItemOptions {
    REPEAT_QUESTION("Repeat question in every item"),
    QUESTION_ONLY_FIRST_ITEM ("Show full question in first item only"),
    ADD_ITEM_GROUP("Add Itemgroup with question");

    private final String asString;
    public static MultiAnswerItemOptions ofKey(String key) {
        for(MultiAnswerItemOptions option : values() ) {
            if (option.asString.equals(key)) {
                return option;
            }
        }
        return null;
    }
}
