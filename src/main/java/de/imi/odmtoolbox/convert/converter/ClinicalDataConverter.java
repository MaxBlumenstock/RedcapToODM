package de.imi.odmtoolbox.convert.converter;

import de.imi.odmtoolbox.convert.enums.ConvertMethodEnumMarker;
import de.imi.odmtoolbox.convert.enums.ConverterMethods;
import de.imi.odmtoolbox.convert.helper.lambda.LambdaHelper;
import de.imi.odmtoolbox.convert.helper.lambda.OptionalConsumer;
import de.imi.odmtoolbox.convert.helper.xml.XmlUtil;
import de.imi.odmtoolbox.convert.model.ConverterModelAttribute;
import de.imi.odmtoolbox.convert.websocket.PushMessageService;
import de.imi.odmtoolbox.library.ConversionNotes;
import lombok.NonNull;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.xpath.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.imi.odmtoolbox.convert.enums.ConverterMethods.ClinicalData.*;

public class ClinicalDataConverter extends AbstractConverter{

    @Setter
    private static class ShareObject {
        private Document importedODM;
        private ConversionNotes conversionNotes;
        private ConverterModelAttribute converterModelAttribute;
        private String language;
        private Element clinicalData;
        private NodeList subjects;
        private NodeList formDefs;
        private String studyEventOID;
        //private NodeList itemGroupData;
        private NodeList itemRefs;
        private Map<String, String> itemOIDToIGOIDMap;
    }

    private final List<ConverterMethods.ClinicalData> converterMethods;
    private ShareObject shareObject;

    public ClinicalDataConverter(Document importedODM, ConversionNotes conversionNotes, ConverterModelAttribute converterModelAttribute,
                                 @NonNull PushMessageService messageService) throws SAXParseException, XPathExpressionException {
        super(importedODM, conversionNotes, messageService);
        this.createShareObject(converterModelAttribute);
        this.converterMethods = new ArrayList<>();
    }

    @Override
    protected Map<ConvertMethodEnumMarker, String> createConverterMethodToMethodNameMap() {
        Map<ConvertMethodEnumMarker, String> map = new HashMap<>();
        map.put(STUDY_EVENT_DATA_TO_SUBJECTS, "addStudyEventInformationToSubjects");
        map.put(FIX_REPEAT_KEYS, "fixRepeatKeys");
        map.put(FIX_IG_DATA, "fixItemGroupData");
        map.put(REMOVE_REDUNDANT_REDCAT_ATTRIBUTES, "removeRedundantRedcapVariables");
        return map;
    }

    @Override
    public void convert() {
        messageService.addMessage("Clinical data conversion started");
        messageService.sendMessages();
        converterMethods.forEach(LambdaHelper.throwingConsumerWrapper(this::executeMethod));
    }

    public void setMethods(List<ConverterMethods.ClinicalData> methods) {
        this.converterMethods.addAll(this.getMandatoryMethodsBefore());
        this.converterMethods.addAll(methods);
        this.converterMethods.addAll(this.getMandatoryMethodsAfter());
    }

    private List<ConverterMethods.ClinicalData> getMandatoryMethodsBefore() {
        return Arrays.asList(STUDY_EVENT_DATA_TO_SUBJECTS, FIX_REPEAT_KEYS);
    }

    private List<ConverterMethods.ClinicalData> getMandatoryMethodsAfter() {
        return Collections.singletonList(REMOVE_REDUNDANT_REDCAT_ATTRIBUTES);
    }

    private void createShareObject(ConverterModelAttribute converterModelAttribute) throws SAXParseException, XPathExpressionException {
        this.shareObject = new ShareObject();
        String language = converterModelAttribute.getLanguage();
        if(language == null || language.equals("")) {
            language = "en";
        }

        Element clinicalData = (Element) XmlUtil.asList(this.importedODM.getElementsByTagName("ClinicalData")).stream().findAny().orElse(null);
        if(clinicalData == null) {
            throw (new SAXParseException("Element with tag name 'clinicalData' not readable", null, null, -1, -1));
        }
        Element metaDataVersion = (Element) XmlUtil.asList(this.importedODM.getElementsByTagName("MetaDataVersion")).stream().findAny().orElse(null);
        if(metaDataVersion == null) {
            throw (new SAXParseException("Element with tag name 'MetaDataVersion' not readable", null, null, -1, -1));
        }

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[@StudyEventOID]");
        XmlUtil.asElementList((NodeList) expr.evaluate(importedODM, XPathConstants.NODESET)).stream().findAny()
                .ifPresent(s -> this.shareObject.setStudyEventOID(s.getAttribute("StudyEventOID")));

        this.shareObject.setImportedODM(this.importedODM);
        this.shareObject.setConversionNotes(this.conversionNotes);
        this.shareObject.setLanguage(language);
        this.shareObject.setClinicalData(clinicalData);
        this.shareObject.setSubjects(clinicalData.getElementsByTagName("SubjectData"));
        this.shareObject.setFormDefs(metaDataVersion.getElementsByTagName("FormDef"));
        this.shareObject.setItemRefs(metaDataVersion.getElementsByTagName("ItemRef"));
    }

