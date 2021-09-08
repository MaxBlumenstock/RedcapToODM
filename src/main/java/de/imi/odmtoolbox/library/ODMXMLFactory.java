/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.imi.odmtoolbox.library;

import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Philipp Neuhaus <Philipp.Neuhaus@uni-muenster.de>
 */
public class ODMXMLFactory {
   private Document doc;

        
   public ODMXMLFactory (MultipartFile odmFile) throws Exception{
       
       Builder parser = new Builder();
       doc = parser.build(odmFile.getInputStream());

   }

   public HashSet<Element> getAllTranslatedTextContainers(){
              
       HashSet<Element> ttContainer = new HashSet<>();
//       Nodes containers = doc.query("//Symbol|//Question|//Decode|//ErrorMessage|//Description");
       Nodes containers = doc.query("//*[local-name() = 'Symbol']|//*[local-name() = 'Question']|//*[local-name() = 'Decode']|//*[local-name() = 'ErrorMessage']|//*[local-name() = 'Description']");
       
       for (int i = 0; i < containers.size(); i++){
           ttContainer.add((Element)containers.get(i));
       }
       
       return ttContainer;
   }
   
   public String getXmlString(){
       return doc.toXML();
   }
   
   public String getXmlns(){
       return doc.getRootElement().getNamespaceURI();
   }
}
