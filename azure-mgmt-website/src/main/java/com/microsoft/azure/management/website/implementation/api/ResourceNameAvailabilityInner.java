/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Describes if a resource name is available.
 */
public class ResourceNameAvailabilityInner {
    /**
     * True indicates name is valid and available.  False indicates the name
     * is invalid, unavailable, or both.
     */
    private Boolean nameAvailable;

    /**
     * Required if nameAvailable is false. 'Invalid' indicates the name
     * provided does not match Azure WebApp service’s naming requirements.
     * 'AlreadyExists' indicates that the name is already in use and is
     * therefore unavailable.
     */
    private String reason;

    /**
     * The message property.
     */
    private String message;

    /**
     * Get the nameAvailable value.
     *
     * @return the nameAvailable value
     */
    public Boolean nameAvailable() {
        return this.nameAvailable;
    }

    /**
     * Set the nameAvailable value.
     *
     * @param nameAvailable the nameAvailable value to set
     * @return the ResourceNameAvailabilityInner object itself.
     */
    public ResourceNameAvailabilityInner withNameAvailable(Boolean nameAvailable) {
        this.nameAvailable = nameAvailable;
        return this;
    }

    /**
     * Get the reason value.
     *
     * @return the reason value
     */
    public String reason() {
        return this.reason;
    }

    /**
     * Set the reason value.
     *
     * @param reason the reason value to set
     * @return the ResourceNameAvailabilityInner object itself.
     */
    public ResourceNameAvailabilityInner withReason(String reason) {
        this.reason = reason;
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
     * @return the ResourceNameAvailabilityInner object itself.
     */
    public ResourceNameAvailabilityInner withMessage(String message) {
        this.message = message;
        return this;
    }

}
