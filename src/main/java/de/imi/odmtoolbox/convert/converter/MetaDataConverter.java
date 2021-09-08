package de.imi.odmtoolbox.convert.converter;

import de.imi.odmtoolbox.convert.enums.ConvertMethodEnumMarker;
import de.imi.odmtoolbox.convert.enums.ConverterMethods;
import de.imi.odmtoolbox.convert.enums.ItemDefCategories;
import de.imi.odmtoolbox.convert.enums.MultiAnswerItemOptions;
import de.imi.odmtoolbox.convert.helper.lambda.LambdaHelper;
import de.imi.odmtoolbox.convert.helper.lambda.OptionalConsumer;
import de.imi.odmtoolbox.convert.helper.xml.XmlUtil;
import de.imi.odmtoolbox.convert.model.ConverterModelAttribute;
import de.imi.odmtoolbox.convert.model.ElementWithID;
import de.imi.odmtoolbox.convert.model.MultipleAnswerHelperObject;
import de.imi.odmtoolbox.convert.model.XMLElementInfo;
import de.imi.odmtoolbox.convert.websocket.PushMessageService;
import de.imi.odmtoolbox.library.ConversionNotes;
import de.imi.odmtoolbox.library.OIDGenerator;
import lombok.NonNull;
import lombok.Setter;
import org.w3c.dom.*;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.imi.odmtoolbox.convert.enums.ConverterMethods.MetaData.*;

public class MetaDataConverter extends AbstractConverter{

    @Setter
    private static class ShareObject {
        private int totalNumberOfTags;
        private Document importedODM;
        private ConversionNotes conversionNotes;
        private String language;
        private String studyEventName;
        private NodeList translateTags;
        private Map<String, List<Element>> itemDefs;
        private Element metaDataVersion;
        private NodeList repeatingInstruments;
        private NodeList codeLists;
        private NodeList itemRefs;
        private RedcapFileWrapper redcapFileWrapper;
        private Element globalVariables;
        private ConverterModelAttribute converterModelAttribute;
        private Map<String, Element> itemRefMap;
        private Map<String, Element> igRefMap;
        private NodeList itemGroupDefs;
    }

    private final List<ConverterMethods.MetaData> converterMethods;
    private ShareObject shareObject;

    public MetaDataConverter(Document importedODM, ConversionNotes conversionNotes, ConverterModelAttribute converterModelAttribute,
                             @NonNull PushMessageService messageService, RedcapFileWrapper redcapFile)
            throws SAXParseException {
        super(importedODM, conversionNotes, messageService);
        this.createShareObject(converterModelAttribute);
        this.converterMethods = new ArrayList<>();
        this.shareObject.redcapFileWrapper = redcapFile;
    }

    @Override
    protected Map<ConvertMethodEnumMarker, String> createConverterMethodToMethodNameMap() {
        Map<ConvertMethodEnumMarker, String> map = new HashMap<>();
        map.put(FIELD_NOTES_TO_COMMENTS, "convertFieldNotesToComments");
        map.put(CONVERT_REPEATING_INSTRUMENTS, "addRepeatingInstrumentsInformation");
        map.put(CONVERT_MULTIPLE_ITEMS, "replaceMultipleAnswerItems");
        map.put(REMOVE_REDUNDANT_REDCAT_ATTRIBUTES, "removeRedundantRedcapVariables");
        map.put(CLEAN_REDCAP_TAGS, "cleanRemainingRedcapTags");
        map.put(ADD_LANGUAGE_INFORMATION, "addLanguageInformationToTranslatedTexts");
        map.put(ADD_PROTOCOL, "addProtocolInformation");
        map.put(CONVERT_REDCAP_CALCULATIONS, "transformCalculations");
        map.put(CONVERT_REDCAP_LOGIC_BRANCHES, "transformLogicBranches");
        map.put(BOOLEAN_TO_INTEGER, "replaceBooleanByInteger");
        map.put(STORE_REDCAP_DATA_IN_FILE, "createSeparateFileWithRedcapInformation");
        map.put(COMPLETE_ITEM_NAMES, "addNamesToEmptyItemsAndIGs");
        return map;
    }

    @Override
    public void convert() {
        double estimatedTime = this.calculateEstimateTime();
        System.out.println("Estimated Time: " + estimatedTime);
        messageService.addMessage("Conversion started");
        messageService.addMessage("Estimated Time: < " + Math.floor(estimatedTime/1000 + 1) + "s");
        messageService.sendMessages();
        converterMethods.forEach(LambdaHelper.throwingConsumerWrapper(this::executeMethod));
    }

    public void setMethods(List<ConverterMethods.MetaData> methods) {
        this.converterMethods.addAll(this.getMandatoryMethodsBefore());
        this.converterMethods.addAll(methods);
        this.converterMethods.addAll(this.getMandatoryMethodsAfter());
    }

    private List<ConverterMethods.MetaData> getMandatoryMethodsBefore() {
        return Arrays.asList(FIELD_NOTES_TO_COMMENTS, CONVERT_REPEATING_INSTRUMENTS, CONVERT_MULTIPLE_ITEMS);
    }

    private List<ConverterMethods.MetaData> getMandatoryMethodsAfter() {
        return Arrays.asList(REMOVE_REDUNDANT_REDCAT_ATTRIBUTES, CLEAN_REDCAP_TAGS);
    }

