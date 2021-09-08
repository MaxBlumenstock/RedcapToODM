package de.imi.odmtoolbox.comparator;

import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupRef;
import java.util.Comparator;

/**
 *
 * @author Philipp Neuhaus <philipp.neuhaus@uni-muenster.de>
 */
public class ODMComplexTypeDefinitionItemGroupRefComparator implements Comparator<ODMcomplexTypeDefinitionItemGroupRef> {

    @Override
    public int compare(ODMcomplexTypeDefinitionItemGroupRef itemGroupRef, ODMcomplexTypeDefinitionItemGroupRef compareItemGroupRef) {
        //Falls der das Zweite keine Ordernummer hat, gewinnt das erste.
        if (compareItemGroupRef.getOrderNumber() == null) {
            return 1;
        } else if (itemGroupRef.getOrderNumber() == null) {
            return -1;
        }
        
        return itemGroupRef.getOrderNumber().intValue() - compareItemGroupRef.getOrderNumber().intValue();
    }
}
