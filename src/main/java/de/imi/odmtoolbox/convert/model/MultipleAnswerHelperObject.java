package de.imi.odmtoolbox.convert.model;


import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class MultipleAnswerHelperObject {
    private String baseName;
    private List<Element> itemDefs = new ArrayList<>();
    private List<Element> codeLists = new ArrayList<>();
    private Map<String, String> codeListValues = new HashMap<>();

}
