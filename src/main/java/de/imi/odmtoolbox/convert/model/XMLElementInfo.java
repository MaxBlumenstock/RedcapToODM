package de.imi.odmtoolbox.convert.model;

import de.imi.odmtoolbox.convert.model.ElementWithID;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class XMLElementInfo {
    Map<ElementWithID, List<ElementWithID>> parentChildElements = new HashMap<>();
    Map<ElementWithID, Map<String, String>> elementAttributes = new HashMap<>();
    Map<ElementWithID, String> elementTexts = new HashMap<>();
}