    private void addStudyEventInformationToSubjects(){
        NodeList subjects = this.shareObject.subjects;
        XmlUtil.asElementList(subjects).forEach(this::addStudyEventDataElementAfterSubject);
    }

    private void addStudyEventDataElementAfterSubject(Element subject) {
        String studyEventOID = this.shareObject.studyEventOID;
        Element studyEventData = this.importedODM.createElement("StudyEventData");
        studyEventData.setAttribute("StudyEventOID", studyEventOID != null ? studyEventOID : "");
        NodeList formDatas = subject.getElementsByTagName("FormData");
        for(int i = formDatas.getLength() - 1; i >= 0; --i) {
            studyEventData.appendChild(formDatas.item(i));
        }
        subject.appendChild(studyEventData);
        conversionNotes.addNote(getElementIdentifierString(subject, "SubjectKey"), "Added StudyEventData to SubjectData tag",
                ConversionNotes.SeverenessLevel.NOTICE);
    }

    private void fixRepeatKeys() {
        NodeList subjects = this.shareObject.subjects;
        NodeList formDefs = this.shareObject.formDefs;
        Map<String, Element> formDefsToOID = new HashMap<>();
        XmlUtil.asElementList(formDefs).forEach(i->formDefsToOID.put(i.getAttribute("OID"), i));

        AtomicInteger counter = new AtomicInteger();
        XmlUtil.asElementList(subjects).stream()
                .flatMap(s->XmlUtil.asElementList(s.getElementsByTagName("FormData")).stream())
                .collect(Collectors.toList())
                .forEach(f->{
                    Element form = formDefsToOID.get(f.getAttribute("FormOID"));
                    boolean removeRepeating = !form.getAttribute("Repeating").equals("Yes");
                    if(removeRepeating) {
                        f.removeAttribute("FormRepeatKey");
                        counter.getAndIncrement();
                    }
                    XmlUtil.asElementList(f.getElementsByTagName("ItemGroupData")).forEach(i->i.removeAttribute("ItemGroupRepeatKey"));
                });
        conversionNotes.addNote("FormData",
                "Removed FormRepeatKey attribute from " + counter.get() + " FormData elements over " + subjects.getLength() + " subjects",
                ConversionNotes.SeverenessLevel.NOTICE);
        conversionNotes.addNote("ItemGroupData",
                "Removed ItemGroupRepeatKey attribute from all ItemGroupData elements",
                ConversionNotes.SeverenessLevel.NOTICE);
    }

