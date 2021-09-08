package de.imi.odmtoolbox.convert;

import de.imi.odmtoolbox.library.ConversionNotes;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.HashMap;

/**
 * RepairModule that removes foreign (unwanted) namespaces from an ODM.
 * 
 * TODO: A namespace could be declared inside any element. The document is not searched for further namespace definitons.
 *
 * @author Philipp Behrendt <development@beph.de>
 */
@Component
public class NamespaceStripper {

    private final static String[] allowedExternalNamespaces = {"http://www.w3.org/2000/09/xmldsig#"};

    public String moduleDescription() {
        return "Removes all foreign namespaces from an ODM.";
    }

    public RepairResult repair(RepairResult repairResult) {

        Document document = repairResult.getODMDocument();
        NamedNodeMap rootAttributes = document.getDocumentElement().getAttributes();
        HashMap<String, String> namespacesToStrip = new HashMap<>();

        //Check for other namespaces
        for (int i = 0; i < rootAttributes.getLength(); i++) {
            if (rootAttributes.item(i).getNodeName().matches("xmlns:(?!ds)(?!xsi).*")) {
                if (!Arrays.asList(allowedExternalNamespaces).contains(rootAttributes.item(i).getNodeValue())) {
                    namespacesToStrip.put(rootAttributes.item(i).getNodeName().split(":")[1], rootAttributes.item(i).getNodeValue());
                    repairResult.getConversionNotes().addNote("ODM", "Remove namespace " + rootAttributes.item(i).getNodeValue() + " from file", ConversionNotes.SeverenessLevel.NOTICE);
                    rootAttributes.removeNamedItem(rootAttributes.item(i).getNodeName());
                }
            }
        }

        //Remove namespaced elements and attributes
        NodeList nodeList = document.getElementsByTagName("*");
        for (int elementIndex = 0; elementIndex < nodeList.getLength(); elementIndex++) {
            Node node = nodeList.item(elementIndex);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
       
                if (node.getNodeName().matches(".*:.*") && node.getNodeName().split(":").length > 1) {
                    if (namespacesToStrip.containsKey(node.getNodeName().split(":")[0])) {
                        node.getParentNode().removeChild(node);
                        elementIndex = elementIndex - 1;
                    }
                } else {
                    NamedNodeMap attributes = node.getAttributes();

                    for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++) {
                        if (attributes.item(attributeIndex).getNodeName().matches(".*:.*") && attributes.item(attributeIndex).getNodeName().split(":").length > 1) {
                            if (namespacesToStrip.containsKey(attributes.item(attributeIndex).getNodeName().split(":")[0])) {
                                attributes.removeNamedItem(attributes.item(attributeIndex).getNodeName());
                                attributeIndex = attributeIndex - 1;
                            }
                        }
                    }
                }
            }
        }

        return repairResult;
    }
}
