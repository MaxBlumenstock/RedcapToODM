package de.imi.odmtoolbox.model;

/**
 * This class represents a parsed range check for the combination of operators
 * 'lesser than' or 'lesser eauals' and 'greater than' or 'greater equals'. So
 * it represents an interval of
 */
public class RangeCheck {

    private Double minimum;
    private Double maximum;
    private Boolean lowerBoundaryIncluded;
    private Boolean upperBoundaryIncluded;
    private String validationErrorMessage;

    public RangeCheck() {
    }

    public RangeCheck(Double minimum, Double maximum, Boolean lowerBoundaryIncluded, Boolean upperBoundaryIncluded) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerBoundaryIncluded = lowerBoundaryIncluded;
        this.upperBoundaryIncluded = upperBoundaryIncluded;
    }
    
        public RangeCheck(Double minimum, Double maximum, Boolean lowerBoundaryIncluded, Boolean upperBoundaryIncluded, String validationErrorMessage) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.lowerBoundaryIncluded = lowerBoundaryIncluded;
        this.upperBoundaryIncluded = upperBoundaryIncluded;
        this.validationErrorMessage = validationErrorMessage;
    }

    /**
     * The minimum represents the lower boundary of the range check interval.
     *
     * @return Returns the lower boundary of this range check.
     */
    public Double getMinimum() {
        return minimum;
    }

    /**
     * Sets the new lower boundary for this range check interval.
     *
     * @param minimum The new lower boundary for this range check interval.
     */
    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    /**
     * The maximum represents the lower boundary of the range check interval.
     *
     * @return Returns the upper boundary of this range check.
     */
    public Double getMaximum() {
        return maximum;
    }

    /**
     * Sets the new upper boundary for this range check interval.
     *
     * @param maximum The new upper boundary for this range check interval.
     */
    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    /**
     * This flag indicates if the lower boundary is included in the interval or
     * not.
     *
     * @return True if the lower boundary is included in the interval, otherwise
     * false.
     */
    public Boolean getLowerBoundaryIncluded() {
        return lowerBoundaryIncluded;
    }

    /**
     * Sets the new flag, which indicates if the lower boundary is included in
     * the interval or not.
     *
     * @param lowerBoundaryIncluded The new flag for the inclusion of the lower
     * boundary.
     */
    public void setLowerBoundaryIncluded(Boolean lowerBoundaryIncluded) {
        this.lowerBoundaryIncluded = lowerBoundaryIncluded;
    }

    /**
     * This flag indicates if the upper boundary is included in the interval or
     * not.
     *
     * @return True if the upper boundary is included in the interval, otherwise
     * false.
     */
    public Boolean getUpperBoundaryIncluded() {
        return upperBoundaryIncluded;
    }

    /**
     * Sets the new flag, which indicates if the upper boundary is included in
     * the interval or not.
     *
     * @param upperBoundaryIncluded The new flag for the inclusion of the upper
     * boundary.
     */
    public void setUpperBoundaryIncluded(Boolean upperBoundaryIncluded) {
        this.upperBoundaryIncluded = upperBoundaryIncluded;
    }

    /**
     * The validationErrorMessage is the message that will be shown if a value
     * does not meet the constraints of the range check.
     *
     * @return Returns the error message of the range check.
     */
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    /**
     * Sets the validationErrorMessage. This is the message that will be shown
     * if a value does not meet the constraints of the range check.
     *
     * @param validationErrorMessage The text of the error message.
     */
    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }
}
