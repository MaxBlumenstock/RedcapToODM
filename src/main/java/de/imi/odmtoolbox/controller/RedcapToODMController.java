package de.imi.odmtoolbox.controller;

import de.imi.odmtoolbox.convert.converter.AbstractConverter;
import de.imi.odmtoolbox.convert.converter.ClinicalDataConverter;
import de.imi.odmtoolbox.convert.converter.FormalExpressionRepairer;
import de.imi.odmtoolbox.convert.converter.MetaDataConverter;
import de.imi.odmtoolbox.convert.enums.ConvertMethodEnumMarker;
import de.imi.odmtoolbox.convert.enums.ConverterMethods;
import de.imi.odmtoolbox.convert.enums.MultiAnswerItemOptions;
import de.imi.odmtoolbox.convert.helper.Pair;
import de.imi.odmtoolbox.convert.helper.lambda.LambdaHelper;
import de.imi.odmtoolbox.convert.model.ConverterModelAttribute;
import de.imi.odmtoolbox.convert.websocket.PushMessageService;
import de.imi.odmtoolbox.library.ConversionNotes;
import de.imi.odmtoolbox.library.ODMParser;
import de.imi.odmtoolbox.library.StringUtilities;
import de.imi.odmtoolbox.model.Language;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Controller to handle requests for converting redcap files to odm files
 *
 * @author Max Blumenstock <max.blumenstock@med.uni-heidelberg.de>
 */
@Controller
public class RedcapToODMController {

    /**
     * Fetch a logger. This must be used for any output.
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(RedcapToODMController.class);

    /**
     * Autowire necessary Spring beans. These are defined in the Spring config.
     */

    private final ServletContext servletContext;
    private final ODMParser odmParser;
    private final StringUtilities stringUtilities;
    @Value("${ODMRepair.URL}")
    private String repairURL;
    private final PushMessageService messageService;
    private final Map<String, ConvertMethodEnumMarker> metaDataConverterForStringOptionMap;
    private final Map<String, ConversionNotes> conversionNotesList;

    public RedcapToODMController(ServletContext servletContext, ODMParser odmParser, StringUtilities stringUtilities, PushMessageService messageService) {
        this.metaDataConverterForStringOptionMap = this.createConverterMap();
        this.servletContext = servletContext;
        this.odmParser = odmParser;
        this.stringUtilities = stringUtilities;
        this.messageService = messageService;
        this.conversionNotesList = new HashMap<>();
    }


    /**
     * Shows the ODM upload page.
     *
     * @param model The model, which holds the information for the view.
     * @return The ODM upload page.
     */
    @RequestMapping(value = "/redcap/redcaptoodm", method = RequestMethod.GET)
    public String uploadFiles(Model model) {
        this.addAvailableLanguagesToModel(model);
        this.addMultiAnswerOptionsToModel(model);
        this.addSelectableOptionsToModel(model);
        this.addFormalExpressionConversionStyles(model);
        return "redcap/redcaptoodm";
    }

    /**
     * Handles the conversion of the uploaded ODM file into target format
     * merged into a zip file as an api interface. Packed into zip archive since
     * the may be several forms defined in the single ODM that result in several
     * output files.
     *
     * @param redcapFile The Redcap file that should be converted to ODM.
     * @param langCode The language, which should be exported.
     * @param response The resulting Zip file as byte array or an error
     *                 response.
     * @throws IOException Error while sending an error response.
     */
    @RequestMapping(value = "/redcap/api", method = RequestMethod.POST)
    public void odmToTemplate(@RequestParam() MultipartFile redcapFile,
                              @RequestParam(required = false) String langCode,
                              HttpServletResponse response) throws IOException, TransformerException, SAXParseException {
        stringUtilities.init();
        Document importedRedcapFile;
        byte[] zipFile;

        // Validate the uploaded file
        List<SAXException> parseErrors = odmParser.isValid(odmParser.parseODMFile(redcapFile));
        if (parseErrors.isEmpty()) {
            // Import the ODM file and check if the format is correct
            importedRedcapFile = odmParser.parseODMFile(redcapFile);

            ////////////////////////////////////////////////////////////////////////////////
            // TODO: call your converter method. Do not change anything else in this method.
            zipFile = convertRedcapToODM(importedRedcapFile, langCode);

            // Configure and write response
            response.setContentType("application/force-download");
            String fileName = redcapFile.getOriginalFilename();
            if(fileName == null || fileName.equals("")) {
                fileName = "converted-odm-file";
            }
            // Strip the extension
            int extensionIndex = fileName.lastIndexOf(".");
            if (extensionIndex != -1) {
                fileName = fileName.substring(0, extensionIndex) + ".zip";
            } else {
                fileName = fileName + ".zip";
            }
            response.setHeader("Content-Disposition", "attachment;filename=\""
                    + "_" + fileName+ ".zip\"");
            ServletOutputStream outStream = response.getOutputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(zipFile, 0, zipFile.length);
            bos.writeTo(outStream);
            bos.close();
            outStream.close();
        } else {
            logger.info("An error occured during importing of Redcap file");
            response.sendError(501,"An error occured during importing of Redcap file");
        }

    }

