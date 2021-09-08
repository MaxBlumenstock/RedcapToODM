package de.imi.odmtoolbox.library;

import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class provides methods to parse and validate elements from an
 * Operational Data Model (ODM) file.
 *
 */
@Service
public class ODMParser {

    @Autowired
    ServletContext servletContext;

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder documentBuilder;
    private List<SAXException> parseErrors = new ArrayList<>();

    public ODMParser() {
        documentBuilderFactory.setNamespaceAware(true);
        try {
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
        }

    }

    /**
     * Renames the XML namespace for the given {@link Node} and recursively for
     * all child nodes.
     *
     * @param node The node for which the namespace should be changed.
     * @param namespace The new name of the XML namespace.
     */
    public static void renameNamespaceRecursive(Node node, String namespace) {
        Document document = node.getOwnerDocument();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            document.renameNode(node, namespace, node.getNodeName());
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); ++i) {
            renameNamespaceRecursive(list.item(i), namespace);
        }
    }

    /**
     * Parses a given ODM file into a {@link Document} and collects the errors.
     *
     * @param odmFile The ODM file which should be parsed.
     * @return The parsed {@link Document}.
     */
    public Document parseODMFile(MultipartFile odmFile) {
        try {
            return parseODMFile(odmFile.getInputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a given ODM file as InputStream into a {@link Document} and
     * collects the errors.
     *
     * @param odmFileInputStream The ODM file as InputStream which should be
     * parsed.
     * @return The parsed {@link Document}.
     */
    public Document parseODMFile(InputStream odmFileInputStream) {
        Document doc = null;
        try {
            doc = documentBuilder.parse(odmFileInputStream);
        } catch (SAXException parseError) {
            addError(parseError);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return doc;
    }

    /**
     * Parses a given ODM file into a {@link ODM}
     *
     * @param odmFile The ODM file which should be parsed.
     * @return The parsed {@link ODM}.
     * @throws SAXException The parsing Errors
     * @throws JAXBException Error reading the odm file
     * @throws IOException Error while reading file from server.
     * @throws NullPointerException If the odmFile was <code>null</code>.
     */
    public ODM parseODMFileToODM(MultipartFile odmFile) throws SAXException, JAXBException, IOException, NullPointerException {
        return parseODMFileToODM(odmFile.getInputStream());
    }

    /**
     * Parses a given ODM file as InputStream into a {@link ODM}
     *
     * @param odmFileInputStream The ODM file as InputStream which should be
     * parsed.
     * @return The parsed {@link ODM}.
     * @throws SAXException The parsing Errors
     * @throws JAXBException Error reading the odm file
     * @throws IOException Error while reading file from server.
     * @throws NullPointerException If the odmFile was <code>null</code>.
     */
    public ODM parseODMFileToODM(InputStream odmFileInputStream) throws SAXException, JAXBException, IOException, NullPointerException {
        if (odmFileInputStream == null) {
            throw new NullPointerException("Expected parameter MultipartFile but recieved null.");
        }
        ODM importedODM = null;

        Document doc = parseODMFile(odmFileInputStream);
        //Get the right shema
        File schemaLocation = null;

        Element odmElement = (Element) doc.getElementsByTagName("ODM").item(0);
        if (odmElement != null) {
            switch (odmElement.getAttribute("ODMVersion")) {
                case "1.3":
                case "1.3.1":
                    String path = servletContext.getRealPath("/xsd") + "/odm1-3-1/ODM1-3-1.xsd";
                    schemaLocation = new File(path);
                    break;
                case "1.3.2":
                    schemaLocation = new File(servletContext.getRealPath("/xsd") + "/odm1-3-2/ODM1-3-2.xsd");
                    break;
                default:
                    throw (new SAXParseException("ODM Version " + odmElement.getAttribute("ODMVersion") + " is not supported", null, null, -1, -1));
            }
        } else {
            throw (new SAXParseException("Element with tag name 'ODM' not readable", null, null, -1, -1));
        }

        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = schemaFactory.newSchema(schemaLocation);

        Unmarshaller unmarshaller = JAXBContext.newInstance(ODM.class)
                .createUnmarshaller();
        unmarshaller.setSchema(schema);

        importedODM = (ODM) unmarshaller.unmarshal(doc);

        return importedODM;
    }

    /**
     * Validates a given {@link Document} and returns all errors.
     *
     * @param document The {@link Document} which should be validated.
     * @return The list of errors for the given {@link Document}.
     */
    public List<SAXException> isValid(Document document) {
        if (document != null) {
            try {
                File schemaLocation = null;
                Element odmElement = (Element) document.getElementsByTagName("ODM").item(0);
                if (odmElement != null) {
                    switch (odmElement.getAttribute("ODMVersion")) {
                        case "1.3":
                        case "1.3.1":
                            String path = servletContext.getRealPath("/xsd") + "/odm1-3-1/ODM1-3-1.xsd";
                            schemaLocation = new File(path);
                            break;
                        case "1.3.2":
                            schemaLocation = new File(servletContext.getRealPath("/xsd") + "/odm1-3-2/ODM1-3-2.xsd");
                            break;
                        default:
                            throw (new SAXParseException("ODM Version " + odmElement.getAttribute("ODMVersion") + " is not supported", null, null, -1, -1));
                    }
                } else {
                    throw (new SAXParseException("Element with tag name 'ODM' not readable", null, null, -1, -1));
                }

                SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                Schema schema = schemaFactory.newSchema(schemaLocation);
                documentBuilderFactory.setSchema(schema);
                Validator schemaValidator = schema.newValidator();
                schemaValidator.setErrorHandler(new ODMErrorHandler(this));

                DOMSource source = new DOMSource(document);
                schemaValidator.validate(source);

                // Validate data types of the codelists
                NodeList codeLists = document.getElementsByTagName("CodeList");
                // Loop through all existing codelists
                for (int i = 0; i < codeLists.getLength(); i++) {
                    // Loop through all child nodes of the codelist
                    for (Node codeListItem = codeLists.item(i).getFirstChild(); codeListItem != null; codeListItem = codeListItem.getNextSibling()) {
                        // Ignore text nodes like newlines
                        if (codeListItem.getNodeType() == Node.ELEMENT_NODE) {
                            // Validate all codelistitems against the codelist's data type
                            switch (codeLists.item(i).getAttributes().getNamedItem("DataType").getNodeValue().toLowerCase()) {
                                case "integer":
                                    try {
                                        Integer.parseInt(codeListItem.getAttributes().getNamedItem("CodedValue").getNodeValue());
                                    } catch (NumberFormatException e) {
                                        this.addError(new SAXException(String.format("The Datatype of the CodeList with OID:\"%s\" is \"integer\" but the CodedValue \"%s\" is not \"integer\".", codeLists.item(i).getAttributes().getNamedItem("OID").getNodeValue(), codeListItem.getAttributes().getNamedItem("CodedValue").getNodeValue())));
                                    }
                                    break;
                                case "float":
                                    try {
                                        Float.parseFloat(codeListItem.getAttributes().getNamedItem("CodedValue").getNodeValue());
                                    } catch (NumberFormatException e) {
                                        this.addError(new SAXException(String.format("The Datatype of the CodeList with OID:\"%s\" is \"float\" but the CodedValue \"%s\" is not \"float\".", codeLists.item(i).getAttributes().getNamedItem("OID").getNodeValue(), codeListItem.getAttributes().getNamedItem("CodedValue").getNodeValue())));
                                    }
                                    break;
                                default:
                            }
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (SAXException saxException) {
                this.addError(saxException);
            }
        }

        return clearAndReturnParseErrors();
    }

    protected void addError(SAXException parseError) {
        this.parseErrors.add(parseError);
    }

    private List<SAXException> getParseErrors() {
        return this.parseErrors;
    }

    private List<SAXException> clearAndReturnParseErrors() {
        List<SAXException> documentExceptions = new ArrayList<>(getParseErrors());
        this.parseErrors.clear();
        return documentExceptions;
    }
}
