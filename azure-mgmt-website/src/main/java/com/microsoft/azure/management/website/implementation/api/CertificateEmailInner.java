/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Certificate Email.
 */
@JsonFlatten
public class CertificateEmailInner extends Resource {
    /**
     * Email id.
     */
    @JsonProperty(value = "properties.emailId")
    private String emailId;

    /**
     * Time stamp.
     */
    @JsonProperty(value = "properties.timeStamp")
    private DateTime timeStamp;

    /**
     * Get the emailId value.
     *
     * @return the emailId value
     */
    public String emailId() {
        return this.emailId;
    }

    /**
     * Set the emailId value.
     *
     * @param emailId the emailId value to set
     * @return the CertificateEmailInner object itself.
     */
    public CertificateEmailInner withEmailId(String emailId) {
        this.emailId = emailId;
        return this;
    }

    /**
     * Get the timeStamp value.
     *
     * @return the timeStamp value
     */
    public DateTime timeStamp() {
        return this.timeStamp;
    }

    /**
     * Set the timeStamp value.
     *
     * @param timeStamp the timeStamp value to set
     * @return the CertificateEmailInner object itself.
     */
    public CertificateEmailInner withTimeStamp(DateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

}
