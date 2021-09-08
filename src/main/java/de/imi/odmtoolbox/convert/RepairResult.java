package de.imi.odmtoolbox.convert;

import de.imi.odmtoolbox.library.ConversionNotes;
import org.w3c.dom.Document;

import java.util.LinkedList;
import java.util.List;

/**
 * Class to hold all results (processed ODM and ConversionNotes) from a
 * RepairModule and the RepairService.
 * <p>
 * As a converter can provide multiple repair solutions we hava list of ODM Documents here.
 *
 * @author Leonard Greulich <l_greu02@uni-muenster.de>
 * @author Philipp Behrendt <development@beph.de>
 */
public class RepairResult {

    private final List<Document> odmDocuments = new LinkedList<>();
    private ConversionNotes conversionNotes;

    /**
     * Constructor with arguments.
     * 
     * @param odmDocument ODM Document to repair.
     * @param conversionNotes Conversion notes.
     */
    public RepairResult(Document odmDocument, ConversionNotes conversionNotes) {
        this.odmDocuments.add(odmDocument);
        this.conversionNotes = conversionNotes;
    }

    /**
     * Gets the first ODM Document.
     *
     * @return Document.
     */
    public Document getODMDocument() {
        return odmDocuments.get(0);
    }
    
    /**
     * Gets all ODM Documents.
     * 
     * @return Document.
     */
    public List<Document> getODMDocuments(){
        return odmDocuments;
    }

    /**
     * Sets the first ODM Document.
     * 
     * @param odmDocument Document to set.
     */
    public void setODMDocument(Document odmDocument) {
        odmDocuments.set(0, odmDocument);
    }
    
    /**
     * Adds an ODM Document to the Repair Result.
     * 
     * @param odmDocument Document to add.
     */
    public void addODMDocument(Document odmDocument){
        odmDocuments.add(odmDocument);
    }

    /**
     * Checks for multiple repaired Documents.
     * 
     * @return boolean flag.
     */
    public boolean hasMultipleDocuments() {
        return odmDocuments.size() > 1;
    }

    /**
     * Getter for ConversionNotes.
     * 
     * @return ConversionNotes.
     */
    public ConversionNotes getConversionNotes() {
        return conversionNotes;
    }

    /**
     * Setter for ConversionNotes.
     * 
     * @param conversionNotes ConversionNotes.
     */
    public void setConversionNotes(ConversionNotes conversionNotes) {
        this.conversionNotes = conversionNotes;
    }
}
