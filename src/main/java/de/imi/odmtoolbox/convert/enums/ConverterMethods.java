package de.imi.odmtoolbox.convert.enums;

import java.util.Arrays;
import java.util.List;

public class ConverterMethods {
    public enum MetaData implements ConvertMethodEnumMarker{
        FIELD_NOTES_TO_COMMENTS,
        CONVERT_REPEATING_INSTRUMENTS,
        CONVERT_MULTIPLE_ITEMS,
        ADD_LANGUAGE_INFORMATION,
        ADD_PROTOCOL,
        CONVERT_REDCAP_CALCULATIONS,
        CONVERT_REDCAP_LOGIC_BRANCHES,
        BOOLEAN_TO_INTEGER,
        STORE_REDCAP_DATA_IN_FILE,
        REMOVE_REDUNDANT_REDCAT_ATTRIBUTES,
        CLEAN_REDCAP_TAGS,
        COMPLETE_ITEM_NAMES
    }
    public enum ClinicalData implements ConvertMethodEnumMarker {
        STUDY_EVENT_DATA_TO_SUBJECTS,
        FIX_REPEAT_KEYS,
        FIX_IG_DATA,
        REMOVE_REDUNDANT_REDCAT_ATTRIBUTES
    }
}
