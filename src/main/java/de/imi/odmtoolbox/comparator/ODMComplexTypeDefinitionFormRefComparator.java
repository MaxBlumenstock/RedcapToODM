package de.imi.odmtoolbox.comparator;


import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormRef;
import java.util.Comparator;

/**
 *
 * @author Philipp Neuhaus <philipp.neuhaus@uni-muenster.de>
 */
public class ODMComplexTypeDefinitionFormRefComparator implements Comparator<ODMcomplexTypeDefinitionFormRef> {

    @Override
    public int compare(ODMcomplexTypeDefinitionFormRef formRefRef, ODMcomplexTypeDefinitionFormRef compareFormRef) {
        //Falls der das Zweite keine Ordernummer hat, gewinnt das erste.
        if (compareFormRef.getOrderNumber() == null) {
            return 1;
        } else if (formRefRef.getOrderNumber() == null) {
            return -1;
        }
        
        return formRefRef.getOrderNumber().intValue() - compareFormRef.getOrderNumber().intValue();
    }
}
