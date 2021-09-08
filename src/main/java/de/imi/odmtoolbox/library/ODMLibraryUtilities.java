package de.imi.odmtoolbox.library;

import de.imi.odmtoolbox.comparator.ODMComplexTypeDefinitionCodeListItemComparator;
import de.imi.odmtoolbox.comparator.ODMComplexTypeDefinitionItemGroupRefComparator;
import de.imi.odmtoolbox.comparator.ODMComplexTypeDefinitionItemRefComparator;
import de.imi.odmtoolbox.model.RangeCheck;
import de.unimuenster.imi.org.cdisc.odm.v132.CLDataType;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCheckValue;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeList;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListItem;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionErrorMessage;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMeasurementUnit;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMeasurementUnitRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionRangeCheck;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionTranslatedText;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Collection of utility methods that operate with {@link ODM} objects.
 *
 * @author m_hein31
 */
@Service
public class ODMLibraryUtilities {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
            .getLogger(ODMLibraryUtilities.class);

    /**
     * Returns the formref searched by given OID in the given list of formrefs.
     *
     * @param formRefList The list of formrefs which will be searched for the
     * given OID.
     * @param OID The OID wich will be searched for.
     * @return The found formref or <code>null</code> if nothing was found.
     */
    public static ODMcomplexTypeDefinitionFormRef getFormRefByOID(List<ODMcomplexTypeDefinitionFormRef> formRefList, String OID) {
        for (ODMcomplexTypeDefinitionFormRef formRef : formRefList) {
            if (formRef.getFormOID().equals(OID)) {
                return formRef;
            }
        }
        return null;
    }

    /**
     * Returns the formdef searched by given OID in the given list of formdefs.
     *
     * @param formDefList The list of formdefs which will be searched for the
     * given OID.
     * @param OID The OID wich will be searched for.
     * @return The found formdef or <code>null</code> if nothing was found.
     */
    public static ODMcomplexTypeDefinitionFormDef getFormDefByOID(List<ODMcomplexTypeDefinitionFormDef> formDefList, String OID) {
        for (ODMcomplexTypeDefinitionFormDef formDef : formDefList) {
            if (formDef.getOID().equals(OID)) {
                return formDef;
            }
        }
        return null;
    }

