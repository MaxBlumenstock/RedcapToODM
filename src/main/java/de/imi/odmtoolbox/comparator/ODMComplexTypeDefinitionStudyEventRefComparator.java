package de.imi.odmtoolbox.comparator;


import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyEventRef;
import java.util.Comparator;

/**
 *
 * @author Philipp Neuhaus <philipp.neuhaus@uni-muenster.de>
 */
public class ODMComplexTypeDefinitionStudyEventRefComparator implements Comparator<ODMcomplexTypeDefinitionStudyEventRef> {

    @Override
    public int compare(ODMcomplexTypeDefinitionStudyEventRef studyEventRef, ODMcomplexTypeDefinitionStudyEventRef compareStudyEventRef) {
        //Falls der das Zweite keine Ordernummer hat, gewinnt das erste.
        if (compareStudyEventRef.getOrderNumber() == null) {
            return 1;
        } else if (studyEventRef.getOrderNumber() == null) {
            return -1;
        }
        
        return studyEventRef.getOrderNumber().intValue() - compareStudyEventRef.getOrderNumber().intValue();
    }
}