    /**
     * Handles the conversion of the uploaded ODM file into target format
     * merged into a zip file. Difference to odmToTemplate: returns a view for
     * the ODMToolbox.
     *
     * @param redcapFile The ODM file that should be converted.
     * @param model The model, which holds the information for the view.
     * @return The page, containing the result as base64string with download.
     */
    @RequestMapping(value = "/redcap/redcaptoodm", method = RequestMethod.POST, params = "action")
    public String odmToRedcap(@RequestParam() MultipartFile redcapFile,
                              @ModelAttribute("selectedOptions") ConverterModelAttribute selectedOptions,
                              Model model){
        stringUtilities.init();
        Document importedRedcapFile;
        byte[] zipFile;
        
        // Errors that are displayed to the user at the end.
        ArrayList<Exception> errors = new ArrayList<>();

        try {
            // Validate the uploaded file
            //List<SAXException> parseErrors = odmParser.isValid(odmParser.parseODMFile(redcapFile));
            // Import the ODM file and check if the format is correct
            importedRedcapFile = odmParser.parseODMFile(redcapFile);

            ////////////////////////////////////////////////////////////////////////////////
            zipFile = convertRedcapToODM(importedRedcapFile, selectedOptions);

            // Encode the ZipFile into a base64 String.
            // The String will be added to the website and downloaded via javascript.
            String base64String = "data:application/octet-stream;base64," + Base64.encodeBase64String(zipFile);
            String fileName = redcapFile.getOriginalFilename();
            // Strip the extension
            if(fileName == null || fileName.equals("")) {
                fileName = "converted-odm-file";
            }
            if (fileName.lastIndexOf(".") != -1) {
                fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".zip";
            } else {
                fileName = fileName + ".zip";
            }
            model.addAttribute("filename", fileName);
            model.addAttribute("base64String", base64String);

        } catch (Exception ex) {
            errors.add(ex);
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            for (Exception ex : errors) {
                logger.info("Error during conversion of the ODM file: ", ex);
            }
        }
        model.addAttribute("conversionNotes",conversionNotesList.values().stream().filter(c->!c.isEmpty()).flatMap(c->c.getNotesList().stream()).collect(Collectors.toList()));

        this.addAvailableLanguagesToModel(model);
        this.addMultiAnswerOptionsToModel(model);
        this.addSelectableOptionsToModel(model);
        this.addFormalExpressionConversionStyles(model);
        return "redcap/redcaptoodm";
    }

    private byte[] convertRedcapToODM(Document importedODM, String langCode)
            throws IOException, TransformerException, SAXParseException {
       ConverterModelAttribute selectedOptions = new ConverterModelAttribute();
       List<Pair<String, Boolean>> selectableOptions = getSelectableOptionsMetaData();
       List<String> selectableOptionsStrings = new ArrayList<>();
       selectableOptions.forEach(e->selectableOptionsStrings.add(e.getKey()));
       selectedOptions.setSelectedAnswersMetaData(selectableOptionsStrings);
       selectedOptions.setLanguage(langCode);
       return this.convertRedcapToODM(importedODM,selectedOptions);
    }