    private void createShareObject(ConverterModelAttribute converterModelAttribute) throws SAXParseException {
        this.shareObject = new ShareObject();
        String language = converterModelAttribute.getLanguage();
        if(language == null || language.equals("")) {
            language = "en";
        }

        Element metaDataVersion = (Element) XmlUtil.asList(this.importedODM.getElementsByTagName("MetaDataVersion")).stream().findAny().orElse(null);
        if(metaDataVersion == null) {
            throw (new SAXParseException("Element with tag name 'MetaDataVersion' not readable", null, null, -1, -1));
        }

        this.shareObject.setImportedODM(this.importedODM);
        this.shareObject.setConversionNotes(this.conversionNotes);
        this.shareObject.setLanguage(language);
        this.shareObject.setItemDefs(this.getItemDefsToTransform(metaDataVersion));
        this.shareObject.setTotalNumberOfTags(this.importedODM.getElementsByTagName("*").getLength());
        this.shareObject.setMetaDataVersion(metaDataVersion);
        this.shareObject.setGlobalVariables((Element) XmlUtil.asList(this.importedODM.getElementsByTagName("GlobalVariables")).stream().findAny().orElse(null));
        this.shareObject.setTranslateTags(metaDataVersion.getElementsByTagName("TranslatedText"));
        this.shareObject.setRepeatingInstruments(this.importedODM.getElementsByTagName("redcap:RepeatingInstrument"));
        this.shareObject.setCodeLists(metaDataVersion.getElementsByTagName("CodeList"));
        this.shareObject.setItemRefs(metaDataVersion.getElementsByTagName("ItemRef"));
        this.shareObject.setStudyEventName(converterModelAttribute.getStudyEventName());
        this.shareObject.setConverterModelAttribute(converterModelAttribute);
        this.shareObject.setItemGroupDefs(metaDataVersion.getElementsByTagName("ItemGroupDef"));
        this.shareObject.itemRefMap = new HashMap<>();
        XmlUtil.asElementList(this.shareObject.itemRefs).forEach(i-> this.shareObject.itemRefMap.put(i.getAttribute("ItemOID"), i));
        if(MultiAnswerItemOptions.ofKey(converterModelAttribute.getMultiAnswerStyle()) == MultiAnswerItemOptions.ADD_ITEM_GROUP) {
            this.shareObject.igRefMap = new HashMap<>();
            XmlUtil.asElementList(metaDataVersion.getElementsByTagName("ItemGroupRef"))
                    .forEach(ig->this.shareObject.igRefMap.put(ig.getAttribute("ItemGroupOID"), ig));
        }
    }

