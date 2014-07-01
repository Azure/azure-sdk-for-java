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
package com.microsoft.azure.storage.analytics;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a Storage Analytics Log record entry.
 */
public class LogRecord {

    /**
     * Holds the date format for the RequestStartTime field.
     */
    protected static final SimpleDateFormat REQUEST_START_TIME_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");

    /**
     * Holds the date format for the LastModifiedTime field.
     */
    protected static final SimpleDateFormat LAST_MODIFIED_TIME_FORMAT = new SimpleDateFormat(
            "E, dd-MMM-yy HH:mm:ss 'GMT'");

    private String versionNumber;

    private Date requestStartTime;

    private String operationType;

    private String requestStatus;

    private String httpStatusCode;

    private Integer endToEndLatencyInMS;

    private Integer serverLatencyInMS;

    private String authenticationType;

    private String requesterAccountName;

    private String ownerAccountName;

    private String serviceType;

    private URI requestUrl;

    private String requestedObjectKey;

    private UUID requestIdHeader;

    private Integer operationCount;

    private String requesterIPAddress;

    private String requestVersionHeader;

    private Long requestHeaderSize;

    private Long requestPacketSize;

    private Long responseHeaderSize;

    private Long responsePacketSize;

    private Long requestContentLength;

    private String requestMD5;

    private String serverMD5;

    private String eTagIdentifier;

    private Date lastModifiedTime;

    private String conditionsUsed;

    private String userAgentHeader;

    private String referrerHeader;

    private String clientRequestId;

    /**
     * Initializes a new instance of the LogRecord class.
     */
    protected LogRecord() {
    }

    /**
     * Initializes a new instance of the LogRecord class using a LogRecordStreamReader to populate.
     * 
     * @param reader
     *            the LogRecordStreamReader to use to populate the LogRecord.
     * @throws IOException
     * @throws ParseException
     * @throws URISyntaxException
     */
    protected LogRecord(LogRecordStreamReader reader) throws IOException, ParseException, URISyntaxException {
        LAST_MODIFIED_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        REQUEST_START_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        
    	Utility.assertNotNull("reader", reader);
        this.versionNumber = reader.readString();
        Utility.assertNotNullOrEmpty("versionNumber", this.versionNumber);

        if (this.versionNumber.equals("1.0")) {
            this.populateVersion1Log(reader);
        }
        else {
            throw new IllegalArgumentException(String.format(SR.LOG_VERSION_UNSUPPORTED, this.versionNumber));
        }
    }

    /**
     * Populates a LogRecord using the Version 1.0 schema and the given LogRecordStreamReader.
     * 
     * @param reader
     *            the LogRecordStreamReader to use to populate the LogRecord.
     * @throws IOException
     * @throws ParseException
     * @throws URISyntaxException
     */
    private void populateVersion1Log(LogRecordStreamReader reader) throws IOException, ParseException,
            URISyntaxException {
        this.requestStartTime = reader.readDate(LogRecord.REQUEST_START_TIME_FORMAT);
        this.operationType = reader.readString();
        this.requestStatus = reader.readString();
        this.httpStatusCode = reader.readString();
        this.endToEndLatencyInMS = reader.readInteger();
        this.serverLatencyInMS = reader.readInteger();
        this.authenticationType = reader.readString();
        this.requesterAccountName = reader.readString();
        this.ownerAccountName = reader.readString();
        this.serviceType = reader.readString();
        this.requestUrl = reader.readUri();
        this.requestedObjectKey = reader.readQuotedString();
        this.requestIdHeader = reader.readUuid();
        this.operationCount = reader.readInteger();
        this.requesterIPAddress = reader.readString();
        this.requestVersionHeader = reader.readString();
        this.requestHeaderSize = reader.readLong();
        this.requestPacketSize = reader.readLong();
        this.responseHeaderSize = reader.readLong();
        this.responsePacketSize = reader.readLong();
        this.requestContentLength = reader.readLong();
        this.requestMD5 = reader.readQuotedString();
        this.serverMD5 = reader.readQuotedString();
        this.eTagIdentifier = reader.readQuotedString();
        this.lastModifiedTime = reader.readDate(LogRecord.LAST_MODIFIED_TIME_FORMAT);
        this.conditionsUsed = reader.readQuotedString();
        this.userAgentHeader = reader.readQuotedString();
        this.referrerHeader = reader.readQuotedString();
        this.clientRequestId = reader.readQuotedString();
        reader.endCurrentRecord();

    }

