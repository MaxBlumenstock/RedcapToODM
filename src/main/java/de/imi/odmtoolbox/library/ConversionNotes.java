package de.imi.odmtoolbox.library;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * This Class represents a comma seperated value file which holds information
 * about conversion errors and warnings.
 *
 * @author m_hein31
 */
public class ConversionNotes {
    private LinkedList<ConversionNote> notes;
    private static final String HEADER = "itemOID;message;severeness";

    /**
     * The severeness of a conversion note can have the values NOTICE, WARNING
     * and CRITICAL.
     */
    public enum SeverenessLevel {
        NOTICE, WARNING, CRITICAL
    }

    /**
     * This Class represents a ConversionNote. A ConversionNote consists of
     * itemOID, message, and severeness level.
     */
    private class ConversionNote {

        private String itemOID;
        private String message;
        private SeverenessLevel severeness;

        /**
         * Creates a new conversion note.
         *
         * @param itemOID The OID of the item where the conversion problem was
         * detected. Must not be <code>null</code> or empty.
         * @param message The message that will be added to the note. Must not
         * be <code>null</code> or empty.
         * @param severeness The level of severeness for this note. Must not be
         * <code>null</code>.
         */
        public ConversionNote(String itemOID, String message, SeverenessLevel severeness) {
            this.itemOID = itemOID;
            this.message = message;
            this.severeness = severeness;
        }

        /**
         * Returns a String representation of the ConversionNote object.
         *
         * @return The String representation.
         */
        @Override
        public String toString() {
            return itemOID +";"+ message.replace("\"", "\\\"").replace("'", "\\'") +";"+ severeness.toString();
        }
    }

    /**
     * Adds a note to the list of conversion notes.
     *
     * @param itemOID The OID of the item where the conversion problem was
     * detected. Must not be <code>null</code> or empty.
     * @param message The message that will be added to the note. Must not be
     * <code>null</code> or empty.
     * @param severeness The level of severeness for this note. Must not be
     * <code>null</code>.
     */
    public void addNote(String itemOID, String message, SeverenessLevel severeness) {
        // Check if the parameters are valid.
        if (itemOID == null) {
            throw new NullPointerException("The itemOID must not be null.");
        } else if (itemOID.isEmpty()) {
            throw new IllegalArgumentException("The itemOID must not be empty.");
        }
        if (message == null) {
            throw new NullPointerException("The message must not be null.");
        } else if (message.isEmpty()) {
            throw new IllegalArgumentException("The message must not be empty.");
        }
        if (severeness == null) {
            throw new NullPointerException("The severeness must not be null.");
        }
        // Initialize notes if they were not already intitialized
        if (notes == null) {
            notes = new LinkedList<>();
        }
        if (!notes.add(new ConversionNote(itemOID, message, severeness))) {
            throw new RuntimeException(String.format("Could not add the note: %s", new ConversionNote(itemOID, message, severeness).toString()));
        }
    }

    /**
     * Returns the csv text starting with the header and one note per row.
     *
     * @return The csv text as String.
     */
    @Override
    public String toString() {
        // Write header first
        String csvText = HEADER + "\n";
        // Write all notes to the csvText
        for (ConversionNote note : notes) {
            csvText += note.toString() + "\n";
        }
        // Cut the last line end off and return the String
        return csvText.substring(0, csvText.length() - 1);
    }

    /**
     * Returns the rows of the csv file as list of Strings
     *
     * @return The rows of the csv file as list of Strings
     */
    public List<String> getNotesList() {
        LinkedList<String> notesList = new LinkedList<>();
        for (ConversionNote note : notes) {
            notesList.add(note.toString());
        }
        return notesList;
    }

    /**
     * Returns a csv text file that contains all conversion notes.
     *
     * @return The csv text file as byte[]
     */
    public byte[] getConversionNotes() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteStream);
        out.print(this.toString());
        return byteStream.toByteArray();
    }

    /**
     * Returns true if there are no saved ConversionNotes otherwise returns
     * false.
     *
     * @return boolean that says if there are saved ConvesionNotes present or
     * not.
     */
    public boolean isEmpty() {
        if (notes == null) {
            return true;
        } else {
            return notes.isEmpty();
        }
    }
}
