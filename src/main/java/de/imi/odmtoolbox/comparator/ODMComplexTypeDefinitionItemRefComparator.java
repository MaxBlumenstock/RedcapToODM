package de.imi.odmtoolbox.comparator;

import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemRef;
import java.util.Comparator;

/**
 *
 * @author Philipp Neuhaus <philipp.neuhaus@uni-muenster.de>
 */
public class ODMComplexTypeDefinitionItemRefComparator implements Comparator<ODMcomplexTypeDefinitionItemRef> {

    @Override
    public int compare(ODMcomplexTypeDefinitionItemRef itemRef, ODMcomplexTypeDefinitionItemRef compareItemRef) {
        //Falls der das Zweite keine Ordernummer hat, gewinnt das erste.
        if (compareItemRef.getOrderNumber() == null) {
            return 1;
        } else if (itemRef.getOrderNumber() == null) {
            return -1;
        }
        
        return itemRef.getOrderNumber().intValue() - compareItemRef.getOrderNumber().intValue();
    }
}
