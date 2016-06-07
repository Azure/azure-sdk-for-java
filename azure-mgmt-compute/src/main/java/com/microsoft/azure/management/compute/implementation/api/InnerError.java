/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


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
    public String exceptiontype() {
        return this.exceptiontype;
    }

    /**
     * Set the exceptiontype value.
     *
     * @param exceptiontype the exceptiontype value to set
     * @return the InnerError object itself.
     */
    public InnerError withExceptiontype(String exceptiontype) {
        this.exceptiontype = exceptiontype;
        return this;
    }

    /**
     * Get the errordetail value.
     *
     * @return the errordetail value
     */
    public String errordetail() {
        return this.errordetail;
    }

    /**
     * Set the errordetail value.
     *
     * @param errordetail the errordetail value to set
     * @return the InnerError object itself.
     */
    public InnerError withErrordetail(String errordetail) {
        this.errordetail = errordetail;
        return this;
    }

}