    /**
     * Gets the version of Storage Analytics Logging used to record the entry.
     * 
     * @return
     *         a <code>String</code> containing the VersionNumber value
     */
    public String getVersionNumber() {
        return this.versionNumber;
    }

    /**
     * Gets the time in UTC when the request was received by Storage Analytics.
     * 
     * @return
     *         a <code>String</code> containing the RequestStartTime value
     */
    public Date getRequestStartTime() {
        return this.requestStartTime;
    }

    /**
     * Gets the type of REST operation performed.
     * 
     * @return
     *         a <code>String</code> containing the OperationType value
     */
    public String getOperationType() {
        return this.operationType;
    }

    /**
     * Gets the status of the requested operation.
     * 
     * @return
     *         a <code>String</code> containing the RequestStatus value
     */
    public String getRequestStatus() {
        return this.requestStatus;
    }

    /**
     * Gets the HTTP status code for the request. If the request is interrupted, this value may be set to Unknown.
     * 
     * @return
     *         a <code>String</code> containing the HttpStatusCode value
     */
    public String getHttpStatusCode() {
        return this.httpStatusCode;
    }

    /**
     * Gets the total time in milliseconds to perform the requested operation, including the time to read the
     * incoming request and send the response to the requester.
     * 
     * @return
     *         an <code>Integer</code> containing the EndToEndLatencyInMS value
     */
    public Integer getEndToEndLatencyInMS() {
        return this.endToEndLatencyInMS;
    }

    /**
     * Gets the total time in milliseconds to perform the requested operation. This value does not include network
     * latency (the time to read the incoming request and send the response to the requester).
     * 
     * @return
     *         an <code>Integer</code> containing the ServerLatencyInMS value
     */
    public Integer getServerLatencyInMS() {
        return this.serverLatencyInMS;
    }

    /**
     * Gets whether the request was authenticated, anonymous, or used Shared Access Signature (SAS).
     * 
     * @return
     *         a <code>String</code> containing the AuthenticationType value
     */
    public String getAuthenticationType() {
        return this.authenticationType;
    }

    /**
     * Gets the account name making the request, if the request is authenticated. This field will be null for
     * anonymous and SAS requests.
     * 
     * @return
     *         a <code>String</code> containing the RequesterAccountName value
     */
    public String getRequesterAccountName() {
        return this.requesterAccountName;
    }

    /**
     * Gets the account name of the service owner.
     * 
     * @return
     *         a <code>String</code> containing the OwnerAccountName value
     */
    public String getOwnerAccountName() {
        return this.ownerAccountName;
    }

    /**
     * Gets the requested storage service: blob, table, or queue.
     * 
     * @return
     *         a <code>String</code> containing the ServiceType value
     */
    public String getServiceType() {
        return this.serviceType;
    }

    /**
     * Gets the complete URL of the request.
     * 
     * @return
     *         a <code>URI</code> containing the RequestUrl value
     */
    public URI getRequestUrl() {
        return this.requestUrl;
    }

    /**
     * Gets the key of the requested object as an encoded string. This field will always use the account name, even
     * if a custom domain name has been configured.
     * 
     * @return
     *         a <code>String</code> containing the RequestedObjectKey value
     */
    public String getRequestedObjectKey() {
        return this.requestedObjectKey;
    }

    /**
     * Gets the request ID assigned by the storage service. This is equivalent to the value of the x-ms-request-id
     * header.
     * 
     * @return
     *         a <code>UUID</code> containing the RequestIdHeader value
     */
    public UUID getRequestIdHeader() {
        return this.requestIdHeader;
    }

    /**
     * Gets the number of each logged operation for a request, using an index of zero. Some requests require more
     * than one operation, such as Copy Blob, though most perform just one operation.
     * 
     * @return
     *         an <code>Integer</code> containing the OperationCount value
     */
    public Integer getOperationCount() {
        return this.operationCount;
    }

    /**
     * Gets the IP address and port of the requester.
     * 
     * @return
     *         a <code>String</code> containing the RequesterIPAddress value
     */
    public String getRequesterIPAddress() {
        return this.requesterIPAddress;
    }

    /**
     * Gets the storage service version specified when the request was made. This is equivalent to the value of the
     * x-ms-version header.
     * 
     * @return
     *         a <code>String</code> containing the RequestVersionHeader value
     */
    public String getRequestVersionHeader() {
        return this.requestVersionHeader;
    }

