package com.kindergarten.kindergarten.parent;

public class PayReference {
    private String months;
    private String reference;
    private Integer idinsc;
    private Double amountpermonth;

    /**
     * @return String return the months
     */
    public String getMonths() {
        return months;
    }

    /**
     * @param months the months to set
     */
    public void setMonths(String months) {
        this.months = months;
    }

    /**
     * @return String return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return Integer return the idinsc
     */
    public Integer getIdinsc() {
        return idinsc;
    }

    /**
     * @param idinsc the idinsc to set
     */
    public void setIdinsc(Integer idinsc) {
        this.idinsc = idinsc;
    }

    /**
     * @return Double return the amountpermonth
     */
    public Double getAmountpermonth() {
        return amountpermonth;
    }

    /**
     * @param amountpermonth the amountpermonth to set
     */
    public void setAmountpermonth(Double amountpermonth) {
        this.amountpermonth = amountpermonth;
    }

}
