package de.imi.odmtoolbox.convert.converter;

public class ConditionFormalExpressionRepairer extends FormalExpressionRepairer{

    public ConditionFormalExpressionRepairer(Format format) {
        super(format);
    }

    @Override
    protected String repairForOpenEDC(String expression) {
        expression = expression
                .replaceAll("=", "==")
                .replaceAll("<>", "!=")
                .replaceAll("\\((\\d+)\\)", "___$1")
                .replaceAll("\\[([\\w_]+)\\]", "$1");
        return "!(" + expression + ")";
    }

    @Override
    protected String repairForRedcap(String expression) {
        expression = expression
                .replaceAll("\\((\\d+)\\)", "___$1");
        return "!(" + expression + ")";
    }
}
