package de.imi.odmtoolbox.convert.converter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class FormalExpressionRepairer {

    @RequiredArgsConstructor
    @Getter
    public enum Format {
        OPEN_EDC("OpenEDC (javascript)"), REDCAP ("Redcap ODM (keep redcap style)");

        private final String asString;
        public static Format ofKey(String key) {
            for(Format format : values() ) {
                if (format.asString.equals(key)) {
                    return format;
                }
            }
            return null;
        }
    }

    private final Format format;

    public FormalExpressionRepairer(Format format){
        this.format = format;
    }

    public String repairExpression(String expression) {
        switch (format) {
            case OPEN_EDC:
                return repairForOpenEDC(expression);
            case REDCAP:
                return repairForRedcap(expression);
            default:
                return expression;
        }
    }

    protected abstract String repairForOpenEDC(String expression);
    protected abstract String repairForRedcap(String expression);


}