    private void fixItemGroupData() {
        NodeList itemRefs = this.shareObject.itemRefs;
        Map<String, String> itemOIDToIGOIDMap = new HashMap<>();
        XmlUtil.asElementList(itemRefs).forEach(i->itemOIDToIGOIDMap.put(i.getAttribute("ItemOID"), ((Element) i.getParentNode()).getAttribute("OID")));

        NodeList subjects = this.shareObject.subjects;
        //Iterate over every formdata element of every subject
        XmlUtil.asElementList(subjects).stream().flatMap(s->XmlUtil.asElementList(s.getElementsByTagName("FormData")).stream()).collect(Collectors.toList()).forEach(f->{
            AtomicInteger counterRename = new AtomicInteger();
            AtomicInteger counterReassign = new AtomicInteger();
            //Create a map to store ItemGroupData elements in respect to their OID, specific for each form and a list for unassigned items
            Map<String, Element> formDataIGsMap = new HashMap<>();
            List<Element> unassignedItems = new ArrayList<>();
            //Iterate over ever ItemGroupData element of every formdata and
            XmlUtil.asElementList(f.getElementsByTagName("ItemGroupData")).forEach(igd->{
                //if the igd element has an oid, it gets added to the map
                String igOID = igd.getAttribute("ItemGroupOID");
                if(!igOID.equals("")) {
                    formDataIGsMap.put(igOID, igd);
                }
                //Iterate over every ItemData element of every ItemGroupData and check, whether it is assigned to the correct ItemGroup
                //If not, check, if there is already an entry in the map and assign it to the correct igd
                //If not, check, if all elements in the itemgroupdata belong to the same ItemRef
                //If so, change/set the OID of the igd to the correct one
                //if not, add the ItemData to the list of unassigned items and keep them for later
                AtomicBoolean skipFlag = new AtomicBoolean(false);
                XmlUtil.asElementList(igd.getElementsByTagName("ItemData")).stream()
                        .filter(id->!itemOIDToIGOIDMap.get(id.getAttribute("ItemOID")).equals(igOID))
                        .forEach(id->{
                            if(skipFlag.get()) return;
                            OptionalConsumer.of(Optional.ofNullable(formDataIGsMap.get(itemOIDToIGOIDMap.get(id.getAttribute("ItemOID")))))
                                    .ifPresent(ig->{
                                        ig.appendChild(id);
                                        counterReassign.getAndIncrement();
                                    })
                                    .ifNotPresent(() -> {
                                        if(checKIfAllItemsInSameGroup(igd, itemOIDToIGOIDMap)) {
                                            igd.setAttribute("ItemGroupOID", itemOIDToIGOIDMap.get(id.getAttribute("ItemOID")));
                                            skipFlag.set(true);
                                            counterRename.getAndIncrement();
                                        }
                                        else {
                                            unassignedItems.add(id);
                                        }
                                    });
                        });
            });

            //Iterate over every not yet assigned item. If the itemgroup of the element does not yet exist in the map, create a new xml element and add it to the FormDat
            //Also add it the the map and keep doing until all items are assigned
            counterReassign.accumulateAndGet(unassignedItems.size(), Integer::sum);
            unassignedItems.forEach(item-> OptionalConsumer.of(Optional.ofNullable(formDataIGsMap.get(itemOIDToIGOIDMap.get(item.getAttribute("ItemOID")))))
                    .ifPresent(ig->ig.appendChild(item))
                    .ifNotPresent(()->{
                        Element newIgd = addIGDToFormData(f, itemOIDToIGOIDMap.get(item.getAttribute("ItemOID")));
                        newIgd.appendChild(item);
                        formDataIGsMap.put(itemOIDToIGOIDMap.get(item.getAttribute("ItemOID")), newIgd);
                    }));
            //Check for every itemgroupdata of the form, if is empty. If so, delete it
            XmlUtil.asElementList(f.getElementsByTagName("ItemGroupData")).stream()
                    .filter(igd->igd.getAttribute("ItemGroupOID").equals("") && igd.getElementsByTagName("ItemData").getLength() < 1).forEach(f::removeChild);

            String formKey = f.getAttribute("FormRepeatKey");
            if(formKey.equals("")) {
                formKey = "-";
            }
            if(counterRename.get() > 0) {
                conversionNotes.addNote("SubjectData(" + ((Element)f.getParentNode()).getAttribute("SubjectKey") + "), FormData("
                                + f.getAttribute("FormOID") + ", RepeatKey: " + formKey + ")",
                        "Renamed " + counterRename.get() + " ItemGroupData elements due to missing ItemGroupOID",
                        ConversionNotes.SeverenessLevel.NOTICE);
            }
            if(counterReassign.get() > 0) {
                conversionNotes.addNote("SubjectData(" + ((Element)f.getParentNode()).getAttribute("SubjectKey") + "), FormData("
                                + f.getAttribute("FormOID") + ", RepeatKey: " + formKey + ")",
                        "Reassigned " + counterReassign.get() + " ItemGroupData elements due to being assigned to the wrong ItemGroupData element",
                        ConversionNotes.SeverenessLevel.NOTICE);
            }
        });
    }

    private boolean checKIfAllItemsInSameGroup(Element igd, Map<String, String> itemOIDToIGOIDMap) {
        return XmlUtil.asElementList(igd.getElementsByTagName("ItemData")).stream().map(id->itemOIDToIGOIDMap.get(id.getAttribute("ItemOID"))).distinct().count() <= 1;
    }

    private Element addIGDToFormData(Element formData, String igOID) {
        Element igd = this.importedODM.createElement("ItemGroupData");
        igd.setAttribute("ItemGroupOID", igOID);
        formData.appendChild(igd);
        return igd;
    }

    private void removeRedundantRedcapVariables() {
        messageService.addMessage("Removing redcap attributes...");
        messageService.sendMessages();

        NodeList subjects = this.shareObject.subjects;
        XmlUtil.asElementList(subjects).forEach(s -> s.removeAttribute("redcap:RecordIdField"));
        XmlUtil.asElementList(subjects).forEach(s -> s.removeAttribute("xmlns:redcap"));

        conversionNotes.addNote("redcap attributes", "Removed redcap attributes from " + subjects.getLength() + " SubjectData tags",
                ConversionNotes.SeverenessLevel.NOTICE);


    }
}