    /**
     * Returns the itemgroupdef searched by given OID in the given list of
     * itemgroupdefs.
     *
     * @param itemGroupDefList The list of itemgroupdefs which will be searched
     * for the given OID.
     * @param OID The OID wich will be searched for.
     * @return The found itemgroupdef or <code>null</code> if nothing was found.
     */
    public static ODMcomplexTypeDefinitionItemGroupDef getItemGroupDefByOID(List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefList, String OID) {
        for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefList) {
            if (itemGroupDef.getOID().equals(OID)) {
                return itemGroupDef;
            }
        }
        return null;
    }

    /**
     * Returns the itemdef searched by given OID in the given list of itemdefs.
     *
     * @param itemDefList The list of itemdefs which will be searched for the
     * given OID.
     * @param OID The OID wich will be searched for.
     * @return The found itemdef or <code>null</code> if nothing was found.
     */
    public static ODMcomplexTypeDefinitionItemDef getItemDefByOID(List<ODMcomplexTypeDefinitionItemDef> itemDefList, String OID) {
        for (ODMcomplexTypeDefinitionItemDef itemDef : itemDefList) {
            if (itemDef.getOID().equals(OID)) {
                return itemDef;
            }
        }
        return null;
    }

    /**
     * Returns the codelist searched by given OID in the given list of
     * codelists.
     *
     * @param codeListList The list of codelists which will be searched for the
     * given OID.
     * @param codeListOID The OID wich will be searched for.
     * @return The found codelist or <code>null</code> if nothing was found.
     */
    public static ODMcomplexTypeDefinitionCodeList getCodeListByOID(List<ODMcomplexTypeDefinitionCodeList> codeListList, String codeListOID) {
        for (ODMcomplexTypeDefinitionCodeList codeList : codeListList) {
            if (codeList.getOID().equals(codeListOID)) {
                return codeList;
            }
        }
        return null;
    }

    /**
     * Returns all translations from given TranslatedText elements according to
     * their language code.
     *
     * @param translatedTextList The translated text elements, which should be
     * returned. Must not be <code>null</code>. Can be empty.
     * @return All Translations from given TranslatedText elements.
     */
    public static Map<String, String> getTranslations(List<ODMcomplexTypeDefinitionTranslatedText> translatedTextList) {
        Map<String, String> result = new HashMap<>();
        // Loop through all translated texts
        for (ODMcomplexTypeDefinitionTranslatedText translatedText : translatedTextList) {
            String lang = translatedText.getLang();
            if (lang == null) { // If a translatedText does not have a lang-attribute, it should be the default
                lang = "default";
            } 
            // Replace new line and HTML newline
            String value = translatedText.getValue().replaceAll("(\\r|\\n)", " ").replaceAll("&#13;", " ");
            // Store it to the result
            result.put(lang, value.trim());
        }
        return result;
    }

    /**
     * Tries to return the translated text corresponding to the given language
     * code. If the given language code is not found amongst the translations
     * then the returned text is english or german if english is not present. If
     * neither english nor german are available then the first translation is
     * chosen.
     *
     * @param translationList The list of translated texts. Must not be
     * <code>null</code> or empty.
     * @param languageCode The language code the translation should be searched
     * for.
     * @return A translated text. Or null if nothing is found at all
     * @throws IllegalArgumentException If the translationList is null or an
     * internal error has occurred.
     */
    public static Map<String, String> getBestFittingTranslation(List<ODMcomplexTypeDefinitionTranslatedText> translationList, String languageCode) throws IllegalArgumentException {
        if (translationList != null && !translationList.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            Map<String, String> localizedTexts = ODMLibraryUtilities.getTranslations(translationList);
            if (localizedTexts == null || localizedTexts.isEmpty()) {
                throw new IllegalArgumentException("Could not find a proper question text.");
            } else {
                String translationLangCode = languageCode;
                String translatedText;
                // If the given language code was found
                if (localizedTexts.get(translationLangCode) != null) {
                    // Store it to the result
                    translatedText = localizedTexts.get(translationLangCode);
                    result.put(translationLangCode, translatedText);
                } // Otherwise search for default, english or german
                else {
                    if (localizedTexts.get("default") !=null) {
                        translationLangCode = "default";
                        translatedText = localizedTexts.get("default");
                        result.put(translationLangCode, translatedText);
                    } else if (localizedTexts.get("en") != null) {
                        translationLangCode = "en";
                        translatedText = localizedTexts.get("en");
                        result.put(translationLangCode, translatedText);
                    } else if (localizedTexts.get("en-GB") != null) {
                        translationLangCode = "en-GB";
                        translatedText = localizedTexts.get("en-GB");
                        result.put(translationLangCode, translatedText);
                    } else if (localizedTexts.get("de") != null) {
                        translationLangCode = "de";
                        translatedText = localizedTexts.get("de");
                        result.put(translationLangCode, translatedText);
                    } else if (localizedTexts.get("de-DE") != null) {
                        translationLangCode = "de-DE";
                        translatedText = localizedTexts.get("de-DE");
                        result.put(translationLangCode, translatedText);
                    } // If neither english nor german was found
                    else {
                        // Store the first translation to the result
                        translationLangCode = localizedTexts.keySet().iterator().next();
                        translatedText = localizedTexts.get(translationLangCode);
                        result.put(translationLangCode, translatedText);
                    }
                }
                return result;
            }
        } else {
            throw new IllegalArgumentException("The translationList was null or empty.");
        }
    }

    /**
     * Returns a map that contains the coded values as key and the best fitting
     * translations of the codelist items as value. The translation is done with  {@link #getBestFittingTranslation(java.util.List, java.lang.String) }
     *
     * @param codeList The codelist wich contains the codelistitems , of which
     * the translations should be returned. Must not be <code>null</code> or
     * empty.
     * @param languageCode The language code the translation should be searched
     * for.
     * @return A map containig coded values as keys and codelist item
     * translations as values.
     */
    public static Map<String, String> getCodeListItemTranslationTextsCodedValueMap(ODMcomplexTypeDefinitionCodeList codeList, String languageCode) {
        if (codeList != null) {
            List<ODMcomplexTypeDefinitionCodeListItem> codeListItems = codeList.getCodeListItem();
            if (codeListItems != null && !codeListItems.isEmpty()) {
                Map<String, String> codeListItemTranslationsCodedValues = new LinkedHashMap<>();
                // Sort the codelistitems by order number
                Collections.sort(codeListItems, new ODMComplexTypeDefinitionCodeListItemComparator());
                // Loop through all codelistitems
                for (ODMcomplexTypeDefinitionCodeListItem codeListItem : codeListItems) {
                    // Fetch the best fitting translation
                    codeListItemTranslationsCodedValues.put(codeListItem.getCodedValue(), ODMLibraryUtilities.getBestFittingTranslation(codeListItem.getDecode().getTranslatedText(), languageCode).entrySet().iterator().next().getValue());
                }
                return codeListItemTranslationsCodedValues;
            } else {
                throw new IllegalArgumentException("The codelistitems were null or empty.");
            }
        } else {
            throw new IllegalArgumentException("The codeList was null.");
        }

    }

    /**
     * Returns a String that contains all referenced measurement units seperated
     * by a semicolon in the language according to the given langCode
     * {@link #getBestFittingTranslation(java.util.List, java.lang.String) }
     *
     * @param measurementUnitRefs The list with measurement unit references.
     * @param measurementUnitList The list of measurement unit definitions.
     * @param languageCode The language code the translation should be searched
     * for.
     * @return A string that contains all referenced measurement units in the
     * language according to the given language code seperated by semicolon.
     * @throws RuntimeException If one of the Translations for a measurement
     * unit is null or empty.
     */
    public static String getMeasurementUnitTranslationTexts(List<ODMcomplexTypeDefinitionMeasurementUnitRef> measurementUnitRefs,
            List<ODMcomplexTypeDefinitionMeasurementUnit> measurementUnitList, String languageCode) throws RuntimeException {
        String measurementUnit = "";
        // Get the MeasurementUnitDefinition of each referenced unit
        for (ODMcomplexTypeDefinitionMeasurementUnitRef measurementUnitRef : measurementUnitRefs) {
            for (ODMcomplexTypeDefinitionMeasurementUnit measurementUnitDef : measurementUnitList) {
                // Find the appropriate measurementunit definition for the refenrenced one
                if (measurementUnitDef.getOID().equals(measurementUnitRef.getMeasurementUnitOID())) {
                    // Get the best fitting translation for the current mesurementunit
                    String measurementUnitText = ODMLibraryUtilities.getBestFittingTranslation(measurementUnitDef.getSymbol().getTranslatedText(), languageCode).entrySet().iterator().next().getValue();
                    if (measurementUnitText != null && !measurementUnitText.equals("")) {
                        measurementUnit += measurementUnitText + ";";
                    } else {
                        throw new RuntimeException(String.format("Could not get the translation for UnitDef with OID:\"%s\"", measurementUnitDef.getOID()));
                    }
                }
            }
        }
        if (!measurementUnit.isEmpty()) {
            // Strip the last semicolon
            measurementUnit = measurementUnit.substring(0, measurementUnit.length() - 1);
        }

        return measurementUnit;
    }

    /**
     * Returns a comma delimited list sorted by ordernumbers of the coded values
     * of the codelistitems contained in the given codelist. Numeric values will
     * be parsed to remove leading zeroes.
     *
     * @param codeList The codelist, of wich the coded values will be returned.
     * Must not be <code>null</code>.
     * @return The comma delimited string of the coded values of the
     * codelistitems contained in the given codelist.
     */
    public static String getCodedValues(ODMcomplexTypeDefinitionCodeList codeList) {
        if (codeList != null) {
            CLDataType codeListDataType = codeList.getDataType();
            List<ODMcomplexTypeDefinitionCodeListItem> codeListItems = codeList.getCodeListItem();
            Collections.sort(codeListItems, new ODMComplexTypeDefinitionCodeListItemComparator());
            String codedValues = "";
            // Build comma delimited list of the codedvalues
            for (ODMcomplexTypeDefinitionCodeListItem codelistItem : codeListItems) { //iterate over all choices
                switch (codeListDataType) {
                    case INTEGER:
                        codedValues = codedValues.concat(Integer.parseInt(codelistItem.getCodedValue()) + ",");
                        break;
                    case FLOAT:
                        codedValues = codedValues.concat(Float.parseFloat(codelistItem.getCodedValue()) + ",");
                        break;
                    default:
                        codedValues = codedValues.concat(codelistItem.getCodedValue() + ",");
                        break;
                }
            }
            // Strip the last comma
            return codedValues.substring(0, codedValues.length() - 1);
        } else {
            throw new IllegalArgumentException("The codelist was null.");
        }
    }

    /**
     * The method sorts the list of ODMcomplexTypeDefinitionItemGroupRef by
     * order numbers and then assigns each itemGroupRef its index as new order
     * number. ODMcomplexTypeDefinitionItemGroupRef without specified order
     * number will be sorted to the end of the list.
     *
     * @param itemGroupList The list of ODMcomplexTypeDefinitionItemGroupRef
     * which will be sorted.
     */
    public static void correctItemGroupRefOrderNumbers(List<ODMcomplexTypeDefinitionItemGroupRef> itemGroupList) {
        int itemIndex = 1;
        Collections.sort(itemGroupList, new ODMComplexTypeDefinitionItemGroupRefComparator());
        for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : itemGroupList) {
            itemGroupRef.setOrderNumber(BigInteger.valueOf(itemIndex));
            itemIndex++;
        }
    }

    /**
     * The method sorts the list of ODMcomplexTypeDefinitionItemRef by order
     * numbers and then assigns each itemRef its index as new order number.
     * ODMcomplexTypeDefinitionItemRef without specified order number will be
     * sorted to the end of the list.
     *
     * @param itemList The list of ODMcomplexTypeDefinitionItemRef which will be
     * sorted.
     */
    public static void correctItemRefOrderNumbers(List<ODMcomplexTypeDefinitionItemRef> itemList) {
        int itemIndex = 1;
        Collections.sort(itemList, new ODMComplexTypeDefinitionItemRefComparator());
        for (ODMcomplexTypeDefinitionItemRef itemRef : itemList) {
            itemRef.setOrderNumber(BigInteger.valueOf(itemIndex));
            itemIndex++;
        }
    }

    /**
     * The method sorts the list of ODMcomplexTypeDefinitionCodeListItem by
     * order numbers and then assigns each codeListItem its index as new order
     * number. ODMcomplexTypeDefinitionCodeListItem without specified order
     * number will be sorted to the end of the list.
     *
     * @param codeListItemList The list of ODMcomplexTypeDefinitionCodeListItem
     * which will be sorted.
     */
    public static void correctCodeListItemOrderNumbers(List<ODMcomplexTypeDefinitionCodeListItem> codeListItemList) {
        int itemIndex = 1;
        Collections.sort(codeListItemList, new ODMComplexTypeDefinitionCodeListItemComparator());
        for (ODMcomplexTypeDefinitionCodeListItem codeListItem : codeListItemList) {
            codeListItem.setOrderNumber(BigInteger.valueOf(itemIndex));
            itemIndex++;
        }
    }

    /**
     * Returns the minimum and maximum value that will be valid foran itemdef
     * using the rangeChecks inside the rangeCheckList.
     *
     * @param rangeCheckList The List of range checks that will be searched for
     * the minumum and maximum value. Must not be <code>null</code>.
     * @return minAndMax A Double[] the 0th index is the minimum value and the
     * 1st index is the maximum value.
     * @throws NullPointerException If the parameter rangeCheckList is
     * <code>null</code>.
     */
    public static RangeCheck getMinMaxFromRangeCheck(List<ODMcomplexTypeDefinitionRangeCheck> rangeCheckList) throws NullPointerException {
        RangeCheck minAndMax = new RangeCheck();
        Double min = null;
        Double max = null;
        //Save which range checks are the ones that are returned in the final range check
        ODMcomplexTypeDefinitionErrorMessage errorMessageFromMinRangeCheck = null;
        ODMcomplexTypeDefinitionErrorMessage errorMessageFromMaxRangeCheck = null;

        if (rangeCheckList != null && rangeCheckList.isEmpty() == false) {
            for (ODMcomplexTypeDefinitionRangeCheck rangeCheck : rangeCheckList) {
                LOGGER.debug("Now checking RangeCheck at position {}", rangeCheckList.indexOf(rangeCheck));
                de.unimuenster.imi.org.cdisc.odm.v132.Comparator comparator = rangeCheck.getComparator();
                if (comparator == null) {
                    LOGGER.debug("RangeCheck at position {} did not contain a Comparator. Since this is necessary, the RangeCheck will be skipped.", rangeCheckList.indexOf(rangeCheck));
                } else {
                    LOGGER.debug("RangeCheck at position {} contains a Comparator. Switching over it.", rangeCheckList.indexOf(rangeCheck));
                    switch (comparator) {
                        case GE:
                        case GT:
                            if (minAndMax.getMinimum() == null) {
                                LOGGER.debug("RangeCheck comparator {} detected. Checking for 'CheckValue'", comparator);
                                List<ODMcomplexTypeDefinitionCheckValue> checkValueList = rangeCheck.getCheckValue();
                                if (checkValueList == null || checkValueList.isEmpty()) {
                                    LOGGER.debug("RangeCheck at position {} does not contain any CheckValue elements. Will skip it.", rangeCheckList.indexOf(rangeCheck));
                                } else {
                                    LOGGER.debug("RangeCheck at position {} contains at least one CheckValue. Iterating over it.", rangeCheckList.indexOf(rangeCheck));
                                    for (ODMcomplexTypeDefinitionCheckValue checkValue : checkValueList) {
                                        LOGGER.debug("Now checking the CheckValue element at position {}.", checkValueList.indexOf(checkValue));
                                        try {
                                            LOGGER.debug("Trying to parse the xml value ('{}') into an int", checkValue.getValue());
                                            minAndMax.setMinimum(Double.parseDouble(checkValue.getValue()));
                                            errorMessageFromMinRangeCheck = rangeCheck.getErrorMessage();
                                            if (comparator.equals(de.unimuenster.imi.org.cdisc.odm.v132.Comparator.GE)) {
                                                minAndMax.setLowerBoundaryIncluded(Boolean.TRUE);
                                            } else {
                                                minAndMax.setLowerBoundaryIncluded(Boolean.FALSE);
                                            }
                                            LOGGER.debug("Parsing successful. Min value is: {}", min);
                                            break;
                                        } catch (NumberFormatException nfe) {
                                            LOGGER.debug("NumberFormatException when trying to parse the CheckValue at position " + checkValueList.indexOf(checkValue) + " of RangeCheck at Position: {}", rangeCheckList.indexOf(rangeCheck), nfe);
                                        }
                                    }
                                }
                            } else {
                                LOGGER.debug("RangeCheck at position {} has the comparator {}, but min value is already set. Won't consider it.", rangeCheckList.indexOf(rangeCheck), comparator);
                            }
                            break;
                        case LE:
                        case LT:
                            if (minAndMax.getMaximum() == null) {
                                LOGGER.debug("RangeCheck comparator {} detected. Checking for 'CheckValue'", comparator);
                                List<ODMcomplexTypeDefinitionCheckValue> checkValueList = rangeCheck.getCheckValue();
                                if (checkValueList == null || checkValueList.isEmpty()) {
                                    LOGGER.debug("RangeCheck at position {} does not contain any CheckValue elements. Will skip it.", rangeCheckList.indexOf(rangeCheck));
                                } else {
                                    LOGGER.debug("RangeCheck at position {} contains at least one CheckValue. Iterating over it.", rangeCheckList.indexOf(rangeCheck));
                                    for (ODMcomplexTypeDefinitionCheckValue checkValue : checkValueList) {
                                        LOGGER.debug("Now checking the CheckValue element at position {}.", checkValueList.indexOf(checkValue));
                                        try {
                                            LOGGER.debug("Trying to parse the xml value ('{}') into an int", checkValue.getValue());
                                            minAndMax.setMaximum(Double.parseDouble(checkValue.getValue()));
                                            errorMessageFromMaxRangeCheck =  rangeCheck.getErrorMessage();
                                            if (comparator.equals(de.unimuenster.imi.org.cdisc.odm.v132.Comparator.LE)) {
                                                minAndMax.setUpperBoundaryIncluded(Boolean.TRUE);
                                            } else {
                                                minAndMax.setUpperBoundaryIncluded(Boolean.FALSE);
                                            }
                                            LOGGER.debug("Parsing successful. Max value is: {}", max);
                                            break;
                                        } catch (NumberFormatException nfe) {
                                            LOGGER.debug("NumberFormatException when trying to parse the CheckValue at position " + checkValueList.indexOf(checkValue) + "of RangeCheck at Position: {}", rangeCheckList.indexOf(rangeCheck), nfe);
                                        }
                                    }
                                }
                            } else {
                                LOGGER.debug("RangeCheck at position {} has the comparator {}, but max value is already set. Won't consider it.", rangeCheckList.indexOf(rangeCheck), comparator);
                            }
                            break;
                        default: {
                            LOGGER.debug("RangeCheck at position {} contains a Comparator I don't support ({}).", rangeCheckList.indexOf(rangeCheck), comparator);
                            break;
                        }
                    }
                }
            }// [bt] finish with searching for min and max values
            if (minAndMax.getMinimum() != null && minAndMax.getMaximum() != null && minAndMax.getMaximum() <= minAndMax.getMinimum()) {
                LOGGER.debug("Min and Max were found, but max ({}) was not > than min ({}).");
                minAndMax.setMaximum(null);
                minAndMax.setMinimum(null);
            }  
                //Combining 2 Errormessages to one.
                String errorMessageText = null;
                if (errorMessageFromMinRangeCheck != null && errorMessageFromMaxRangeCheck != null) {
                    errorMessageText = ODMLibraryUtilities.getBestFittingTranslation(errorMessageFromMinRangeCheck.getTranslatedText(), "en").entrySet().iterator().next().getValue() + ". " + ODMLibraryUtilities.getBestFittingTranslation(errorMessageFromMaxRangeCheck.getTranslatedText(), "en").entrySet().iterator().next().getValue() + ".";
                } else if (errorMessageFromMinRangeCheck != null) {
                    errorMessageText = ODMLibraryUtilities.getBestFittingTranslation(errorMessageFromMinRangeCheck.getTranslatedText(), "en").entrySet().iterator().next().getValue() + ". ";
                } else if (errorMessageFromMaxRangeCheck != null) {
                    errorMessageText = ODMLibraryUtilities.getBestFittingTranslation(errorMessageFromMaxRangeCheck.getTranslatedText(), "en").entrySet().iterator().next().getValue() + ". ";
                }
                minAndMax.setValidationErrorMessage(errorMessageText);
            

        } else {
            throw new NullPointerException("The parameter rangeCheckList was null.");
        }
        return minAndMax;
    }
}
