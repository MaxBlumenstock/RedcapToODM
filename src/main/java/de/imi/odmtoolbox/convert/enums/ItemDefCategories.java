package de.imi.odmtoolbox.convert.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ItemDefCategories {
    FIELD_NOTE("redcap:FieldNote"),
    FIELD_TYPE("redcap:FieldType"),
    CHECKBOX ("checkbox"),
    CALCULATION("redcap:Calculation"),
    BRANCHING_LOGIC("redcap:BranchingLogic"),
    ALL("all");

    private final String asString;
    public static ItemDefCategories ofKey(String key) {
        for(ItemDefCategories category : values() ) {
            if (category.asString.equals(key)) {
                return category;
            }
        }
        return null;
    }
}