    private Map<String, List<Element>> getItemDefsToTransform(Element metaDataVersion) {
        long start = System.currentTimeMillis();
        Map<String, List<Element>> map = new HashMap<>();
        map.put(ItemDefCategories.FIELD_NOTE.getAsString(), new ArrayList<>());
        map.put(ItemDefCategories.CHECKBOX.getAsString(), new ArrayList<>());
        map.put(ItemDefCategories.CALCULATION.getAsString(), new ArrayList<>());
        map.put(ItemDefCategories.BRANCHING_LOGIC.getAsString(), new ArrayList<>());
        map.put(ItemDefCategories.ALL.getAsString(), new ArrayList<>());


        XmlUtil.asElementList(metaDataVersion.getElementsByTagName("ItemDef")).forEach(i->{
            map.get(ItemDefCategories.ALL.getAsString()).add(i);
            if(!i.getAttribute(ItemDefCategories.FIELD_NOTE.getAsString()).equals("")) {
                map.get(ItemDefCategories.FIELD_NOTE.getAsString()).add(i);
            }
            if(i.getAttribute(ItemDefCategories.FIELD_TYPE.getAsString()).equals(ItemDefCategories.CHECKBOX.getAsString())) {
                map.get(ItemDefCategories.CHECKBOX.getAsString()).add(i);
            }
            if(!i.getAttribute(ItemDefCategories.CALCULATION.getAsString()).equals("")) {
                map.get(ItemDefCategories.CALCULATION.getAsString()).add(i);
            }
            if(!i.getAttribute(ItemDefCategories.BRANCHING_LOGIC.getAsString()).equals("")) {
                map.get(ItemDefCategories.BRANCHING_LOGIC.getAsString()).add(i);
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("creating itemdef maps");
        System.out.println(end-start);
        return map;
    }

    private double calculateEstimateTime() {
        int length = this.shareObject.totalNumberOfTags;
        NodeList translateTags = shareObject.translateTags;
        Map<String, List<Element>> itemDefs = this.shareObject.itemDefs;
        NodeList itemRefs = shareObject.itemRefs;
        NodeList codeLists = shareObject.codeLists;

        double estimatedTime = length*0.02 + //creating itemDef maps
                translateTags.getLength()*0.35 + //missing Translations
                (itemDefs.get(ItemDefCategories.CHECKBOX.getAsString()).size() + codeLists.getLength())*0.1 + //MultianswerItems
                Math.pow(itemRefs.getLength(), 2)*0.0004*2 + //Creating maps for Calculation and BranchingLogic
                length*0.015 + //boolean items
                length*0.1 + //redundant variables
                100 + //remaining Stuff to convert and a bit of a buffer
                length*0.25 + 5; //creating file
        return estimatedTime * SYSTEM_FACTOR;
    }

    /*****************************************************************************************************************************************
     *************************************************************CONVERTER METHODS***********************************************************
     *****************************************************************************************************************************************/

    private void convertFieldNotesToComments() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Converting redcap:FieldNotes to comment attributes...");
        messageService.sendMessages();

        List<Element> itemDefs = this.shareObject.itemDefs.get(ItemDefCategories.FIELD_NOTE.getAsString());
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        //Converts redcap:FieldNote to Comment
        itemDefs.forEach(e-> {
            e.setAttribute("Comment", e.getAttribute("redcap:FieldNote"));
            e.removeAttribute("redcap:FieldNote");
        });
        conversionNotes.addNote("redcap:FieldNote", "Converted " + itemDefs.size() + " redcap:FieldNote attributes to comments",
                ConversionNotes.SeverenessLevel.NOTICE);

        long end = System.currentTimeMillis();
        System.out.println("convertFieldNotesToComments");
        System.out.println(end-start);
        System.out.println("Number of Items: " + itemDefs.size());
    }

    private void addRepeatingInstrumentsInformation() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Converting redcap:RepeatingInstruments to repeating attributes...");
        messageService.sendMessages();

        Element metaDataversion = this.shareObject.metaDataVersion;
        NodeList repeatingInstruments = this.shareObject.repeatingInstruments;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        Element globalVariables = this.shareObject.globalVariables;

        //adds the Repeating="Yes" attribute to all FormDefs, that are RepeatingInstruments in Redcap
        NodeList formDefs = metaDataversion.getElementsByTagName("FormDef");
        for(Element repeatingInstrument : XmlUtil.asElementList(repeatingInstruments)) {
            String formName = repeatingInstrument.getAttribute("redcap:RepeatInstrument");
            Optional<Element> formDef = XmlUtil.asElementList(formDefs).stream()
                    .filter(n->n.getAttribute("redcap:FormName").equals(formName))
                    .findAny();

            if (formDef.isPresent()) {
                Element formElement = formDef.get();
                formElement.setAttribute("Repeating", "Yes");
                conversionNotes.addNote(getElementIdentifierString(formElement, "OID"),
                        "Set Repeating to 'Yes', since it is part of <redcap:RepeatingInstrumentsAndEvents> tag", ConversionNotes.SeverenessLevel.NOTICE);
            }
        }
        long numberOfItems = repeatingInstruments.getLength();
        if(globalVariables != null) {
            NodeList nl2 = globalVariables.getElementsByTagName("redcap:RepeatingInstrumentsAndEvents");
            if(nl2.getLength() > 0) {
                globalVariables.removeChild(nl2.item(0));
                conversionNotes.addNote("Repeating Instruments",
                        "Removed 'redcap:RepeatingInstrumentsAndEvents' since information was added to FormDefs", ConversionNotes.SeverenessLevel.NOTICE);

            }
        }
        long end = System.currentTimeMillis();
        System.out.println("addRepeatingInstrumentsInformation");
        System.out.println(end-start);
        System.out.println("Number of Items: " + numberOfItems);
    }

    private void replaceMultipleAnswerItems() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Converting MultipleAnswerItems to standard odm format...");
        messageService.sendMessages();

        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        List<Element> itemDefs = this.shareObject.itemDefs.get(ItemDefCategories.CHECKBOX.getAsString());
        NodeList codeLists = this.shareObject.codeLists;

        Map<String, MultipleAnswerHelperObject> items = new HashMap<>();
        itemDefs.forEach(n-> OptionalConsumer.of(Optional.ofNullable(items.get(n.getAttribute("OID").split("___")[0])))
                .ifPresent(i->i.getItemDefs().add(n))
                .ifNotPresent(()->{
                    String oid_base = n.getAttribute("OID").split("___")[0];
                    items.put(oid_base, new MultipleAnswerHelperObject());
                    items.get(oid_base).setBaseName(oid_base);
                    items.get(oid_base).getItemDefs().add(n);
                }));
        items.forEach(LambdaHelper.throwingBiConsumerWrapper((k, v) -> addCodeListsAndValuesToMultiAnswerItem(v, codeLists)));
        items.values().forEach(i->transformMultipleAnswerNodes(conversionNotes, i));
        long end = System.currentTimeMillis();
        System.out.println("replaceMultipleAnswerItems");
        System.out.println(end-start);
        System.out.println("Number of Items: " + itemDefs.size());
    }

    private void addCodeListsAndValuesToMultiAnswerItem(MultipleAnswerHelperObject item, NodeList codeLists) {
        final boolean[] alreadyAdded = {false};
        item.getItemDefs().forEach(LambdaHelper.throwingConsumerWrapper(i->{
            Optional<Node> codeListRef = XmlUtil.asList(i.getElementsByTagName("CodeListRef")).stream().findAny();

            if(codeListRef.isPresent()) {
                Optional<Node> codeList = XmlUtil.asList(codeLists).stream()
                        .filter(c->((Element)c).getAttribute("OID").equals(((Element)codeListRef.get()).getAttribute("CodeListOID")))
                        .findAny();

                codeList.ifPresent(c->item.getCodeLists().add((Element)c));
                if(!alreadyAdded[0]) {
                    String possibleValues = codeList.map(c->((Element)c).getAttribute("redcap:CheckboxChoices")).orElse("");
                    if(!possibleValues.equals("")) {
                        Arrays.asList(possibleValues.split("\\|"))
                                .forEach(s->item.getCodeListValues().put(s.split(",")[0].trim(), s.split(",")[1].trim()));
                    }
                    alreadyAdded[0] = true;
                }
            }
        }));
    }

    private void transformMultipleAnswerNodes(ConversionNotes conversionNotes, MultipleAnswerHelperObject item) {

        MultiAnswerItemOptions multipleAnswerStyle = Optional.ofNullable(MultiAnswerItemOptions.ofKey(this.shareObject.converterModelAttribute.getMultiAnswerStyle()))
                .orElse(MultiAnswerItemOptions.REPEAT_QUESTION);
        switch (multipleAnswerStyle) {
            case ADD_ITEM_GROUP:
                addItemGroupForMultipleAnswerItem(item);
            case QUESTION_ONLY_FIRST_ITEM:
            case REPEAT_QUESTION:
                item.getItemDefs().forEach(id->transFormMultipleAnswerItemDef(conversionNotes, id, item, multipleAnswerStyle));
                break;
        }

        item.getCodeLists().forEach(c->transFormMultipleAnswerCodeList(conversionNotes,c,item));
    }

