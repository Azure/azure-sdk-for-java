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

package com.microsoft.azure.storage;

import java.util.Date;

/**
 * Represents the result of a physical request.
 */
public final class RequestResult {

    /**
     * Represents an exception that occurred while processing the request. This field may be <code>null</code>.
     */
    private Exception exception;

    /**
     * Represents the request ID supplied by the storage service.
     */
    private String serviceRequestID;

    /**
     * Represents the ContentMD5 header returned by the storage service.
     */
    private String contentMD5;

    /**
     * Represents the date header returned by the storage service.
     */
    private String requestDate;

    /**
     * Represents the ETag header returned by the storage service.
     */
    private String etag;

    /**
     * Represents the start date of the operation.
     */
    private Date startDate;

    /**
     * Represents the HTTP status code for the request.
     */
    private int statusCode = -1;

    /**
     * Represents the HTTP status message for the request.
     */
    private String statusMessage;

    /**
     * Represents the stop date of the operation.
     */
    private Date stopDate;

    /**
     * Location that the request was sent to.
     */
    private StorageLocation targetLocation;

    /**
     * Gets the location that the request was sent to.
     * 
     * @return A {@link StorageLocation} object.
     */
    public StorageLocation getTargetLocation() {
        return this.targetLocation;
    }

    /**
     * Gets the MD5 hash for the request.
     * 
     * @return A <code>String</code> which contains the MD5 hash.
     */
    public String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * Gets the ETag for the request.
     * 
     * @return A <code>String</code> which contains the ETag.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the <code>Exception</code> for the request.
     * 
     * @return An <code>Exception</code>.
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * Gets the request date.
     * 
     * @return A <code>String</code> which contains the date of the request.
     */
    public String getRequestDate() {
        return this.requestDate;
    }

    /**
     * Gets the service request ID.
     * 
     * @return A <code>String</code> which contains the service request ID.
     */
    public String getServiceRequestID() {
        return this.serviceRequestID;
    }

    /**
     * Gets the start date for the request.
     * 
     * @return A <code>java.util.Date</code> object which contains the start date.
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Gets the HTTP status code for the request.
     * 
     * @return An <code>int</code> which contains the HTTP status code.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the HTTP status message for the request.
     * 
     * @return A <code>String</code> which contains the HTTP status message.
     */
    public String getStatusMessage() {
        return this.statusMessage;
    }

    /**
     * Gets the stop date for the request.
     * 
     * @return A <code>java.util.Date</code> object which contains the stop date.
     */
    public Date getStopDate() {
        return this.stopDate;
    }

    /**
     * Sets the MD5 hash for the request.
     * 
     * @param contentMD5
     *            A <code>String</code> object which contains the MD5 hash to set.
     */
    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * Sets the ETag for the request.
     * 
     * @param etag
     *            A <code>String</code> object which contains the ETag to set.
     */
    public void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the <code>Exception</code> for the request.
     * 
     * @param exception
     *            The <code>Exception</code> to set.
     */
    public void setException(final Exception exception) {
        this.exception = exception;
    }

    /**
     * Sets the request date.
     * 
     * @param requestDate
     *            A <code>java.util.Date</code> object which contains the request date to set.
     */
    public void setRequestDate(final String requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Sets the service request ID.
     * 
     * @param serviceRequestID
     *            A <code>String</code> object which contains the service request ID to set.
     */
    public void setServiceRequestID(final String serviceRequestID) {
        this.serviceRequestID = serviceRequestID;
    }

    /**
     * Sets the start date for the request.
     * 
     * @param startDate
     *            A <code>java.util.Date</code> object which contains the start date to set.
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Sets the HTTP status code for the request.
     * 
     * @param statusCode
     *            An <code>int</code> which contains the HTTP status code to set.
     */
    public void setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Sets the HTTP status message for the request.
     * 
     * @param statusMessage
     *            A <code>String</code> which contains the status message to set.
     */
    public void setStatusMessage(final String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Sets the stop date for the request.
     * 
     * @param stopDate
     *            A <code>java.util.Date</code> object which contains the stop date to set.
     */
    public void setStopDate(final Date stopDate) {
        this.stopDate = stopDate;
    }

    /**
     * Sets the location that the request was sent to.
     * 
     * @param targetLocation
     *            A {@link StorageLocation} object to set.
     */
    public void setTargetLocation(StorageLocation targetLocation) {
        this.targetLocation = targetLocation;
    }
}
