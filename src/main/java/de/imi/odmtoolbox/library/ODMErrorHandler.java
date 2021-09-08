package de.imi.odmtoolbox.library;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Manages the errors thrown by the corresponding {@link ODMParser}.
 * 
 */
public class ODMErrorHandler implements ErrorHandler {

    private ODMParser odmParser;

    public ODMErrorHandler(ODMParser odmParser) {
        this.odmParser = odmParser;
    }

    @Override
    public void warning(SAXParseException parseError) throws SAXException {
        odmParser.addError(parseError);
    }

    @Override
    public void error(SAXParseException parseError) throws SAXException {
        odmParser.addError(parseError);
    }

    @Override
    public void fatalError(SAXParseException parseError) throws SAXException {
        odmParser.addError(parseError);
    }
}