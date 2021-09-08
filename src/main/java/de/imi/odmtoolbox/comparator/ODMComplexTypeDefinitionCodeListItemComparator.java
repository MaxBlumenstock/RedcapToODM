package de.imi.odmtoolbox.comparator;

import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListItem;
import java.util.Comparator;

public class ODMComplexTypeDefinitionCodeListItemComparator implements Comparator<ODMcomplexTypeDefinitionCodeListItem> {

    @Override
    public int compare(ODMcomplexTypeDefinitionCodeListItem codeListItem, ODMcomplexTypeDefinitionCodeListItem comparedCodeListItem) {
        // Codelistitems without order number will be sorted after codelistitems with order number
        if (comparedCodeListItem.getOrderNumber() == null) {
            return 1;
        } else if (codeListItem.getOrderNumber() == null) {
            return -1;
        } else {
            return codeListItem.getOrderNumber().compareTo(comparedCodeListItem.getOrderNumber());
        }
    }
}
