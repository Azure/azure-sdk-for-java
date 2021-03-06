/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.support.v2020_04_01.implementation;

import com.microsoft.azure.management.support.v2020_04_01.CommunicationType;
import com.microsoft.azure.management.support.v2020_04_01.CommunicationDirection;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.ProxyResource;

/**
 * Object that represents a Communication resource.
 */
@JsonFlatten
public class CommunicationDetailsInner extends ProxyResource {
    /**
     * Communication type. Possible values include: 'web', 'phone'.
     */
    @JsonProperty(value = "properties.communicationType", access = JsonProperty.Access.WRITE_ONLY)
    private CommunicationType communicationType;

    /**
     * Direction of communication. Possible values include: 'inbound',
     * 'outbound'.
     */
    @JsonProperty(value = "properties.communicationDirection", access = JsonProperty.Access.WRITE_ONLY)
    private CommunicationDirection communicationDirection;

    /**
     * Email address of the sender. This property is required if called by a
     * service principal.
     */
    @JsonProperty(value = "properties.sender")
    private String sender;

    /**
     * Subject of the communication.
     */
    @JsonProperty(value = "properties.subject", required = true)
    private String subject;

    /**
     * Body of the communication.
     */
    @JsonProperty(value = "properties.body", required = true)
    private String body;

    /**
     * Time in UTC (ISO 8601 format) when the communication was created.
     */
    @JsonProperty(value = "properties.createdDate", access = JsonProperty.Access.WRITE_ONLY)
    private DateTime createdDate;

    /**
     * Get communication type. Possible values include: 'web', 'phone'.
     *
     * @return the communicationType value
     */
    public CommunicationType communicationType() {
        return this.communicationType;
    }

    /**
     * Get direction of communication. Possible values include: 'inbound', 'outbound'.
     *
     * @return the communicationDirection value
     */
    public CommunicationDirection communicationDirection() {
        return this.communicationDirection;
    }

    /**
     * Get email address of the sender. This property is required if called by a service principal.
     *
     * @return the sender value
     */
    public String sender() {
        return this.sender;
    }

    /**
     * Set email address of the sender. This property is required if called by a service principal.
     *
     * @param sender the sender value to set
     * @return the CommunicationDetailsInner object itself.
     */
    public CommunicationDetailsInner withSender(String sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Get subject of the communication.
     *
     * @return the subject value
     */
    public String subject() {
        return this.subject;
    }

    /**
     * Set subject of the communication.
     *
     * @param subject the subject value to set
     * @return the CommunicationDetailsInner object itself.
     */
    public CommunicationDetailsInner withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get body of the communication.
     *
     * @return the body value
     */
    public String body() {
        return this.body;
    }

    /**
     * Set body of the communication.
     *
     * @param body the body value to set
     * @return the CommunicationDetailsInner object itself.
     */
    public CommunicationDetailsInner withBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Get time in UTC (ISO 8601 format) when the communication was created.
     *
     * @return the createdDate value
     */
    public DateTime createdDate() {
        return this.createdDate;
    }

}
