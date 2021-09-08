package de.imi.odmtoolbox.convert.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ElementWithID {
    @NonNull private int id;
    @NonNull private String name;
}
