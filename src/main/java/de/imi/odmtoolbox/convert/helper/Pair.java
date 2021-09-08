package de.imi.odmtoolbox.convert.helper;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Pair<T,U> {
    @NonNull private T key;
    @NonNull private U value;
}
