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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.LoggingOperations;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageLocation;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

/**
 * Provides a client-side logical representation for Microsoft Azure Storage Analytics. This client is used to configure
 * and execute storage analytics requests.
 * <p>
 * The service client encapsulates the endpoint or endpoints for the blob and table service. It also encapsulates the
 * credentials for accessing the storage account.
 */
public class CloudAnalyticsClient {

    /**
     * The blob client for logging features.
     */
    protected final CloudBlobClient blobClient;

    /**
     * The table client for metrics features.
     */
    protected final CloudTableClient tableClient;

    /**
     * The container in which to look for logs.
     */
    protected String LogContainer = Constants.AnalyticsConstants.LOGS_CONTAINER;

    /**
     * Initializes a new instance of the <code>CloudAnalyticsClient</code> class using the specified blob and table
     * service endpoints and account credentials.
     * 
     * @param blobStorageUri
     *            A {@link StorageUri} object containing the Blob service endpoint to use to create the client.
     * @param tableStorageUri
     *            A {@link StorageUri} object containing the Table service endpoint to use to create the client.
     * @param credentials
     *            A {@link StorageCredentials} object.
     */
    public CloudAnalyticsClient(StorageUri blobStorageUri, StorageUri tableStorageUri, StorageCredentials credentials) {
        Utility.assertNotNull("blobStorageUri", blobStorageUri);
        Utility.assertNotNull("tableStorageUri", tableStorageUri);

        this.blobClient = new CloudBlobClient(blobStorageUri, credentials);
        this.tableClient = new CloudTableClient(tableStorageUri, credentials);
    }

