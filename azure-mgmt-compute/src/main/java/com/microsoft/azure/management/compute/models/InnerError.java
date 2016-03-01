/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;


/**
 * Inner error details.
 */
public class InnerError {
    /**
     * Gets or sets the exception type.
     */
    private String exceptiontype;

    /**
     * Gets or sets the internal error message or exception dump.
     */
    private String errordetail;

    /**
     * Get the exceptiontype value.
     *
     * @return the exceptiontype value
     */
    public String getExceptiontype() {
        return this.exceptiontype;
    }

    /**
     * Set the exceptiontype value.
     *
     * @param exceptiontype the exceptiontype value to set
     */
    public void setExceptiontype(String exceptiontype) {
        this.exceptiontype = exceptiontype;
    }

    /**
     * Get the errordetail value.
     *
     * @return the errordetail value
     */
    public String getErrordetail() {
        return this.errordetail;
    }

    /**
     * Set the errordetail value.
     *
     * @param errordetail the errordetail value to set
     */
    public void setErrordetail(String errordetail) {
        this.errordetail = errordetail;
    }

}