    private void addItemGroupForMultipleAnswerItem(MultipleAnswerHelperObject helperItem){
        Document importedODM = this.importedODM;
        Map<String, Element> itemRefMap = this.shareObject.itemRefMap;
        Map<String, Element> igRefMap = this.shareObject.igRefMap;

        String newOID = OIDGenerator.getInstance().getOID();
        Element firstItemDef = helperItem.getItemDefs().get(0);
        try{
            String mandatory = itemRefMap.get(firstItemDef.getAttribute("OID")).getAttribute("Mandatory");
            if(mandatory.equals("")) {
                mandatory = "No";
            }

            Element parentIGDef = (Element) itemRefMap.get(firstItemDef.getAttribute("OID")).getParentNode();

            Element newIGDef = parentIGDef;

            //if this is false, the original IGDef already only contains the ItemDefs to be added to a new one -> we can just rework it
            if(parentIGDef.getElementsByTagName("ItemRef").getLength() != helperItem.getItemDefs().size()) {
                Element itemGroupRef = importedODM.createElement("ItemGroupRef");
                itemGroupRef.setAttribute("ItemGroupOID", newOID);
                itemGroupRef.setAttribute("Mandatory", mandatory);
                Element igRef = igRefMap.get(parentIGDef.getAttribute("OID"));
                igRef.getParentNode().insertBefore(itemGroupRef, igRef);

                Element itemGroupDef = importedODM.createElement("ItemGroupDef");
                itemGroupDef.setAttribute("Repeating", "No");
                itemGroupDef.setAttribute("OID", newOID);
                itemGroupDef.setAttribute("Name", helperItem.getBaseName() + "_head_ig");
                parentIGDef.getParentNode().insertBefore(itemGroupDef, parentIGDef);
                //parentIGDef.removeChild(ir);
                helperItem.getItemDefs().stream().map(id->itemRefMap.get(id.getAttribute("OID"))).forEach(itemGroupDef::appendChild);
                newIGDef = itemGroupDef;
            }
            if(!XmlUtil.asElementList(newIGDef.getElementsByTagName("Description")).stream().findAny().isPresent()) {
                Optional<List<Element>> translatedTexts = XmlUtil.asElementList(firstItemDef.getElementsByTagName("Question")).stream()
                        .findAny()
                        .map(q->XmlUtil.asElementList(q.getElementsByTagName("TranslatedText")));

                XMLElementInfo xmlElementInfo = new XMLElementInfo();

                ElementWithID igDefElementWithId = new ElementWithID(0, newIGDef.getNodeName());

                AtomicInteger internID = new AtomicInteger();
                ElementWithID description = new ElementWithID(internID.getAndIncrement(), "Description");
                xmlElementInfo.getParentChildElements().put(igDefElementWithId, Collections.singletonList(description));
                List<ElementWithID> translateTextsWithID = new ArrayList<>();
                translatedTexts.ifPresent(l->l.forEach(t->{
                    ElementWithID text = new ElementWithID(internID.getAndIncrement(), "TranslatedText");
                    translateTextsWithID.add(text);
                    String language = t.getAttribute("xml:lang");
                    if(!language.equals("")) {
                        xmlElementInfo.getElementAttributes().put(text, new HashMap<String, String>() {
                            {
                                put("xml:lang", language);
                            }
                        });
                    }
                    xmlElementInfo.getElementTexts().put(text,t.getTextContent());

                }));
               xmlElementInfo.getParentChildElements().put(description, translateTextsWithID);
               this.createXMLTree(importedODM, newIGDef, igDefElementWithId, xmlElementInfo);
            }
        } catch(Exception ex) {
            conversionNotes.addNote("Multiansweritem(" + firstItemDef.getAttribute("OID") + ")",
                    "Could not create itemgroup for MultipleAnswer-ItemDef. Consider chosing another mode for handling MultipleAnswerItems",
                    ConversionNotes.SeverenessLevel.CRITICAL);
        }

    }

    private void transFormMultipleAnswerItemDef(ConversionNotes conversionNotes, Element itemDef, MultipleAnswerHelperObject multipleAnswerItem,
                                                MultiAnswerItemOptions multipleAnswerStyle) {

        String numberInCodeList = itemDef.getAttribute("OID").split("___")[1];
        String value = multipleAnswerItem.getCodeListValues().get(numberInCodeList);
        itemDef.setAttribute("Name", multipleAnswerItem.getBaseName() + "_" + value);
        conversionNotes.addNote(getElementIdentifierString(itemDef, "OID"),
                "Changed name from " + itemDef.getAttribute("OID") + " to " + itemDef.getAttribute("Name"),
                ConversionNotes.SeverenessLevel.NOTICE);
        Element question = XmlUtil.asElementList(itemDef.getElementsByTagName("Question")).stream().findAny().orElse(null);

        if(question == null) {
            conversionNotes.addNote(getElementIdentifierString(itemDef, "OID"),
                    "Could not find any Question tag for given itemDef",
                    ConversionNotes.SeverenessLevel.CRITICAL);
            return;
        }

        switch (multipleAnswerStyle) {
            case ADD_ITEM_GROUP:
                replaceTranslatedTextByCodeListValue(question,value,itemDef);
                break;
            case QUESTION_ONLY_FIRST_ITEM:
                if(numberInCodeList.equals("1")) {
                    appendCodeListValueToTranslatedText(question,value,itemDef);
                }
                else {
                    replaceTranslatedTextByCodeListValue(question,value,itemDef);
                }
                break;
            case REPEAT_QUESTION:
               appendCodeListValueToTranslatedText(question,value,itemDef);
               break;
        }
    }