    /**
     * Gets the {@link CloudBlobDirectory} object for the logs for a specific storage service.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @return
     *         A {@link CloudBlobDirectory} object.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public CloudBlobDirectory getLogDirectory(StorageService service) throws URISyntaxException, StorageException {
        Utility.assertNotNull("service", service);
        return this.blobClient.getContainerReference(this.LogContainer).getDirectoryReference(
                service.toString().toLowerCase(Locale.US));
    }

    /**
     * Gets the hour metrics table for a specific storage service.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @return
     *         The {@link CloudTable} object for the storage service.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public CloudTable getHourMetricsTable(StorageService service) throws URISyntaxException, StorageException {
        return this.getHourMetricsTable(service, null);
    }

    /**
     * Gets the hour metrics table for a specific storage service.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @param location
     *            A {@link StorageLocation} enumeration value that indicates which storage location to use.
     * @return
     *         The {@link CloudTable} object for the storage service.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public CloudTable getHourMetricsTable(StorageService service, StorageLocation location) throws URISyntaxException,
            StorageException {
        Utility.assertNotNull("service", service);
        if (location == null) {
            location = StorageLocation.PRIMARY;
        }

        switch (service) {
            case BLOB:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_BLOB);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_BLOB);
                }
                
            case FILE:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_FILE);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_FILE);
                }
                
            case QUEUE:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_QUEUE);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_QUEUE);
                }
                
            case TABLE:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_TABLE);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_TABLE);
                }
                
            default:
                throw new IllegalArgumentException(SR.INVALID_STORAGE_SERVICE);
        }
    }

    /**
     * Gets the minute metrics table for a specific storage service.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @return
     *         The {@link CloudTable} object for the storage service.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public CloudTable getMinuteMetricsTable(StorageService service) throws URISyntaxException, StorageException {
        return this.getMinuteMetricsTable(service, null);
    }

    /**
     * Gets the minute metrics table for a specific storage service.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @param location
     *            A {@link StorageLocation} enumeration value that indicates which storage location to use.
     * @return
     *         The <code>CloudTable</code> object for the storage service.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public CloudTable getMinuteMetricsTable(StorageService service, StorageLocation location)
            throws URISyntaxException, StorageException {
        Utility.assertNotNull("service", service);
        if (location == null) {
            location = StorageLocation.PRIMARY;
        }

        switch (service) {
            case BLOB:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_BLOB);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_BLOB);
                }
            case FILE:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_FILE);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_FILE);
                }
            case QUEUE:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_QUEUE);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_QUEUE);
                }
            case TABLE:
                if (location == StorageLocation.PRIMARY) {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_TABLE);
                }
                else {
                    return this.tableClient.getTableReference(
                            Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_TABLE);
                }
            default:
                throw new IllegalArgumentException(SR.INVALID_STORAGE_SERVICE);
        }
    }

    /**
     * Gets the capacity metrics table for the blob service.
     * 
     * @return
     *         A {@link CloudTable} object.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public CloudTable getCapacityTable() throws URISyntaxException, StorageException {
        return this.tableClient.getTableReference(Constants.AnalyticsConstants.METRICS_CAPACITY_BLOB);
    }

    /**
     * Returns an enumerable collection of log blobs, retrieved lazily.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @return
     *         An enumerable collection of objects that implement <code>ListBlobItem</code> and are retrieved lazily.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public Iterable<ListBlobItem> listLogBlobs(StorageService service) throws URISyntaxException, StorageException {
        return this.listLogBlobs(service, null /* startTime */, null /* endTime */, null /* operations */,
                null /* details */, null /* options */, null /* operationContext */);
    }

    /**
     * Returns an enumerable collection of log blobs, retrieved lazily.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @param startTime
     *            A <code>java.util.Date</code> object representing the start of the time range for which logs should
     *            be retrieved.
     * @param endTime
     *            A <code>java.util.Date</code> object representing the end of the time range for which logs should
     *            be retrieved.
     * @param operations
     *            A {@link LoggingOperations} enumeration set that indicates which log types to return.
     * @param details
     *            A {@link BlobListingDetails} enumeration set that indicates whether or not blob metadata should
     *            be returned. None or METADATA are the only valid values.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies additional options for the request.
     * @param operationContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     * @return
     *         An enumerable collection of objects that implement {@link ListBlobItem} and are retrieved lazily.
     * @throws StorageException
     * @throws URISyntaxException
     */
    public Iterable<ListBlobItem> listLogBlobs(StorageService service, Date startTime, Date endTime,
            EnumSet<LoggingOperations> operations, BlobListingDetails details, BlobRequestOptions options,
            OperationContext operationContext) throws StorageException, URISyntaxException {
        Utility.assertNotNull("service", service);
        if (operations == null) {
            operations = EnumSet.allOf(LoggingOperations.class);
        }

        if (!(details == null || details.equals(BlobListingDetails.METADATA))) {
            throw new IllegalArgumentException(SR.INVALID_LISTING_DETAILS);
        }

        if (operations.equals(EnumSet.noneOf(LoggingOperations.class))) {
            throw new IllegalArgumentException(SR.INVALID_LOGGING_LEVEL);
        }

        EnumSet<BlobListingDetails> metadataDetails;
        if (details != null
                && (details.equals(BlobListingDetails.METADATA) || !operations.equals(EnumSet
                        .allOf(LoggingOperations.class)))) {
            metadataDetails = EnumSet.of(BlobListingDetails.METADATA);
        }
        else {
            metadataDetails = EnumSet.noneOf(BlobListingDetails.class);
        }

        return new LogBlobIterable(this.getLogDirectory(service), startTime, endTime, operations, metadataDetails,
                options, operationContext);
    }

    /**
     * Returns an enumerable collection of log records, retrieved lazily.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @return
     *         An enumerable collection of objects that implement <code>ListBlobItem</code> and are retrieved lazily.
     * @throws URISyntaxException
     * @throws StorageException
     */
    public Iterable<LogRecord> listLogRecords(StorageService service) throws URISyntaxException, StorageException {
        return this
                .listLogRecords(service, null /* startTime */, null /* endTime */, null /* options */, null /* operationContext */);
    }

    /**
     * Returns an enumerable collection of log records, retrieved lazily.
     * 
     * @param service
     *            A {@link StorageService} enumeration value that indicates which storage service to use.
     * @param startTime
     *            A <code>java.util.Date</code> object representing the start of the time range for which logs should
     *            be retrieved.
     * @param endTime
     *            A <code>java.util.Date</code> object representing the end of the time range for which logs should
     *            be retrieved.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies additional options for the request.
     * @param operationContext
     *            An {@link OperationContext} object that represents the context for the current operation.
     * @return
     *         An enumerable collection of objects that implement {@link ListBlobItem} and are retrieved lazily.
     * @throws StorageException
     * @throws URISyntaxException
     */
    public Iterable<LogRecord> listLogRecords(StorageService service, Date startTime, Date endTime,
            BlobRequestOptions options, OperationContext operationContext) throws StorageException, URISyntaxException {
        Utility.assertNotNull("service", service);
        EnumSet<LoggingOperations> operations = EnumSet.allOf(LoggingOperations.class);
        EnumSet<BlobListingDetails> metadataDetails = EnumSet.noneOf(BlobListingDetails.class);
        Iterator<ListBlobItem> blobIterator = new LogBlobIterable(this.getLogDirectory(service), startTime, endTime,
                operations, metadataDetails, options, operationContext).iterator();

        return new LogRecordIterable(blobIterator);
    }

    /**
     * Returns an enumerable collection of log records, retrieved lazily.
     * 
     * @param logBlobs
     *            An {@link Iterable} of blobs to parse LogRecords from.
     * @return
     *         An enumerable collection of objects that implement {@link LogRecords} and are retrieved lazily.
     * @throws StorageException
     * @throws URISyntaxException
     */
    public static Iterable<LogRecord> parseLogBlobs(Iterable<ListBlobItem> logBlobs) {
        Utility.assertNotNull("logBlobs", logBlobs);

        return new LogRecordIterable(logBlobs.iterator());
    }

    /**
     * Returns an enumerable collection of log records, retrieved lazily.
     * 
     * @param logBlobs
     *            An {@link Iterable} of blobs to parse LogRecords from.
     * @return
     *         An enumerable collection of objects that implement {@link LogRecords} and are retrieved lazily.
     * @throws StorageException
     * @throws URISyntaxException
     */
    public static Iterable<LogRecord> parseLogBlob(ListBlobItem logBlob) {
        Utility.assertNotNull("logBlob", logBlob);
        ArrayList<ListBlobItem> blobWrapper = new ArrayList<ListBlobItem>();
        blobWrapper.add(logBlob);

        return new LogRecordIterable(blobWrapper.iterator());
    }
}