    private byte[] convertRedcapToODM(Document importedODM,  ConverterModelAttribute converterModelAttribute)
            throws IOException, TransformerException, SAXParseException {
        long start = System.currentTimeMillis();

        ConversionNotes metaDataConversionNotes = new ConversionNotes();
        conversionNotesList.put("Conversion-Notes-MetaData.csv", metaDataConversionNotes);
        AbstractConverter.RedcapFileWrapper redcapFileWrapper = new AbstractConverter.RedcapFileWrapper();
        List<ConverterMethods.MetaData> metaDataConverterList = new ArrayList<>();
        converterModelAttribute.getSelectedAnswersMetaData()
                .forEach(e->metaDataConverterList.add((ConverterMethods.MetaData) metaDataConverterForStringOptionMap.get(e)));
        MetaDataConverter metaDataConverter = new MetaDataConverter(importedODM, metaDataConversionNotes, converterModelAttribute, messageService, redcapFileWrapper);
        metaDataConverter.setMethods(metaDataConverterList);
        metaDataConverter.convert();

        if(converterModelAttribute.isConvertClinicalData()) {
            ConversionNotes clinicalDataConversionNotes = new ConversionNotes();
            conversionNotesList.put("Conversion-Notes-ClinicalData.csv", clinicalDataConversionNotes);
            try{
                List<ConverterMethods.ClinicalData> clinicalDataConverterList = new ArrayList<>();
                converterModelAttribute.getSelectedAnswersClinicalData()
                        .forEach(e->clinicalDataConverterList.add((ConverterMethods.ClinicalData) metaDataConverterForStringOptionMap.get(e)));
                ClinicalDataConverter clinicalDataConverter = new ClinicalDataConverter(importedODM, clinicalDataConversionNotes, converterModelAttribute, messageService);
                clinicalDataConverter.setMethods(clinicalDataConverterList);
                clinicalDataConverter.convert();
            } catch (Exception ex) {
                clinicalDataConversionNotes.addNote("Clinical Data",
                        "Something went wrong when converting clinical data. It was probably not converted entirely, if at all",
                        ConversionNotes.SeverenessLevel.CRITICAL);
            }
        }

        long start2 = System.currentTimeMillis();
        ByteArrayOutputStream studyByteOutput = new ByteArrayOutputStream();
        ZipOutputStream studyZipOutput = new ZipOutputStream(studyByteOutput);

        Map<String, ByteArrayOutputStream> streamMap = new HashMap<>();
        if(converterModelAttribute.isRepairODM()) {
            List<String> converter = new ArrayList<>();
            converter.add("50");
            converter.add("70");
            converter.add("80");
            Optional<Map<byte[], ConversionNotes>> repairedODM = this.repairODM(createByteStreamFromXML(importedODM), converter, metaDataConversionNotes);
            if(repairedODM.isPresent()) {
                ByteArrayOutputStream repairedFormByteOutput = new ByteArrayOutputStream();
                repairedFormByteOutput.write((byte[]) repairedODM.map(s->s.keySet().toArray()[0]).orElse(new byte[0]));
                streamMap.put("odm-file.xml",repairedFormByteOutput);
            }
            else {
                streamMap.put("odm-file.xml", createByteStreamFromXML(importedODM));
                metaDataConversionNotes.addNote("Repair ODM",
                        "Something went wrong when trying to repair the ODM file. The converted, but not repaired odm file was put as output.",
                        ConversionNotes.SeverenessLevel.CRITICAL);
            }
        }
        streamMap.put("odm-file.xml", createByteStreamFromXML(importedODM));
        if(redcapFileWrapper.getRedcapFile() != null) {
            streamMap.put("redcap-tags-file.xml", createByteStreamFromXML(redcapFileWrapper.getRedcapFile()));
        }

        //put the converstion notes in the zip file
        conversionNotesList.forEach(LambdaHelper.throwingBiConsumerWrapper((k,v)->{
            ZipEntry zec = new ZipEntry(k);
            studyZipOutput.putNextEntry(zec);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] csvFile = v.getConversionNotes();
            outputStream.write(csvFile, 0, csvFile.length);
            outputStream.writeTo(studyZipOutput);
        }));

