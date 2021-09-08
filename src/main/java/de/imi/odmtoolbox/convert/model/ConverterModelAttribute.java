package de.imi.odmtoolbox.convert.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConverterModelAttribute {
    private String language;
    private String multiAnswerStyle;
    private String expressionConversionStyle;
    private String studyEventName;
    private List<String> optionsMetaData;
    private List<String> selectedAnswersMetaData;
    private List<String> optionsClinicalData;
    private List<String> selectedAnswersClinicalData;
    private boolean convertClinicalData = true;
    private boolean repairODM = false;
}

