/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus.implementation;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

//
// members of this class defined per specification at
// http://msdn.microsoft.com/en-us/library/windowsazure/hh367521.aspx#BKMK_REST3prod
//

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class BrokerProperties {

    @JsonProperty("CorrelationId")
    private String correlationId;

    @JsonProperty("SessionId")
    private String sessionId;

    @JsonProperty("PartitionKey")
    private String partitionKey;

    @JsonProperty("ViaPartitionKey")
    private String viaPartitionKey;

    @JsonProperty("DeliveryCount")
    private Integer deliveryCount;

    @JsonProperty("LockedUntilUtc")
    private Date lockedUntilUtc;

    @JsonProperty("LockToken")
    private String lockToken;

    @JsonProperty("MessageId")
    private String messageId;

    @JsonProperty("Label")
    private String label;

    @JsonProperty("ReplyTo")
    private String replyTo;

    @JsonProperty("SequenceNumber")
    private Long sequenceNumber;

    @JsonProperty("TimeToLive")
    private Double timeToLive;

    @JsonProperty("To")
    private String to;

    @JsonProperty("ScheduledEnqueueTimeUtc")
    private Date scheduledEnqueueTimeUtc;

    @JsonProperty("ReplyToSessionId")
    private String replyToSessionId;

    @JsonProperty("MessageLocation")
    private String messageLocation;

    @JsonProperty("LockLocation")
    private String lockLocation;

    @JsonIgnore
    public Integer getDeliveryCount() {
        return deliveryCount;
    }

    public void setDeliveryCount(Integer deliveryCount) {
        this.deliveryCount = deliveryCount;
    }

    @JsonIgnore
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @JsonIgnore
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @JsonIgnore
    public Double getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Double timeToLive) {
        this.timeToLive = timeToLive;
    }

    @JsonIgnore
    public String getLockToken() {
        return lockToken;
    }

    public void setLockToken(String lockToken) {
        this.lockToken = lockToken;
    }

    @JsonIgnore
    public Date getLockedUntilUtc() {
        return lockedUntilUtc;
    }

    public void setLockedUntilUtc(Date lockedUntilUtc) {
        this.lockedUntilUtc = lockedUntilUtc;
    }

    @JsonIgnore
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @JsonIgnore
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @JsonIgnore
    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    @JsonIgnore
    public String getViaPartitionKey() {
        return partitionKey;
    }

    public void setViaPartitionKey(String viaPartitionKey) {
        this.viaPartitionKey = viaPartitionKey;
    }

    @JsonIgnore
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonIgnore
    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    @JsonIgnore
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @JsonIgnore
    public Date getScheduledEnqueueTimeUtc() {
        return scheduledEnqueueTimeUtc;
    }

    public void setScheduledEnqueueTimeUtc(Date scheduledEnqueueTimeUtc) {
        this.scheduledEnqueueTimeUtc = scheduledEnqueueTimeUtc;
    }

    @JsonIgnore
    public String getReplyToSessionId() {
        return replyToSessionId;
    }

    public void setReplyToSessionId(String replyToSessionId) {
        this.replyToSessionId = replyToSessionId;
    }

    @JsonIgnore
    public String getMessageLocation() {
        return messageLocation;
    }

    public void setMessageLocation(String messageLocation) {
        this.messageLocation = messageLocation;
    }

    @JsonIgnore
    public String getLockLocation() {
        return lockLocation;
    }

    public void setLockLocation(String lockLocation) {
        this.lockLocation = lockLocation;
    }

}