    private void appendCodeListValueToTranslatedText(Element question, String value, Element itemDef) {
        NodeList translatedTexts = question.getElementsByTagName("TranslatedText");
        XmlUtil.asList(translatedTexts).forEach(t->t.setTextContent(t.getTextContent() + " " + value));
        conversionNotes.addNote(getElementIdentifierString(itemDef, "OID"),
                "Added choice name " + value + " to " + translatedTexts.getLength() + " TranslatedText items",
                ConversionNotes.SeverenessLevel.NOTICE);
    }

    private void replaceTranslatedTextByCodeListValue(Element question, String value, Element itemDef) {
        NodeList translatedTexts = question.getElementsByTagName("TranslatedText");
        XmlUtil.asList(translatedTexts).forEach(t->t.setTextContent(value));
        conversionNotes.addNote(getElementIdentifierString(itemDef, "OID"),
                "Replaced itemtext by choice name " + value + " for " + translatedTexts.getLength() + " TranslatedText items",
                ConversionNotes.SeverenessLevel.NOTICE);
    }

    private void transFormMultipleAnswerCodeList(ConversionNotes conversionNotes, Element codeList, MultipleAnswerHelperObject multipleAnswerItem) {
        String numberInCodeList = codeList.getAttribute("OID").split("___")[1].split("\\.")[0];
        String value = multipleAnswerItem.getCodeListValues().get(numberInCodeList);
        codeList.setAttribute("Name", multipleAnswerItem.getBaseName() + "_ " + value);
        conversionNotes.addNote(getElementIdentifierString(codeList, "OID"),
                "Changed name from " + codeList.getAttribute("OID").split("\\.")[0] + " to " + codeList.getAttribute("Name"),
                ConversionNotes.SeverenessLevel.NOTICE);
        codeList.removeAttribute("redcap:Variable");
        codeList.removeAttribute("redcap:CheckboxChoices");
        conversionNotes.addNote(getElementIdentifierString(codeList, "OID"),
                "removed redcap variables 'redcap:Variable' and 'redcap:CheckboxChoices'",
                ConversionNotes.SeverenessLevel.NOTICE);
        NodeList codeListItems = codeList.getElementsByTagName("CodeListItem");
        if(codeListItems.getLength() > 0) {
            XmlUtil.asElementList(codeListItems).forEach(i->XmlUtil.asList(i.getElementsByTagName("TranslatedText"))
                    .forEach(t->t.setTextContent(i.getAttribute("CodedValue").equals("1") ? "Yes" : "No")));
            conversionNotes.addNote(getElementIdentifierString(codeList, "OID"),
                    "Changed CodeListItem values to 'Yes' and 'No' respectively",
                    ConversionNotes.SeverenessLevel.NOTICE);
        }
    }

    private void removeRedundantRedcapVariables() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Removing redcap attributes...");
        messageService.sendMessages();

        Element metaDataVersion = this.shareObject.metaDataVersion;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        List<Element> itemDefs = this.shareObject.itemDefs.get(ItemDefCategories.ALL.getAsString());
        NodeList itemRefs = this.shareObject.itemRefs;
        NodeList codeLists = this.shareObject.codeLists;

        List<String> attributesToRemove = new ArrayList<>();
        attributesToRemove.add("redcap:Variable");
        attributesToRemove.add("redcap:FieldType");
        attributesToRemove.add("redcap:SectionHeader");
        attributesToRemove.add("redcap:TextValidationType");
        attributesToRemove.add("redcap:FieldAnnotation");
        attributesToRemove.add("redcap:RequiredField");
        attributesToRemove.add("redcap:MatrixGroupName");
        attributesToRemove.add("redcap:Calculation");
        attributesToRemove.add("redcap:BranchingLogic");
        attributesToRemove.add("redcap:CustomAlignment");

        itemDefs.forEach(i->attributesToRemove.forEach(i::removeAttribute));
        conversionNotes.addNote("redcap attributes", "Removed the following redcap attributes from " + itemDefs.size() + " ItemDefs: " +
                        String.join(", ", attributesToRemove),
                ConversionNotes.SeverenessLevel.NOTICE);

        XmlUtil.asElementList(itemRefs).forEach(i-> i.removeAttribute("redcap:Variable"));
        conversionNotes.addNote("redcap attributes", "Removed redundant redcap attributes from " + itemRefs.getLength() + " ItemRefs",
                ConversionNotes.SeverenessLevel.NOTICE);

        NodeList formDefs = metaDataVersion.getElementsByTagName("FormDef");
        XmlUtil.asElementList(formDefs).forEach(f->f.removeAttribute("redcap:FormName"));
        conversionNotes.addNote("redcap attributes", "Removed 'redcap:FormName' attribute from " + formDefs.getLength() + " FormDefs",
                ConversionNotes.SeverenessLevel.NOTICE);

        XmlUtil.asElementList(codeLists).forEach(c->c.removeAttribute("redcap:Variable"));
        conversionNotes.addNote("redcap attributes", "Removed 'redcap:Variable' attribute from " + codeLists.getLength() + " CodeLists",
                ConversionNotes.SeverenessLevel.NOTICE);

        metaDataVersion.removeAttribute("redcap:RecordIdField");
        conversionNotes.addNote("redcap attributes", "Removed 'redcap:RecordIdField' attribute from MetaDataVersion",
                ConversionNotes.SeverenessLevel.NOTICE);

