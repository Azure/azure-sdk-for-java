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

package com.microsoft.azure.storage.table;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.core.JsonParseException;
import com.microsoft.azure.storage.RequestResult;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;

/**
 * An exception that results when a table storage service operation fails to complete successfully.
 */
public final class TableServiceException extends StorageException {

    private static final long serialVersionUID = 6037366449663934891L;

    private TableOperation operation;

    /**
     * Reserved for internal use. This flag indicates whether the operation that threw the exception can be retried.
     */
    private boolean retryable = false;

    /**
     * Reserved for internal use. A static factory method to create a {@link TableServiceException} instance using
     * the specified parameters.
     * 
     * @param retryable
     *            <code>true</code> if the table operation can be retried; otherwise, <code>false</code>.
     * @param res
     *            A {@link RequestResult} containing the result of the table storage service operation.
     * @param op
     *            The {@link TableOperation} representing the table operation that caused the exception.
     * @param inStream
     *            The <code>java.io.InputStream</code> of the error response from the table operation request.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing
     * @return
     *         A {@link TableServiceException} instance initialized with values from the input parameters.
     */
    protected static TableServiceException generateTableServiceException(boolean retryable, RequestResult res,
            TableOperation op, InputStream inStream, TablePayloadFormat format) {
        TableServiceException retryableException = new TableServiceException(res.getStatusCode(),
                res.getStatusMessage(), op, new InputStreamReader(inStream), format);
        retryableException.retryable = retryable;

        return retryableException;
    }

    /**
     * Constructs a <code>TableServiceException</code> instance using the specified error code, message, status code,
     * extended error information and inner exception.
     * 
     * @param errorCode
     *            A <code>String</code> that represents the error code returned by the table operation.
     * @param message
     *            A <code>String</code> that represents the error message returned by the table operation.
     * @param statusCode
     *            An <code>int>/code> which represents the HTTP status code returned by the table operation.
     * @param extendedErrorInfo
     *            A {@link StorageExtendedErrorInformation} object that represents the extended error information
     *            returned by the table operation.
     * @param innerException
     *            An <code>Exception</code> object that represents a reference to the initial exception, if one exists.
     */
    public TableServiceException(final String errorCode, final String message, final int statusCode,
            final StorageExtendedErrorInformation extendedErrorInfo, final Exception innerException) {
        super(errorCode, message, statusCode, extendedErrorInfo, innerException);
    }

    /**
     * Reserved for internal use. Constructs a <code>TableServiceException</code> instance using the specified HTTP
     * status code, message, operation, and stream reader.
     * 
     * @param httpStatusCode
     *            The <code>int</code> HTTP Status Code value returned by the table operation that caused the exception.
     * @param message
     *            A <code>String</code> description of the error that caused the exception.
     * @param operation
     *            The {@link TableOperation} object representing the table operation that was in progress when the
     *            exception occurred.
     * @param reader
     *            The <code>Java.IO.Stream</code> derived stream reader for the HTTP request results returned by the
     *            table operation, if any.
     * @param format
     *            The {@link TablePayloadFormat} to use for parsing
     */
    protected TableServiceException(final int httpStatusCode, final String message, final TableOperation operation,
            final Reader reader, final TablePayloadFormat format) {
        super(null, message, httpStatusCode, null, null);
        this.operation = operation;

        if (reader != null) {
            try {
                this.extendedErrorInformation = TableStorageErrorDeserializer.getExtendedErrorInformation(reader, format);
                this.errorCode = this.extendedErrorInformation.getErrorCode();
            }
            catch (XMLStreamException e) {
                // no-op, if error parsing fails, just throw first exception.
            }
            catch (JsonParseException e) {
                // no-op, if error parsing fails, just throw first exception.
            }
            catch (IOException e) {
                // no-op, if error parsing fails, just throw first exception.
            }
        }
    }

    /**
     * Gets the table operation that caused the <code>TableServiceException</code> to be thrown.
     * 
     * @return
     *         The {@link TableOperation} object representing the table operation that caused this
     *         {@link TableServiceException} to be thrown.
     */
    public TableOperation getOperation() {
        return this.operation;
    }

    /**
     * Reserved for internal use. Gets a flag indicating the table operation can be retried.
     * 
     * @return
     *         The <code>boolean</code> flag indicating whether the table operation that caused the exception can be
     *         retried.
     */
    public boolean isRetryable() {
        return this.retryable;
    }

    /**
     * Reserved for internal use. Sets the table operation that caused the <code>TableServiceException</code> to be
     * thrown.
     * 
     * @param operation
     *            The {@link TableOperation} object representing the table operation that caused this
     *            {@link TableServiceException} to be thrown.
     */
    protected void setOperation(final TableOperation operation) {
        this.operation = operation;
    }

    /**
     * Reserved for internal use. Sets a flag indicating the table operation can be retried.
     * 
     * @param retryable
     *            The <code>boolean</code> flag to set indicating whether the table operation that caused the exception
     *            can be retried.
     */
    protected void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
}