    /**
     * Gets the size of the request header, in bytes. If a request is unsuccessful, this value may be null.
     * 
     * @return
     *         a <code>Long</code> containing the RequestHeaderSize value
     */
    public Long getRequestHeaderSize() {
        return this.requestHeaderSize;
    }

    /**
     * Gets the size of the request packets read by the storage service, in bytes. If a request is unsuccessful, this
     * value may be null.
     * 
     * @return
     *         a <code>Long</code> containing the RequestPacketSize value
     */
    public Long getRequestPacketSize() {
        return this.requestPacketSize;
    }

    /**
     * Gets the size of the response header, in bytes. If a request is unsuccessful, this value may be null.
     * 
     * @return
     *         a <code>Long</code> containing the ResponseHeaderSize value
     */
    public Long getResponseHeaderSize() {
        return this.responseHeaderSize;
    }

    /**
     * Gets the size of the response packets written by the storage service, in bytes. If a request is unsuccessful,
     * this value may be null.
     * 
     * @return
     *         a <code>Long</code> containing the ResponsePacketSize value
     */
    public Long getResponsePacketSize() {
        return this.responsePacketSize;
    }

    /**
     * Gets the value of the Content-Length header for the request sent to the storage service. If the request was
     * successful, this value is equal to request-packet-size. If a request is unsuccessful, this value may not
     * be equal to request-packet-size, or it may be null.
     * 
     * @return
     *         a <code>Long</code> containing the RequestContentLength value
     */
    public Long getRequestContentLength() {
        return this.requestContentLength;
    }

    /**
     * Gets the value of either the Content-MD5 header or the x-ms-content-md5 header in the request as an encoded
     * string. The MD5 hash value specified in this field represents the content in the request. This field can
     * be null.
     * 
     * @return
     *         an encoded <code>String</code> containing the RequestMD5 value
     */
    public String getRequestMD5() {
        return this.requestMD5;
    }

    /**
     * Gets the value of the MD5 hash calculated by the storage service as an encoded string.
     * 
     * @return
     *         an encoded <code>String</code> containing the ServerMD5 value
     */
    public String getServerMD5() {
        return this.serverMD5;
    }

    /**
     * Gets the ETag identifier for the returned object as an encoded string.
     * 
     * @return
     *         an encoded <code>String</code> containing the ETagIdentifier value
     */
    public String getETagIdentifier() {
        return this.eTagIdentifier;
    }

    /**
     * Gets the Last Modified Time (LMT) for the returned object as an encoded string. This field is null for
     * operations that can return multiple objects.
     * 
     * @return
     *         a <code>Date</code> containing the LastModifiedTime value
     */
    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    /**
     * Gets conditions used, as an encoded string semicolon-separated list in the form of ConditionName=value .
     * 
     * @return
     *         an encoded <code>String</code> containing the ConditionsUsed value
     */
    public String getConditionsUsed() {
        return this.conditionsUsed;
    }

    /**
     * Gets the User-Agent header value as an encoded string.
     * 
     * @return
     *         an encoded <code>String</code> containing the UserAgentHeader value
     */
    public String getUserAgentHeader() {
        return this.userAgentHeader;
    }

    /**
     * Gets the Referrer header value as an encoded string.
     * 
     * @return
     *         an encoded <code>String</code> containing the ReferrerHeader value
     */
    public String getReferrerHeader() {
        return this.referrerHeader;
    }

    /**
     * Gets the x-ms-client-request-id header value included in the request.
     * 
     * @return
     *         an encoded <code>String</code> containing the ClientRequestId value
     */
    public String getClientRequestId() {
        return this.clientRequestId;
    }

    /**
     * @param versionNumber
     *            the versionNumber to set
     */
    protected void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    /**
     * @param requestStartTime
     *            the requestStartTime to set
     */
    protected void setRequestStartTime(Date requestStartTime) {
        this.requestStartTime = requestStartTime;
    }

    /**
     * @param operationType
     *            the operationType to set
     */
    protected void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * @param requestStatus
     *            the requestStatus to set
     */
    protected void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     * @param httpStatusCode
     *            the httpStatusCode to set
     */
    protected void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * @param endToEndLatencyInMS
     *            the endToEndLatencyInMS to set
     */
    protected void setEndToEndLatencyInMS(Integer endToEndLatencyInMS) {
        this.endToEndLatencyInMS = endToEndLatencyInMS;
    }

