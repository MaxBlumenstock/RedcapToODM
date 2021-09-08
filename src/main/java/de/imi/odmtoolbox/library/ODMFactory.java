/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.imi.odmtoolbox.library;


import de.unimuenster.imi.org.cdisc.odm.v132.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Philipp Neuhaus <Philipp.Neuhaus@uni-muenster.de>
 */
public class ODMFactory {
    /**
     * Holds the ODM to work with.
     */
    private ODM odm;
    private ODMcomplexTypeDefinitionFormDef currentWorkingForm;
    private ODMcomplexTypeDefinitionMetaDataVersion currentWorkingMDV;
    
    Schema schema;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ODMFactory.class);
    
    ServletContext servletContext;

    
    /**
     * Standardconstructor. Creates a class from an empty template.
     */
    public ODMFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
        
        this.loadTemplate();
        
        //Change the FileOID to a new unique file OID
        this.odm.setFileOID(OIDGenerator.getInstance().getOID());
        
        this.currentWorkingForm = this.getFormDef("F.1");
        this.currentWorkingMDV = this.getMetaDataVersionForForm(this.currentWorkingForm);
    }
    
     /**
     * Contructor that loads an uploaded ODMFile
     */
    public ODMFactory(ServletContext servletContext, MultipartFile odmFile) {
        this.servletContext = servletContext;

        
        SchemaFactory sf;

        
        //First: Try to load the template ODM
        try{
            sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
            Schema schema = null;
            try {
                //First try with schema 1.3.1 - if fails, try with 1.3.2
                schema = sf.newSchema(new File(servletContext.getRealPath("/xsd") + "/odm1-3-1/ODM1-3-1.xsd"));  //TODO: Relative Path
                Unmarshaller unmarshaller = JAXBContext.newInstance(ODM.class).createUnmarshaller();
                unmarshaller.setSchema(schema);
                //importedODM = JAXB.unmarshal(odmFile.getInputStream(), ODM.class);
                this.odm = (ODM) unmarshaller.unmarshal(odmFile.getInputStream());
            } catch ( UnmarshalException e ) {
                schema = sf.newSchema(new File(servletContext.getRealPath("/xsd") + "/odm1-3-2/ODM1-3-2.xsd"));  //TODO: Relative Path
                Unmarshaller unmarshaller = JAXBContext.newInstance(ODM.class).createUnmarshaller();
                unmarshaller.setSchema(schema);
                //importedODM = JAXB.unmarshal(odmFile.getInputStream(), ODM.class);
                this.odm = (ODM) unmarshaller.unmarshal(odmFile.getInputStream());
            }
            
            logger.info("Unmarshalled ODM");
            logger.info("get in touch:", odm);
        } catch (Exception e) {
                logger.info("An error occured during importing of ODM: {}", e);
        }
        
        //Change the FileOID to a new unique file OID
        this.odm.setFileOID(OIDGenerator.getInstance().getOID());
        
        this.currentWorkingForm = this.getFirstFormDef();
        this.currentWorkingMDV = this.getMetaDataVersionForForm(this.currentWorkingForm);
    }
    
    /**
     * Adds an itemGroup(Def) to a form(Def). The Ref is automatically created.
     * 
     * @param itemGroupDef 
     * @param formDef if null, then the current working form is used
     * @return the inserted ItemGroupDef for easier further use
     */
    public ODMcomplexTypeDefinitionItemGroupDef addItemgroup (
            ODMcomplexTypeDefinitionItemGroupDef itemGroupDef, 
            ODMcomplexTypeDefinitionFormDef formDef) {
        if (formDef == null)
            formDef = this.currentWorkingForm;
        
        ODMcomplexTypeDefinitionItemGroupRef itemGroupRef = new ODMcomplexTypeDefinitionItemGroupRef();
        itemGroupRef.setMandatory(YesOrNo.YES);
        itemGroupRef.setItemGroupOID(itemGroupDef.getOID());
        
        this.currentWorkingMDV.getItemGroupDef().add(itemGroupDef);
        formDef.getItemGroupRef().add(itemGroupRef);

        return itemGroupDef;
    }
    
    /**
     * Adds an item(Def) to a itemGroup(Def). The Ref is automatically created.
     * @param itemDef
     * @param itemGroupDef
     * @return the itemDef added for easier further use
     */
    public ODMcomplexTypeDefinitionItemDef addItem (
            ODMcomplexTypeDefinitionItemDef itemDef,
            ODMcomplexTypeDefinitionItemGroupDef itemGroupDef) {
        
        ODMcomplexTypeDefinitionItemRef itemRef = new ODMcomplexTypeDefinitionItemRef();
        
        itemRef.setItemOID(itemDef.getOID());
        itemRef.setMandatory(YesOrNo.YES);
        
        this.currentWorkingMDV.getItemDef().add(itemDef);
        itemGroupDef.getItemRef().add(itemRef);
        
        return itemDef;
    }

    
    /**
     * Cleans a string to be used for a xml name
     * @param string string to be cleaned
     * @return cleaned string
     */
    private String cleanString (String string){
        string = string.replaceAll("[^\\w]", "");
        string = string.trim();
        return string;
    }
    
    /** 
     * Returns a FormDef, identified by the OID.
     * 
     * @param OID The OID of the formDef
     * @return null, if OID not found
     */
    private ODMcomplexTypeDefinitionFormDef getFormDef ( String OID ) {

        List<ODMcomplexTypeDefinitionStudy> studyList = this.odm.getStudy();
        for (ODMcomplexTypeDefinitionStudy study: studyList) {
            
            List<ODMcomplexTypeDefinitionMetaDataVersion> mdvList = study.getMetaDataVersion();
            for (ODMcomplexTypeDefinitionMetaDataVersion mdv: mdvList) {
                
                List<ODMcomplexTypeDefinitionFormDef> formDefList = mdv.getFormDef();
                for(ODMcomplexTypeDefinitionFormDef formDef: formDefList) {
                    if (formDef.getOID().equals(OID)) 
                        return formDef;
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * @return first FormDef in formular (first in the meaning of first referenced)
     */
    private ODMcomplexTypeDefinitionFormDef getFirstFormDef () {
        List<ODMcomplexTypeDefinitionStudy> studyList = this.odm.getStudy();
        for (ODMcomplexTypeDefinitionStudy study: studyList) {
            
            List<ODMcomplexTypeDefinitionMetaDataVersion> mdvList = study.getMetaDataVersion();
            for (ODMcomplexTypeDefinitionMetaDataVersion mdv: mdvList) {
                
                List<ODMcomplexTypeDefinitionFormDef> formDefList = mdv.getFormDef();
                for(ODMcomplexTypeDefinitionFormDef formDef: formDefList) {
                    return formDef;
                }
            }
        }
        return null;
    }
    
    /**
     * Creates a new ItemGroup (standard-conform)
     * @author Philipp Neuhaus <philipp.neuhaus@uni.muenster.de>
     * @param name The name of the ItemGroup
     * @return the newly creates ItemGroup
    */
    public ODMcomplexTypeDefinitionItemGroupDef createItemGroupDef(String name) {
        ODMcomplexTypeDefinitionItemGroupDef newIG = new ODMcomplexTypeDefinitionItemGroupDef();

        newIG.setName(cleanString(name));
        newIG.setOID(OIDGenerator.getInstance().getOID());
        newIG.setRepeating(YesOrNo.NO);
        
        /* If the name only contains non-latin characters, use a OID for it */
        if (newIG.getName().isEmpty()) 
            newIG.setName("DefaultItemgroup");
        
        return newIG;
    }
            
    /**
     * Creates a new ItemDef (standard-conform)
     * @param name the name of the item
     * @param withQuestion if true, a question with translated text (lang = en) and with the name as body will be created
     * @param dataType the dataType
     * @return the item created
     */
    public ODMcomplexTypeDefinitionItemDef createItemDef(String name, boolean withQuestion, DataType dataType) {
        ODMcomplexTypeDefinitionItemDef itemDef = 
                new ODMcomplexTypeDefinitionItemDef();
        
        itemDef.setOID(OIDGenerator.getInstance().getOID());
        itemDef.setDataType(dataType);
        itemDef.setName(cleanString(name));
        
        /* If the name only contains non-latin characters, use a OID for it */
        if (itemDef.getName().isEmpty()) 
            itemDef.setName(OIDGenerator.getInstance().getOID());
        
        if (withQuestion == true) {
            itemDef.setQuestion(this.createQuestion(name));
        }
        
        return itemDef;
    }
    
     /**
     * Creates a new ItemDef (standard-conform), DataType TEXT
     * @param name the name of the item
     * @param withQuestion if true, a question with translated text (lang = en) and with the name as body will be created
     * @return the item created
     */
    public ODMcomplexTypeDefinitionItemDef createItemDef(String name, boolean withQuestion) {
        return createItemDef(name, withQuestion, DataType.TEXT);
    }

    
    /**
    Just loads an empty Template as ODM.
    @author Philipp Neuhaus <Philipp.Neuhaus@uni-muenster.de>
    */
    private void loadTemplate(){
        SchemaFactory sf;

        
        //First: Try to load the template ODM
        try{
            sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
            this.schema = sf.newSchema(new File(servletContext.getRealPath("/xsd") + "/odm1-3-2/ODM1-3-2.xsd"));  //TODO: Relative Path
            Unmarshaller unmarshaller = JAXBContext.newInstance(ODM.class).createUnmarshaller();
            unmarshaller.setSchema(schema);

            
            this.odm = (ODM) unmarshaller.unmarshal(new File(servletContext.getRealPath("/template") + "/empty.odm.xml"));
            logger.info("Unmarshalled ODM");
            logger.info("get in touch:", odm);
        } catch (Exception e) {
                logger.info("An error occured during importing of ODM: {}", e);
        }
    }

    /**
     * Return the MetaDataVersion containing a given FormDef
     * @param givenFormDef
     * @return the MetaDataVersion containing a given FormDef
     */
    private ODMcomplexTypeDefinitionMetaDataVersion getMetaDataVersionForForm(
            ODMcomplexTypeDefinitionFormDef givenFormDef) {
        List<ODMcomplexTypeDefinitionStudy> studyList = this.odm.getStudy();
        for (ODMcomplexTypeDefinitionStudy study: studyList) {
            
            List<ODMcomplexTypeDefinitionMetaDataVersion> mdvList = study.getMetaDataVersion();
            for (ODMcomplexTypeDefinitionMetaDataVersion mdv: mdvList) {
                
                List<ODMcomplexTypeDefinitionFormDef> formDefList = mdv.getFormDef();
                for(ODMcomplexTypeDefinitionFormDef formDef: formDefList) {
                    if (formDef == givenFormDef) 
                        return mdv;
                }
            }
        }
        return null;
    }
    
    public void feedOutput (ServletOutputStream outputStream) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance( this.odm.getClass().getPackage().getName() );
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setSchema( this.schema );
            marshaller.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.marshal( this.odm, outputStream );
        } catch (Exception ex) {
            Logger.getLogger(ODMFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ODMcomplexTypeDefinitionQuestion createQuestion (String question){
        ODMcomplexTypeDefinitionTranslatedText translatedText = 
                new ODMcomplexTypeDefinitionTranslatedText();
        translatedText.setLang("en");
        translatedText.setValue(question);
        
        ODMcomplexTypeDefinitionQuestion odmQuestion = 
                new ODMcomplexTypeDefinitionQuestion();
        odmQuestion.getTranslatedText().add(translatedText);
        
        return odmQuestion;  
    }
    

}
