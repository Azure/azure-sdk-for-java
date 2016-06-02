/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import org.joda.time.DateTime;

/**
 * Instance view status.
 */
public class InstanceViewStatus {
    /**
     * Gets the status Code.
     */
    private String code;

    /**
     * Gets or sets the level Code. Possible values include: 'Info',
     * 'Warning', 'Error'.
     */
    private StatusLevelTypes level;

    /**
     * Gets or sets the short localizable label for the status.
     */
    private String displayStatus;

    /**
     * Gets or sets the detailed Message, including for alerts and error
     * messages.
     */
    private String message;

    /**
     * Gets or sets the time of the status.
     */
    private DateTime time;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String code() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the InstanceViewStatus object itself.
     */
    public InstanceViewStatus withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Get the level value.
     *
     * @return the level value
     */
    public StatusLevelTypes level() {
        return this.level;
    }

    /**
     * Set the level value.
     *
     * @param level the level value to set
     * @return the InstanceViewStatus object itself.
     */
    public InstanceViewStatus withLevel(StatusLevelTypes level) {
        this.level = level;
        return this;
    }

    /**
     * Get the displayStatus value.
     *
     * @return the displayStatus value
     */
    public String displayStatus() {
        return this.displayStatus;
    }

    /**
     * Set the displayStatus value.
     *
     * @param displayStatus the displayStatus value to set
     * @return the InstanceViewStatus object itself.
     */
    public InstanceViewStatus withDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
        return this;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

    /**
     * Set the message value.
     *
     * @param message the message value to set
     * @return the InstanceViewStatus object itself.
     */
    public InstanceViewStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the time value.
     *
     * @return the time value
     */
    public DateTime time() {
        return this.time;
    }

    /**
     * Set the time value.
     *
     * @param time the time value to set
     * @return the InstanceViewStatus object itself.
     */
    public InstanceViewStatus withTime(DateTime time) {
        this.time = time;
        return this;
    }

}