        streamMap.forEach(LambdaHelper.throwingBiConsumerWrapper((k, v)->{
            ZipEntry ze = new ZipEntry(k);
            studyZipOutput.putNextEntry(ze);
            //repiaredODMStream.write((byte[]) repairedODM.map(s->s.keySet().toArray()[0]).orElse(new byte[0]));
            //repiaredODMStream = transformedODM;
            // write the bytes to the zipfile
            v.writeTo(studyZipOutput);
            v.flush();
            v.reset();
        }));

        // finish the file inside the zip file
        studyZipOutput.closeEntry();

        studyZipOutput.flush();
        studyZipOutput.close();
        // convert into Bytes
        long end2 = System.currentTimeMillis();
        System.out.println("creating files");
        System.out.println(end2-start2);

        long end = System.currentTimeMillis();
        System.out.println("actual time");
        System.out.println(end-start);

        return studyByteOutput.toByteArray();
    }


    private void addAvailableLanguagesToModel(Model model) {
        Map<String, String> languages = new HashMap<>();
        Arrays.stream(Language.language.values()).forEach(l->languages.put(l.name(), Language.getWrittenName(l)));
        model.addAttribute("languages", languages);
    }

    private void addMultiAnswerOptionsToModel(Model model) {
        model.addAttribute("multiAnswerOptions",  Arrays.stream(MultiAnswerItemOptions.values()).map(MultiAnswerItemOptions::getAsString).collect(Collectors.toList()));
    }

    private void addFormalExpressionConversionStyles(Model model) {
        model.addAttribute("formalExpressionOptions",Arrays.stream(FormalExpressionRepairer.Format.values()).map(FormalExpressionRepairer.Format::getAsString).collect(Collectors.toList()));
    }

    public Optional<Map<byte[], ConversionNotes>> repairODM(ByteArrayOutputStream odmOutput, List<String> converter, ConversionNotes conversionNotes) throws IOException {

        messageService.addMessage("Repairing remaining errors with repair converter...");
        messageService.sendMessages();

        byte[] odm = null;
        String newNotes = null;

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();

        ByteArrayResource odmFileResource;
        odmFileResource = new ByteArrayResource(odmOutput.toByteArray()) {
            @Override
            public String getFilename() {
                return "odmFile.xml";
            }
        };

        parameters.add("odmFile", odmFileResource);
        if (converter != null) {
            converter.forEach(c->parameters.add("converter", c));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(parameters, headers);
        RestTemplate restTemplate = new RestTemplate();

        InputStream responseStream;

        try {
            String url = repairURL + "/api.html";
            ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, Resource.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                responseStream = Optional.ofNullable(responseEntity.getBody()).map(LambdaHelper.throwingOptionalFunctionrWrapper(InputStreamSource::getInputStream)).orElse(null);
                //responseStream = Optional.ofNullable(responseEntity.getBody()).map(LambdaHelper.throwingOptionalFunctionrWrapper(InputStreamSource::getInputStream)).orElse(null);
            } else {
                logger.error("RestClient bad request - responseEntity{}", responseEntity);
                return Optional.empty();
            }

        } catch (RestClientException ex) {
            logger.error("RestClient communication failure", ex);
            return Optional.empty();
        }

        if (responseStream != null) {
            try (ZipInputStream zis = new ZipInputStream(responseStream)) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    if (zipEntry.getName().equals("conversionNotes.csv") || zipEntry.getName().equals("odm.xml")) {
                        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] bytes = new byte[2048];
                        int length;
                        while ((length = zis.read(bytes)) >= 0) {
                            bos.write(bytes, 0, length);
                        }
                        bos.close();

                        if (zipEntry.getName().equals("conversionNotes.csv")) {
                            newNotes = new String(bos.toByteArray(), StandardCharsets.UTF_8);
                        } else {
                            odm = bos.toByteArray();
                        }
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        }
        return Optional.of(Collections.singletonMap(odm, parseConversionNotes(conversionNotes, newNotes)));
    }

    /**
     * Helper to parse notes from repair API to Notes object
     *
     * @param conversionNotes String of notes to convert
     * @return Notes object.
     * @throws java.io.IOException thrown when conversion notes cannot be parsed
     */
    private ConversionNotes parseConversionNotes(ConversionNotes notes, String conversionNotes) throws IOException {

        try (BufferedReader br = new BufferedReader(new StringReader(conversionNotes))) {
            String line;
            //Skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                notes.addNote(values[0], values[1], ConversionNotes.SeverenessLevel.valueOf(values[2]));
            }
        }
        return notes;
    }

    private ByteArrayOutputStream createByteStreamFromXML(Document document) throws TransformerException {
        document.setXmlStandalone(true);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource xmlSource = new StreamSource(servletContext.getRealPath("/template") + "/xmlTemplate.xsl");
        Transformer transformer = transformerFactory.newTransformer(xmlSource);
        /*transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");*/
        DOMSource source = new DOMSource(document);

        ByteArrayOutputStream formByteOutput = new ByteArrayOutputStream();
        StreamResult documentStreamResult = new StreamResult(formByteOutput);
        transformer.transform(source, documentStreamResult);
        return formByteOutput;
    }

    private void addSelectableOptionsToModel(Model model) {
        ConverterModelAttribute converterModelAttribute = new ConverterModelAttribute();
        List<Pair<String, Boolean>> listMetaData = getSelectableOptionsMetaData();
        List<Pair<String, Boolean>> listClinicalData = getSelectedOptionsClinicalData();

        List<String> selectOptionsMetaData = new ArrayList<>();
        listMetaData.forEach(i->selectOptionsMetaData.add(i.getKey()));
        converterModelAttribute.setOptionsMetaData(selectOptionsMetaData);

        List<String> selectedOptionsMetData = new ArrayList<>();
        selectedOptionsMetData.add("studyEvent");
        listMetaData.forEach(i->{
            if(i.getValue()) {
                selectedOptionsMetData.add(i.getKey());
            }
        });

        List<String> selectOptionsClinicalData = new ArrayList<>();
        listClinicalData.forEach(i->selectOptionsClinicalData.add(i.getKey()));
        converterModelAttribute.setOptionsClinicalData(selectOptionsClinicalData);

        List<String> selectedOptionsClinicalData = new ArrayList<>();
        listClinicalData.forEach(i->{
            if(i.getValue()) {
                selectedOptionsClinicalData.add(i.getKey());
            }
        });


        converterModelAttribute.setSelectedAnswersMetaData(selectedOptionsMetData);
        converterModelAttribute.setSelectedAnswersClinicalData(selectedOptionsClinicalData);
        model.addAttribute("selectedOptions", converterModelAttribute);
    }

    private List<Pair<String, Boolean>> getSelectableOptionsMetaData() {
        return Arrays.asList(
                new Pair<>("Convert redcap:LogicBranches", true),
                new Pair<>("Convert redcap:Calculations", true),
                new Pair<>("Convert boolean to integer", false),
                new Pair<>("Replace empty names with OID", true),
                new Pair<>("Store redcap data in file", false));
    }

    private List<Pair<String, Boolean>> getSelectedOptionsClinicalData() {
        return Collections.singletonList(new Pair<>("Repair ItemGroupData OIDs", false));
    }

    private Map<String, ConvertMethodEnumMarker> createConverterMap() {
        Map<String, ConvertMethodEnumMarker> map = new HashMap<>();
        map.put("languageTransform", ConverterMethods.MetaData.ADD_LANGUAGE_INFORMATION);
        map.put("studyEvent", ConverterMethods.MetaData.ADD_PROTOCOL);
        map.put("Convert redcap:Calculations", ConverterMethods.MetaData.CONVERT_REDCAP_CALCULATIONS);
        map.put("Convert redcap:LogicBranches", ConverterMethods.MetaData.CONVERT_REDCAP_LOGIC_BRANCHES);
        map.put("Convert boolean to integer", ConverterMethods.MetaData.BOOLEAN_TO_INTEGER);
        map.put("Replace empty names with OID", ConverterMethods.MetaData.COMPLETE_ITEM_NAMES);
        map.put("Store redcap data in file", ConverterMethods.MetaData.STORE_REDCAP_DATA_IN_FILE);
        map.put("Repair ItemGroupData OIDs", ConverterMethods.ClinicalData.FIX_IG_DATA);

        return map;
    }

}