        NamedNodeMap attributes = this.importedODM.getFirstChild().getAttributes();
        for(int i = attributes.getLength() - 1; i >= 0; --i) {
            if(attributes.item(i).getNodeName().contains("redcap") || attributes.item(i).getNodeValue().equals("REDCap")) {
                conversionNotes.addNote("redcap attribute", "Removed " + attributes.item(i).getNodeName() + " attribute from ODM tag",
                        ConversionNotes.SeverenessLevel.NOTICE);
                attributes.removeNamedItem(attributes.item(i).getNodeName());
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("removeRedundantRedcapVariables");
        System.out.println(end-start);
        System.out.println("Number of Items: " + itemDefs.size());
    }

    private void cleanRemainingRedcapTags() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Cleaning remaining redcap tags");
        messageService.sendMessages();

        Element metaDataVersion = this.shareObject.metaDataVersion;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        NodeList formattedTexts = metaDataVersion.getElementsByTagName("redcap:FormattedTranslatedText");
        Element globalVariables = this.shareObject.globalVariables;
        int length = formattedTexts.getLength();
        for(int i = length - 1; i >= 0; i--) {
            formattedTexts.item(i).getParentNode().removeChild(formattedTexts.item(i));
        }
        conversionNotes.addNote("FormattedTranslatedText", "Removed " + length + " FormattedTranslatedText tags",
                ConversionNotes.SeverenessLevel.NOTICE);

        if(globalVariables != null) {
            NodeList childNodes = globalVariables.getChildNodes();
            length = childNodes.getLength();

            for(int j = length - 1; j >= 0; j--) {
                if(childNodes.item(j).getNodeName().startsWith("redcap")) {
                    conversionNotes.addNote("Redcap tags", "Removed tag " + childNodes.item(j).getNodeName() + " from odm file",
                            ConversionNotes.SeverenessLevel.NOTICE);
                    globalVariables.removeChild(childNodes.item(j));
                }
            }
        }

        NodeList attachments = metaDataVersion.getElementsByTagName("redcap:Attachment");
        int length2 = attachments.getLength();
        for(int i = length2 - 1; i >= 0; i--) {
            attachments.item(i).getParentNode().removeChild(attachments.item(i));
        }
        conversionNotes.addNote("Attachments", "Removed " + length2 + " redcap:Attachment tags",
                ConversionNotes.SeverenessLevel.NOTICE);

        long end = System.currentTimeMillis();
        System.out.println("cleanRemainingRedcapTags");
        System.out.println(end-start);
        System.out.println("Number of Items: " + length);
    }

    private void addLanguageInformationToTranslatedTexts() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Adding language information to TranslatedText tags...");
        messageService.sendMessages();

        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        String language = this.shareObject.language;
        NodeList translateTags = this.shareObject.translateTags;

        if(translateTags == null || language == null) return;
        XmlUtil.asElementList(translateTags).forEach(e->e.setAttribute("xml:lang", language));
        conversionNotes.addNote("TranslatedText",
                "Added language information to " + translateTags.getLength() + " <TranslatedText> tags", ConversionNotes.SeverenessLevel.NOTICE);

