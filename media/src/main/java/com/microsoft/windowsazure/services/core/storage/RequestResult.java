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

package com.microsoft.windowsazure.services.core.storage;

import java.util.Date;

/**
 * Represents the result of a physical request.
 */
public final class RequestResult
{

    /**
     * Represents an exception that occurred while processing the request. This
     * field may be <code>null</code>.
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
     * @return the contentMD5
     */
    public String getContentMD5()
    {
        return this.contentMD5;
    }

    /**
     * @return the etag
     */
    public String getEtag()
    {
        return this.etag;
    }

    /**
     * @return the exception
     */
    public Exception getException()
    {
        return this.exception;
    }

    /**
     * @return the requestDate
     */
    public String getRequestDate()
    {
        return this.requestDate;
    }

    /**
     * @return the serviceRequestID
     */
    public String getServiceRequestID()
    {
        return this.serviceRequestID;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate()
    {
        return this.startDate;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode()
    {
        return this.statusCode;
    }

    /**
     * @return the statusMessage
     */
    public String getStatusMessage()
    {
        return this.statusMessage;
    }

    /**
     * @return the stopDate
     */
    public Date getStopDate()
    {
        return this.stopDate;
    }

    /**
     * @param contentMD5
     *            the contentMD5 to set
     */
    public void setContentMD5(final String contentMD5)
    {
        this.contentMD5 = contentMD5;
    }

    /**
     * @param etag
     *            the etag to set
     */
    public void setEtag(final String etag)
    {
        this.etag = etag;
    }

    /**
     * @param exception
     *            the exception to set
     */
    public void setException(final Exception exception)
    {
        this.exception = exception;
    }

    /**
     * @param requestDate
     *            the date to set
     */
    public void setRequestDate(final String requestDate)
    {
        this.requestDate = requestDate;
    }

    /**
     * @param serviceRequestID
     *            the serviceRequestID to set
     */
    public void setServiceRequestID(final String serviceRequestID)
    {
        this.serviceRequestID = serviceRequestID;
    }

    /**
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate(final Date startDate)
    {
        this.startDate = startDate;
    }

    /**
     * @param statusCode
     *            the statusCode to set
     */
    public void setStatusCode(final int statusCode)
    {
        this.statusCode = statusCode;
    }

    /**
     * @param statusMessage
     *            the statusMessage to set
     */
    public void setStatusMessage(final String statusMessage)
    {
        this.statusMessage = statusMessage;
    }

    /**
     * @param stopDate
     *            the stopDate to set
     */
    public void setStopDate(final Date stopDate)
    {
        this.stopDate = stopDate;
    }
}
