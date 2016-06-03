/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * StatusCodeBasedTrigger.
 */
public class StatusCodesBasedTrigger {
    /**
     * HTTP status code.
     */
    private Integer status;

    /**
     * SubStatus.
     */
    private Integer subStatus;

    /**
     * Win32 error code.
     */
    private Integer win32Status;

    /**
     * Count.
     */
    private Integer count;

    /**
     * TimeInterval.
     */
    private String timeInterval;

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public Integer status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the StatusCodesBasedTrigger object itself.
     */
    public StatusCodesBasedTrigger withStatus(Integer status) {
        this.status = status;
        return this;
    }

    /**
     * Get the subStatus value.
     *
     * @return the subStatus value
     */
    public Integer subStatus() {
        return this.subStatus;
    }

    /**
     * Set the subStatus value.
     *
     * @param subStatus the subStatus value to set
     * @return the StatusCodesBasedTrigger object itself.
     */
    public StatusCodesBasedTrigger withSubStatus(Integer subStatus) {
        this.subStatus = subStatus;
        return this;
    }

    /**
     * Get the win32Status value.
     *
     * @return the win32Status value
     */
    public Integer win32Status() {
        return this.win32Status;
    }

    /**
     * Set the win32Status value.
     *
     * @param win32Status the win32Status value to set
     * @return the StatusCodesBasedTrigger object itself.
     */
    public StatusCodesBasedTrigger withWin32Status(Integer win32Status) {
        this.win32Status = win32Status;
        return this;
    }

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public Integer count() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     * @return the StatusCodesBasedTrigger object itself.
     */
    public StatusCodesBasedTrigger withCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Get the timeInterval value.
     *
     * @return the timeInterval value
     */
    public String timeInterval() {
        return this.timeInterval;
    }

    /**
     * Set the timeInterval value.
     *
     * @param timeInterval the timeInterval value to set
     * @return the StatusCodesBasedTrigger object itself.
     */
    public StatusCodesBasedTrigger withTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
        return this;
    }

}
