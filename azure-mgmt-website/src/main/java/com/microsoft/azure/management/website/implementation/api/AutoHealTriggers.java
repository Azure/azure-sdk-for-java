/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * AutoHealTriggers - describes the triggers for auto-heal.
 */
public class AutoHealTriggers {
    /**
     * Requests - Defines a rule based on total requests.
     */
    private RequestsBasedTrigger requests;

    /**
     * PrivateBytesInKB - Defines a rule based on private bytes.
     */
    private Integer privateBytesInKB;

    /**
     * StatusCodes - Defines a rule based on status codes.
     */
    private List<StatusCodesBasedTrigger> statusCodes;

    /**
     * SlowRequests - Defines a rule based on request execution time.
     */
    private SlowRequestsBasedTrigger slowRequests;

    /**
     * Get the requests value.
     *
     * @return the requests value
     */
    public RequestsBasedTrigger requests() {
        return this.requests;
    }

    /**
     * Set the requests value.
     *
     * @param requests the requests value to set
     * @return the AutoHealTriggers object itself.
     */
    public AutoHealTriggers withRequests(RequestsBasedTrigger requests) {
        this.requests = requests;
        return this;
    }

    /**
     * Get the privateBytesInKB value.
     *
     * @return the privateBytesInKB value
     */
    public Integer privateBytesInKB() {
        return this.privateBytesInKB;
    }

    /**
     * Set the privateBytesInKB value.
     *
     * @param privateBytesInKB the privateBytesInKB value to set
     * @return the AutoHealTriggers object itself.
     */
    public AutoHealTriggers withPrivateBytesInKB(Integer privateBytesInKB) {
        this.privateBytesInKB = privateBytesInKB;
        return this;
    }

    /**
     * Get the statusCodes value.
     *
     * @return the statusCodes value
     */
    public List<StatusCodesBasedTrigger> statusCodes() {
        return this.statusCodes;
    }

    /**
     * Set the statusCodes value.
     *
     * @param statusCodes the statusCodes value to set
     * @return the AutoHealTriggers object itself.
     */
    public AutoHealTriggers withStatusCodes(List<StatusCodesBasedTrigger> statusCodes) {
        this.statusCodes = statusCodes;
        return this;
    }

    /**
     * Get the slowRequests value.
     *
     * @return the slowRequests value
     */
    public SlowRequestsBasedTrigger slowRequests() {
        return this.slowRequests;
    }

    /**
     * Set the slowRequests value.
     *
     * @param slowRequests the slowRequests value to set
     * @return the AutoHealTriggers object itself.
     */
    public AutoHealTriggers withSlowRequests(SlowRequestsBasedTrigger slowRequests) {
        this.slowRequests = slowRequests;
        return this;
    }

}