        long end = System.currentTimeMillis();
        System.out.println("addLanguageInformationToTranslatedTexts");
        System.out.println(end-start);
        System.out.println("Number of Items: " + translateTags.getLength());
    }

    private void addProtocolInformation() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Adding Protocol information...");
        messageService.sendMessages();

        Document importedODM = this.shareObject.importedODM;
        Element metaDataVersion = this.shareObject.metaDataVersion;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        String name = this.shareObject.studyEventName;

        if(name == null || name.equals("")) {
            name = ((Element) metaDataVersion.getElementsByTagName("FormDef").item(0)).getAttribute("Name");
        }
        Element protocol = importedODM.createElement("Protocol");
        Element studyEventRef = importedODM.createElement("StudyEventRef");
        String id = OIDGenerator.getInstance().getOID();
        studyEventRef.setAttribute("OrderNumber", "1");
        studyEventRef.setAttribute("Mandatory", "Yes");
        studyEventRef.setAttribute("StudyEventOID", id);
        protocol.appendChild(studyEventRef);
        Element studyEventDef = importedODM.createElement("StudyEventDef");
        studyEventDef.setAttribute("OID", id);
        studyEventDef.setAttribute("Name", name);
        studyEventDef.setAttribute("Repeating", "No");
        studyEventDef.setAttribute("Type", "Common");

        NodeList formDefs = importedODM.getElementsByTagName("FormDef");
        for(int i = 0; i < formDefs.getLength(); ++i) {
            Element formRef = importedODM.createElement("FormRef");
            formRef.setAttribute("OrderNumber", Integer.toString(i+1));
            formRef.setAttribute("Mandatory", "No");
            formRef.setAttribute("FormOID", ((Element) formDefs.item(i)).getAttribute("OID"));
            studyEventDef.appendChild(formRef);
        }

        metaDataVersion.insertBefore(studyEventDef, metaDataVersion.getFirstChild());
        metaDataVersion.insertBefore(protocol, metaDataVersion.getFirstChild());
        conversionNotes.addNote("Protocol", "Added Protocol Node to MetaDataVersion Node", ConversionNotes.SeverenessLevel.NOTICE);
        conversionNotes.addNote("StudyEventDef", "Added StudyEventDef to MetaDataVersion Node", ConversionNotes.SeverenessLevel.NOTICE);
        conversionNotes.addNote("FormRef", "Referenced all Forms in StudyEventDef", ConversionNotes.SeverenessLevel.NOTICE);

        long end = System.currentTimeMillis();
        System.out.println("addProtocolInformation");
        System.out.println(end-start);
        System.out.println("Number of Items: " + 1);
    }

    private void transformCalculations() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Converting redcap:Calculations to MethodDefs...");
        messageService.sendMessages();

        Document importedODM = this.shareObject.importedODM;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        String language = shareObject.language;
        List<Element> itemDefs = this.shareObject.itemDefs.get(ItemDefCategories.CALCULATION.getAsString());
        Element metaDataVersion = this.shareObject.metaDataVersion;

        Map<String, Element> itemRefMap = this.shareObject.itemRefMap;

        itemDefs.forEach(LambdaHelper.throwingConsumerWrapper(i->addMethodNode(importedODM, conversionNotes, i, language, itemRefMap, metaDataVersion)));
        long end = System.currentTimeMillis();
        System.out.println("transformCalculations");
        System.out.println(end-start);
        System.out.println("Number of Items: " + itemDefs.size());
    }

    private void addMethodNode(Document importedODM, ConversionNotes conversionNotes, Element itemDef, String language,
                               Map<String, Element> itemRefs,Element metaDataVersion) {
        XMLElementInfo xmlElementInfo = new XMLElementInfo();

        Element methodDef = importedODM.createElement("MethodDef");
        String methodDefOID = OIDGenerator.getInstance().getOID();
        methodDef.setAttribute("OID", methodDefOID);
        methodDef.setAttribute("Name", itemDef.getAttribute("Name"));
        methodDef.setAttribute("Type", "Computation");
        ElementWithID methodDefElementWithId = new ElementWithID(0, methodDef.getNodeName());

        int internID = 0;
        ElementWithID description = new ElementWithID(internID++, "Description");
        ElementWithID formalExpression = new ElementWithID(internID++, "FormalExpression");
        xmlElementInfo.getParentChildElements().put(methodDefElementWithId, Arrays.asList(description, formalExpression));

        ElementWithID translatedText = new ElementWithID(internID++, "TranslatedText");
        xmlElementInfo.getParentChildElements().put(description, Collections.singletonList(translatedText));

        xmlElementInfo.getElementAttributes().put(formalExpression, new HashMap<String, String>() {
            {
                put("Context", "redcap_calculation");
            }
        });

        FormalExpressionRepairer formalExpressionRepairer = new MethodFormalExpressionRepairer(
                FormalExpressionRepairer.Format.ofKey(this.shareObject.converterModelAttribute.getExpressionConversionStyle())
        );
        xmlElementInfo.getElementTexts().put(formalExpression, formalExpressionRepairer.repairExpression(itemDef.getAttribute(ItemDefCategories.CALCULATION.getAsString())));
        xmlElementInfo.getElementAttributes().put(translatedText, new HashMap<String, String>() {
            {
                put("xml:lang", language);
            }
        });

        this.createXMLTree(importedODM, methodDef, methodDefElementWithId, xmlElementInfo);

        metaDataVersion.appendChild(methodDef);
        conversionNotes.addNote(getElementIdentifierString(methodDef, "OID"),
                "Added MethodDef to reflect calculation in ItemDef " + itemDef.getAttribute("OID"),
                ConversionNotes.SeverenessLevel.NOTICE);

        Element itemRef = itemRefs.get(itemDef.getAttribute("OID"));
        if(itemRef != null) {
            itemRef.setAttribute("MethodOID", methodDefOID);
            itemDef.removeAttribute(ItemDefCategories.CALCULATION.getAsString());
            conversionNotes.addNote(getElementIdentifierString(itemRef, "ItemOID"),
                    "Added MethodOID " + methodDefOID + " to ItemRef to reference added MethodDef and removed "
                            + ItemDefCategories.CALCULATION.getAsString() + " attribute",
                    ConversionNotes.SeverenessLevel.NOTICE);
            conversionNotes.addNote(getElementIdentifierString(itemRef, "ItemOID"),
                    "Only copy and pasted logical expression. This will probably not work, since it most likely contains redcap specific functions.",
                    ConversionNotes.SeverenessLevel.WARNING);
        }
    }

    private void transformLogicBranches() {
        long start = System.currentTimeMillis();
        messageService.addMessage("Converting redcap:BranchingLogics to ConditionDefs...");
        messageService.sendMessages();

        Document importedODM = this.shareObject.importedODM;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        String language = shareObject.language;
        List<Element> itemDefs = this.shareObject.itemDefs.get(ItemDefCategories.BRANCHING_LOGIC.getAsString());
        Element metaDataVersion = this.shareObject.metaDataVersion;

        Map<String, Element> itemRefMap = this.shareObject.itemRefMap;

        itemDefs.forEach(LambdaHelper.throwingConsumerWrapper(i->addConditionNode(importedODM, conversionNotes, i, language, itemRefMap, metaDataVersion)));
        long end = System.currentTimeMillis();
        System.out.println("transformLogicBranches");
        System.out.println(end-start);
        System.out.println("Number of Items: " + itemDefs.size());
    }

    private void addConditionNode(Document importedODM, ConversionNotes conversionNotes, Element itemDef, String language,
                                  Map<String, Element> itemRefs, Element metaDataVersion) {
        XMLElementInfo xmlElementInfo = new XMLElementInfo();

        Element conditionDef = importedODM.createElement("ConditionDef");
        String conditionDefOID = OIDGenerator.getInstance().getOID();
        conditionDef.setAttribute("OID", conditionDefOID);
        conditionDef.setAttribute("Name", itemDef.getAttribute("Name") + "_cond");
        ElementWithID conditionDefElementWithId = new ElementWithID(0, conditionDef.getNodeName());

        int internID = 0;
        ElementWithID description = new ElementWithID(internID++, "Description");
        ElementWithID formalExpression = new ElementWithID(internID++, "FormalExpression");
        xmlElementInfo.getParentChildElements().put(conditionDefElementWithId, Arrays.asList(description, formalExpression));

        ElementWithID translatedText = new ElementWithID(internID++, "TranslatedText");
        xmlElementInfo.getParentChildElements().put(description, Collections.singletonList(translatedText));

        xmlElementInfo.getElementAttributes().put(formalExpression, new HashMap<String, String>() {
            {
                put("Context", "redcap_branchinglogic");
            }
        });

        FormalExpressionRepairer formalExpressionRepairer = new ConditionFormalExpressionRepairer(
                FormalExpressionRepairer.Format.ofKey(this.shareObject.converterModelAttribute.getExpressionConversionStyle())
        );
        xmlElementInfo.getElementTexts().put(formalExpression, formalExpressionRepairer.repairExpression(itemDef.getAttribute(ItemDefCategories.BRANCHING_LOGIC.getAsString())));
        xmlElementInfo.getElementAttributes().put(translatedText, new HashMap<String, String>() {
            {
                put("xml:lang", language);
            }
        });

        this.createXMLTree(importedODM, conditionDef, conditionDefElementWithId, xmlElementInfo);

        metaDataVersion.appendChild(conditionDef);
        conversionNotes.addNote(getElementIdentifierString(conditionDef, "OID"),
                "Added ConditionDef to reflect BranchingLogic in ItemDef " + itemDef.getAttribute("OID"),
                ConversionNotes.SeverenessLevel.NOTICE);

        Element itemRef = itemRefs.get(itemDef.getAttribute("OID"));
        if(itemRef != null) {
            itemRef.setAttribute("CollectionExceptionConditionOID", conditionDefOID);
            itemDef.removeAttribute(ItemDefCategories.BRANCHING_LOGIC.getAsString());
            conversionNotes.addNote(getElementIdentifierString(itemRef, "ItemOID"),
                    "Added CollectionExceptionConditionOID " + conditionDefOID + " to ItemRef to reference added ConditionDef and removed "
                            + ItemDefCategories.BRANCHING_LOGIC.getAsString() + " attribute",
                    ConversionNotes.SeverenessLevel.NOTICE);
        }
    }


    private void createXMLTree(Document importedODM, Element parentElement, ElementWithID parentElementWithId, XMLElementInfo xmlElementInfo) {

        Optional.ofNullable(xmlElementInfo.getParentChildElements().get(parentElementWithId)).ifPresent(p->p.forEach(e->{
            Element element = importedODM.createElement(e.getName());
            Optional.ofNullable(xmlElementInfo.getElementAttributes().get(e)).ifPresent(c->c.forEach(element::setAttribute));
            element.setTextContent(Optional.ofNullable(xmlElementInfo.getElementTexts().get(e)).orElse(""));
            parentElement.appendChild(element);
            createXMLTree(importedODM, element, e, xmlElementInfo);
        }));

    }

    private void replaceBooleanByInteger() throws XPathExpressionException {
        long start = System.currentTimeMillis();
        messageService.addMessage("Replacing boolean data types by integer...");
        messageService.sendMessages();

        Element metaDataVersion = this.shareObject.metaDataVersion;
        ConversionNotes conversionNotes = this.shareObject.conversionNotes;

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[@DataType='boolean']");
        NodeList nl = (NodeList) expr.evaluate(metaDataVersion, XPathConstants.NODESET);
        XmlUtil.asElementList(nl).forEach(n->n.setAttribute("DataType", "integer"));
        conversionNotes.addNote("boolean DataType", "Converted " + nl.getLength() + " boolean DataTypes to boolean",
                ConversionNotes.SeverenessLevel.NOTICE);

        long end = System.currentTimeMillis();
        System.out.println("replaceBooleanByInteger");
        System.out.println(end-start);
        System.out.println("Number of Items: " + nl.getLength());
    }

    private void createSeparateFileWithRedcapInformation() throws ParserConfigurationException {
        long start = System.currentTimeMillis();
        messageService.addMessage("Creating separate redcap file");
        messageService.sendMessages();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document redcapDocument = dBuilder.newDocument();

        ConversionNotes conversionNotes = this.shareObject.conversionNotes;
        Element globalVariables = this.shareObject.globalVariables;

        if(globalVariables != null) {
            Element redcapVariables = redcapDocument.createElement("RedcapTags");
            redcapVariables.setAttribute("xmlns:redcap", "https://projectredcap.org");
            redcapDocument.appendChild(redcapVariables);
            Element globalVariablesRedcap = redcapDocument.createElement("GlobalVariables");
            redcapVariables.appendChild(globalVariablesRedcap);
            XmlUtil.asList(globalVariables.getChildNodes()).forEach(n->{
                if(n.getNodeName().startsWith("redcap")) {
                    globalVariablesRedcap.appendChild(redcapDocument.adoptNode(n.cloneNode(true)));
                    conversionNotes.addNote("Redcap file", "Moved tag " + n.getNodeName() + " to separate redcap file",
                            ConversionNotes.SeverenessLevel.NOTICE);
                }
            });
        }
        this.shareObject.redcapFileWrapper.setRedcapFile(redcapDocument);
        long end = System.currentTimeMillis();
        System.out.println("createSeparateFileWithRedcapInformation");
        System.out.println(end-start);
    }

    private void addNamesToEmptyItemsAndIGs() {
        AtomicInteger counter = new AtomicInteger();
        this.shareObject.itemDefs.get(ItemDefCategories.ALL.getAsString()).forEach(i->{
            if(i.getAttribute("Name").equals("")) {
                i.setAttribute("Name", i.getAttribute("OID"));
                counter.getAndIncrement();
            }
        });
        conversionNotes.addNote("ItemDefs", "Name empty: Added OID as Name to " + counter.get() + " ItemDefs",
                ConversionNotes.SeverenessLevel.NOTICE);

        counter.set(0);
        XmlUtil.asElementList(this.shareObject.itemGroupDefs).forEach(i->{
            if(i.getAttribute("Name").equals("")) {
                i.setAttribute("Name", i.getAttribute("OID"));
                counter.getAndIncrement();
            }
        });
        conversionNotes.addNote("ItemGroupDef", "Name empty: Added OID as Name to " + counter.get() + " ItemGroupDefs",
                ConversionNotes.SeverenessLevel.NOTICE);
    }
}