    /**
     * @param serverLatencyInMS
     *            the serverLatencyInMS to set
     */
    protected void setServerLatencyInMS(Integer serverLatencyInMS) {
        this.serverLatencyInMS = serverLatencyInMS;
    }

    /**
     * @param authenticationType
     *            the authenticationType to set
     */
    protected void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    /**
     * @param requesterAccountName
     *            the requesterAccountName to set
     */
    protected void setRequesterAccountName(String requesterAccountName) {
        this.requesterAccountName = requesterAccountName;
    }

    /**
     * @param ownerAccountName
     *            the ownerAccountName to set
     */
    protected void setOwnerAccountName(String ownerAccountName) {
        this.ownerAccountName = ownerAccountName;
    }

    /**
     * @param serviceType
     *            the serviceType to set
     */
    protected void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * @param requestUrl
     *            the requestUrl to set
     */
    protected void setRequestUrl(URI requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * @param requestedObjectKey
     *            the requestedObjectKey to set
     */
    protected void setRequestedObjectKey(String requestedObjectKey) {
        this.requestedObjectKey = requestedObjectKey;
    }

    /**
     * @param requestIdHeader
     *            the requestIdHeader to set
     */
    protected void setRequestIdHeader(UUID requestIdHeader) {
        this.requestIdHeader = requestIdHeader;
    }

    /**
     * @param operationCount
     *            the operationCount to set
     */
    protected void setOperationCount(Integer operationCount) {
        this.operationCount = operationCount;
    }

    /**
     * @param requesterIPAddress
     *            the requesterIPAddress to set
     */
    protected void setRequesterIPAddress(String requesterIPAddress) {
        this.requesterIPAddress = requesterIPAddress;
    }

    /**
     * @param requestVersionHeader
     *            the requestVersionHeader to set
     */
    protected void setRequestVersionHeader(String requestVersionHeader) {
        this.requestVersionHeader = requestVersionHeader;
    }

    /**
     * @param requestHeaderSize
     *            the requestHeaderSize to set
     */
    protected void setRequestHeaderSize(Long requestHeaderSize) {
        this.requestHeaderSize = requestHeaderSize;
    }

    /**
     * @param requestPacketSize
     *            the requestPacketSize to set
     */
    protected void setRequestPacketSize(Long requestPacketSize) {
        this.requestPacketSize = requestPacketSize;
    }

    /**
     * @param responseHeaderSize
     *            the responseHeaderSize to set
     */
    protected void setResponseHeaderSize(Long responseHeaderSize) {
        this.responseHeaderSize = responseHeaderSize;
    }

    /**
     * @param responsePacketSize
     *            the responsePacketSize to set
     */
    protected void setResponsePacketSize(Long responsePacketSize) {
        this.responsePacketSize = responsePacketSize;
    }

    /**
     * @param requestContentLength
     *            the requestContentLength to set
     */
    protected void setRequestContentLength(Long requestContentLength) {
        this.requestContentLength = requestContentLength;
    }

    /**
     * @param requestMD5
     *            the requestMD5 to set
     */
    protected void setRequestMD5(String requestMD5) {
        this.requestMD5 = requestMD5;
    }

    /**
     * @param serverMD5
     *            the serverMD5 to set
     */
    protected void setServerMD5(String serverMD5) {
        this.serverMD5 = serverMD5;
    }

    /**
     * @param eTagIdentifier
     *            the eTagIdentifier to set
     */
    protected void setETagIdentifier(String eTagIdentifier) {
        this.eTagIdentifier = eTagIdentifier;
    }

    /**
     * @param lastModifiedTime
     *            the lastModifiedTime to set
     */
    protected void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * @param conditionsUsed
     *            the conditionsUsed to set
     */
    protected void setConditionsUsed(String conditionsUsed) {
        this.conditionsUsed = conditionsUsed;
    }

    /**
     * @param userAgentHeader
     *            the userAgentHeader to set
     */
    protected void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }

    /**
     * @param referrerHeader
     *            the referrerHeader to set
     */
    protected void setReferrerHeader(String referrerHeader) {
        this.referrerHeader = referrerHeader;
    }

    /**
     * @param clientRequestId
     *            the clientRequestId to set
     */
    protected void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }
}
