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

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageLocation;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Analytics Client Tests
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class CloudAnalyticsClientTests {

    protected CloudAnalyticsClient client;
    protected CloudBlobContainer container;

    @Before
    public void analyticsTestMethodSetUp() throws URISyntaxException, StorageException {
        this.container = AnalyticsTestHelper.getRandomContainerReference();
        this.client = AnalyticsTestHelper.createCloudAnalyticsClient();
    }

    @After
    public void analyticsTestMethodTearDown() throws StorageException {
        this.container.deleteIfExists();
    }

    /**
     * Test table getters.
     *
     * @throws StorageException
     * @throws URISyntaxException
     */
    @Test
    public void testCloudAnalyticsClientGetTables() throws URISyntaxException, StorageException {
        CloudTable blobHourPrimary = this.client.getHourMetricsTable(StorageService.BLOB);
        CloudTable blobHourSecondary = this.client.getHourMetricsTable(StorageService.BLOB, StorageLocation.SECONDARY);
        CloudTable fileHourPrimary = this.client.getHourMetricsTable(StorageService.FILE);
        CloudTable fileHourSecondary = this.client.getHourMetricsTable(StorageService.FILE, StorageLocation.SECONDARY);
        CloudTable queueHourPrimary = this.client.getHourMetricsTable(StorageService.QUEUE, StorageLocation.PRIMARY);
        CloudTable queueHourSecondary = this.client.getHourMetricsTable(StorageService.QUEUE, StorageLocation.SECONDARY);
        CloudTable tableHourPrimary = this.client.getHourMetricsTable(StorageService.TABLE, StorageLocation.PRIMARY);
        CloudTable tableHourSecondary = this.client.getHourMetricsTable(StorageService.TABLE, StorageLocation.SECONDARY);

        CloudTable blobMinutePrimary = this.client.getMinuteMetricsTable(StorageService.BLOB);
        CloudTable blobMinuteSecondary = this.client.getMinuteMetricsTable(StorageService.BLOB, StorageLocation.SECONDARY);
        CloudTable fileMinutePrimary = this.client.getMinuteMetricsTable(StorageService.FILE);
        CloudTable fileMinuteSecondary = this.client.getMinuteMetricsTable(StorageService.FILE, StorageLocation.SECONDARY);
        CloudTable queueMinutePrimary = this.client.getMinuteMetricsTable(StorageService.QUEUE, StorageLocation.PRIMARY);
        CloudTable queueMinuteSecondary = this.client.getMinuteMetricsTable(StorageService.QUEUE, StorageLocation.SECONDARY);
        CloudTable tableMinutePrimary = this.client.getMinuteMetricsTable(StorageService.TABLE, StorageLocation.PRIMARY);
        CloudTable tableMinuteSecondary = this.client.getMinuteMetricsTable(StorageService.TABLE, StorageLocation.SECONDARY);

        CloudTable capacity = this.client.getCapacityTable();

        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_BLOB, blobHourPrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_BLOB, blobHourSecondary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_FILE, fileHourPrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_FILE, fileHourSecondary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_QUEUE, queueHourPrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_QUEUE, queueHourSecondary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_PRIMARY_TRANSACTIONS_TABLE, tableHourPrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_HOUR_SECONDARY_TRANSACTIONS_TABLE, tableHourSecondary.getName());

        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_BLOB, blobMinutePrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_BLOB, blobMinuteSecondary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_FILE, fileMinutePrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_FILE, fileMinuteSecondary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_QUEUE, queueMinutePrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_QUEUE, queueMinuteSecondary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_PRIMARY_TRANSACTIONS_TABLE, tableMinutePrimary.getName());
        assertEquals(Constants.AnalyticsConstants.METRICS_MINUTE_SECONDARY_TRANSACTIONS_TABLE, tableMinuteSecondary.getName());

        assertEquals(Constants.AnalyticsConstants.METRICS_CAPACITY_BLOB, capacity.getName());
    }

    /**
     * List all logs
     *
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    public void testCloudAnalyticsClientListLogs() throws URISyntaxException, StorageException, IOException {
        this.container.create();
        this.client.LogContainer = this.container.getName();
        int numBlobs = 13;
        Calendar now = new GregorianCalendar();

        now.add(GregorianCalendar.MONTH, -13);
        List<String> blobNames = AnalyticsTestHelper.CreateLogs(this.container, StorageService.BLOB, 13, now,
                Granularity.MONTH);

        assertEquals(numBlobs, blobNames.size());

        for (ListBlobItem blob : this.client.listLogBlobs(StorageService.BLOB)) {
            assertEquals(CloudBlockBlob.class, blob.getClass());
            assertTrue(blobNames.remove(((CloudBlockBlob) blob).getName()));
        }
        assertTrue(blobNames.size() == 0);
    }

    /**
     * List Logs with open ended time range
     *
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCloudAnalyticsClientListLogsStartTime() throws URISyntaxException, StorageException, IOException {
        this.container.create();
        this.client.LogContainer = this.container.getName();
        int numBlobs = 48;
        Calendar now = new GregorianCalendar();
        now.add(GregorianCalendar.DAY_OF_MONTH, -2);
        List<String> blobNames = AnalyticsTestHelper.CreateLogs(this.container, StorageService.BLOB, 48, now,
                Granularity.HOUR);

        assertEquals(numBlobs, blobNames.size());

        Calendar start = new GregorianCalendar();
        start.add(GregorianCalendar.DAY_OF_MONTH, -1);
        for (ListBlobItem blob : this.client.listLogBlobs(StorageService.BLOB, start.getTime(), null, null, null, null,
                null)) {
            assertEquals(CloudBlockBlob.class, blob.getClass());
            assertTrue(blobNames.remove(((CloudBlockBlob) blob).getName()));
        }
        assertTrue(blobNames.size() == 24);
    }

    /**
     * List Logs with well defined time range
     *
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCloudAnalyticsClientListLogsStartEndTime() throws URISyntaxException, StorageException, IOException {
        this.container.create();
        this.client.LogContainer = this.container.getName();
        int numBlobs = 72;
        Calendar now = new GregorianCalendar();
        now.add(GregorianCalendar.DAY_OF_MONTH, -3);
        List<String> blobNames = AnalyticsTestHelper.CreateLogs(this.container, StorageService.BLOB, 72, now,
                Granularity.HOUR);

        assertEquals(numBlobs, blobNames.size());

        Calendar start = new GregorianCalendar();
        start.add(GregorianCalendar.DAY_OF_MONTH, -2);
        Calendar end = new GregorianCalendar();
        end.add(GregorianCalendar.DAY_OF_MONTH, -1);
        for (ListBlobItem blob : this.client.listLogBlobs(StorageService.BLOB, start.getTime(), end.getTime(), null,
                null, null, null)) {
            assertEquals(CloudBlockBlob.class, blob.getClass());
            assertTrue(blobNames.remove(((CloudBlockBlob) blob).getName()));
        }
        assertTrue(blobNames.size() == 48);
    }

    /**
     * Validate Log Parser
     *
     * @throws ParseException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCloudAnalyticsClientParseExLogs() throws ParseException, URISyntaxException, StorageException,
            IOException {
        String logText = "1.0;2011-08-09T18:52:40.9241789Z;GetBlob;AnonymousSuccess;200;18;10;anonymous;;myaccount;blob;\"https://myaccount.blob.core.windows.net/thumb&amp;nails/lake.jpg?timeout=30000\";\"/myaccount/thumbnails/lake.jpg\";a84aa705-8a85-48c5-b064-b43bd22979c3;0;123.100.2.10;2009-09-19;252;0;265;100;0;;;\"0x8CE1B6EA95033D5\";Tuesday, 09-Aug-11 18:52:40 GMT;;;;\"8/9/2011 6:52:40 PM ba98eb12-700b-4d53-9230-33a3330571fc\""
                + '\n'
                + "1.0;2011-08-09T18:02:40.6271789Z;PutBlob;Success;201;28;21;authenticated;myaccount;myaccount;blob;\"https://myaccount.blob.core.windows.net/thumbnails/lake.jpg?timeout=30000\";\"/myaccount/thumbnails/lake.jpg\";fb658ee6-6123-41f5-81e2-4bfdc178fea3;0;201.9.10.20;2009-09-19;438;100;223;0;100;;\"66CbMXKirxDeTr82SXBKbg==\";\"0x8CE1B67AD25AA05\";Tuesday, 09-Aug-11 18:02:40 GMT;;;;\"8/9/2011 6:02:40 PM ab970a57-4a49-45c4-baa9-20b687941e32\""
                + '\n';
        this.container.createIfNotExists();
        CloudBlockBlob blob = this.container.getBlockBlobReference("blob1");
        blob.uploadText(logText);
        Iterator<LogRecord> iterator = CloudAnalyticsClient.parseLogBlob(blob).iterator();

        assertTrue(iterator.hasNext());
        LogRecord actualItemOne = iterator.next();
        assertTrue(iterator.hasNext());
        LogRecord actualItemTwo = iterator.next();

        LogRecord expectedItemOne = new LogRecord();
        expectedItemOne.setVersionNumber("1.0");
        expectedItemOne.setRequestStartTime(LogRecord.REQUEST_START_TIME_FORMAT.parse("2011-08-09T18:52:40.9241789Z"));
        expectedItemOne.setOperationType("GetBlob");
        expectedItemOne.setRequestStatus("AnonymousSuccess");
        expectedItemOne.setHttpStatusCode("200");
        expectedItemOne.setEndToEndLatencyInMS(18);
        expectedItemOne.setServerLatencyInMS(10);
        expectedItemOne.setAuthenticationType("anonymous");
        expectedItemOne.setRequesterAccountName(null);
        expectedItemOne.setOwnerAccountName("myaccount");
        expectedItemOne.setServiceType("blob");
        expectedItemOne.setRequestUrl(new URI(
                "https://myaccount.blob.core.windows.net/thumb&nails/lake.jpg?timeout=30000"));
        expectedItemOne.setRequestedObjectKey("/myaccount/thumbnails/lake.jpg");
        expectedItemOne.setRequestIdHeader(UUID.fromString("a84aa705-8a85-48c5-b064-b43bd22979c3"));
        expectedItemOne.setOperationCount(0);
        expectedItemOne.setRequesterIPAddress("123.100.2.10");
        expectedItemOne.setRequestVersionHeader("2009-09-19");
        expectedItemOne.setRequestHeaderSize(252L);
        expectedItemOne.setRequestPacketSize(0L);
        expectedItemOne.setResponseHeaderSize(265L);
        expectedItemOne.setResponsePacketSize(100L);
        expectedItemOne.setRequestContentLength(0L);
        expectedItemOne.setRequestMD5(null);
        expectedItemOne.setServerMD5(null);
        expectedItemOne.setETagIdentifier("0x8CE1B6EA95033D5");
        expectedItemOne.setLastModifiedTime(LogRecord.LAST_MODIFIED_TIME_FORMAT
                .parse("Tuesday, 09-Aug-11 18:52:40 GMT"));
        expectedItemOne.setConditionsUsed(null);
        expectedItemOne.setUserAgentHeader(null);
        expectedItemOne.setReferrerHeader(null);
        expectedItemOne.setClientRequestId("8/9/2011 6:52:40 PM ba98eb12-700b-4d53-9230-33a3330571fc");

        LogRecord expectedItemTwo = new LogRecord();
        expectedItemTwo.setVersionNumber("1.0");
        expectedItemTwo.setRequestStartTime(LogRecord.REQUEST_START_TIME_FORMAT.parse("2011-08-09T18:02:40.6271789Z"));
        expectedItemTwo.setOperationType("PutBlob");
        expectedItemTwo.setRequestStatus("Success");
        expectedItemTwo.setHttpStatusCode("201");
        expectedItemTwo.setEndToEndLatencyInMS(28);
        expectedItemTwo.setServerLatencyInMS(21);
        expectedItemTwo.setAuthenticationType("authenticated");
        expectedItemTwo.setRequesterAccountName("myaccount");
        expectedItemTwo.setOwnerAccountName("myaccount");
        expectedItemTwo.setServiceType("blob");
        expectedItemTwo.setRequestUrl(new URI(
                "https://myaccount.blob.core.windows.net/thumbnails/lake.jpg?timeout=30000"));
        expectedItemTwo.setRequestedObjectKey("/myaccount/thumbnails/lake.jpg");
        expectedItemTwo.setRequestIdHeader(UUID.fromString("fb658ee6-6123-41f5-81e2-4bfdc178fea3"));
        expectedItemTwo.setOperationCount(0);
        expectedItemTwo.setRequesterIPAddress("201.9.10.20");
        expectedItemTwo.setRequestVersionHeader("2009-09-19");
        expectedItemTwo.setRequestHeaderSize(438L);
        expectedItemTwo.setRequestPacketSize(100L);
        expectedItemTwo.setResponseHeaderSize(223L);
        expectedItemTwo.setResponsePacketSize(0L);
        expectedItemTwo.setRequestContentLength(100L);
        expectedItemTwo.setRequestMD5(null);
        expectedItemTwo.setServerMD5("66CbMXKirxDeTr82SXBKbg==");
        expectedItemTwo.setETagIdentifier("0x8CE1B67AD25AA05");
        expectedItemTwo.setLastModifiedTime(LogRecord.LAST_MODIFIED_TIME_FORMAT
                .parse("Tuesday, 09-Aug-11 18:02:40 GMT"));
        expectedItemTwo.setConditionsUsed(null);
        expectedItemTwo.setUserAgentHeader(null);
        expectedItemTwo.setReferrerHeader(null);
        expectedItemTwo.setClientRequestId("8/9/2011 6:02:40 PM ab970a57-4a49-45c4-baa9-20b687941e32");

        CloudAnalyticsClientTests.assertLogItemsEqual(expectedItemOne, actualItemOne);
        CloudAnalyticsClientTests.assertLogItemsEqual(expectedItemTwo, actualItemTwo);
    }

    /**
     * Validate Log Parser with prod data
     *
     * @throws ParseException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCloudAnalyticsClientParseProdLogs() throws ParseException, URISyntaxException, StorageException,
            IOException {

        Calendar startTime = new GregorianCalendar();
        startTime.add(GregorianCalendar.DAY_OF_MONTH, -3);

        Iterator<LogRecord> logRecordsIterator = (this.client.listLogRecords(StorageService.BLOB, startTime.getTime(),
                null, null, null)).iterator();

        while (logRecordsIterator.hasNext()) {
            // Makes sure there's no exceptions thrown and that no records are null.
            // Primarily a sanity check.
            LogRecord rec = logRecordsIterator.next();
            assertNotNull(rec);
        }
    }

    /**
     * Log parser error cases.
     *
     * @throws ParseException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCloudAnalyticsClientParseLogErrors() throws ParseException, URISyntaxException, StorageException,
            IOException {
        this.container.createIfNotExists();

        String v2Entry = "2.0;2011-08-09T18:02:40.6271789Z;PutBlob;Success;201;28;21;authenticated;myaccount;myaccount;blob;\"https://myaccount.blob.core.windows.net/thumbnails/lake.jpg?timeout=30000\";\"/myaccount/thumbnails/lake.jpg\";fb658ee6-6123-41f5-81e2-4bfdc178fea3;0;201.9.10.20;2009-09-19;438;100;223;0;100;;\"66CbMXKirxDeTr82SXBKbg==\";\"0x8CE1B67AD25AA05\";Tuesday, 09-Aug-11 18:02:40 GMT;;;;\"8/9/2011 6:02:40 PM ab970a57-4a49-45c4-baa9-20b687941e32\"" + '\n';
        CloudBlockBlob v2Blob = this.container.getBlockBlobReference("v2Blob");
        v2Blob.uploadText(v2Entry);
        Iterator<LogRecord> v2Iterator = CloudAnalyticsClient.parseLogBlob(v2Blob).iterator();
        try {
            v2Iterator.next();
            fail();
        }
        catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "A storage log version of 2.0 is unsupported.");
        }

        // Note that if this non log data ('%s') contains a semicolon, we'll get an unfortunate failure
        // saying that a log version of %s is not supported. Otherwise, we'll see the behavior below.
        String nonLogData = "THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG 1234567890";
        CloudBlockBlob nonLogDataBlob = this.container.getBlockBlobReference("nonLogBlob");
        nonLogDataBlob.uploadText(nonLogData);
        Iterator<LogRecord> nonLogDataIterator = CloudAnalyticsClient.parseLogBlob(nonLogDataBlob).iterator();
        try {
            nonLogDataIterator.next();
            fail();
        }
        catch (NoSuchElementException e) {
            assertEquals(e.getMessage(),
                    "An error occurred while enumerating the result, check the original exception for details.");
            assertEquals(e.getCause().getMessage(), "Error parsing log record: unexpected end of stream.");
        }

        CloudBlockBlob nullBlob1 = this.container.getBlockBlobReference("nullBlob1");
        CloudBlockBlob nullBlob2 = this.container.getBlockBlobReference("nullBlob2");
        CloudBlockBlob nullBlob3 = this.container.getBlockBlobReference("nullBlob3");
        ArrayList<ListBlobItem> nullBlobIterator = new ArrayList<ListBlobItem>();
        nullBlobIterator.add(nullBlob1);
        nullBlobIterator.add(nullBlob2);
        nullBlobIterator.add(nullBlob3);
        Iterator<LogRecord> nullLogIterator = CloudAnalyticsClient.parseLogBlobs(nullBlobIterator).iterator();
        try {
            nullLogIterator.next();
            fail();
        }
        catch (NoSuchElementException e) {
            assertEquals(e.getMessage(),
                    "An error occurred while enumerating the result, check the original exception for details.");
            assertEquals(e.getCause().getMessage(), "The specified blob does not exist.");
        }

        try {
            Iterator<LogRecord> emptyIterator = CloudAnalyticsClient.parseLogBlobs(new ArrayList<ListBlobItem>()).iterator();
            assertFalse(emptyIterator.hasNext());
            emptyIterator.next();
        }
        catch (NoSuchElementException e) {
            assertEquals(e.getMessage(), "There are no more elements in this enumeration.");
        }

        try {
            CloudAnalyticsClient.parseLogBlobs(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("The argument must not be null. Argument name: logBlobs.", e.getMessage());
        }
    }

    public static void assertLogItemsEqual(LogRecord expected, LogRecord actual) {
        assertEquals(expected.getVersionNumber(), actual.getVersionNumber());

        assertEquals(expected.getRequestStartTime(), actual.getRequestStartTime());
        assertEquals(expected.getOperationType(), actual.getOperationType());
        assertEquals(expected.getRequestStatus(), actual.getRequestStatus());
        assertEquals(expected.getHttpStatusCode(), actual.getHttpStatusCode());
        assertEquals(expected.getEndToEndLatencyInMS(), actual.getEndToEndLatencyInMS());
        assertEquals(expected.getServerLatencyInMS(), actual.getServerLatencyInMS());
        assertEquals(expected.getAuthenticationType(), actual.getAuthenticationType());
        assertEquals(expected.getRequesterAccountName(), actual.getRequesterAccountName());
        assertEquals(expected.getOwnerAccountName(), actual.getOwnerAccountName());
        assertEquals(expected.getServiceType(), actual.getServiceType());
        assertEquals(expected.getRequestUrl(), actual.getRequestUrl());
        assertEquals(expected.getRequestedObjectKey(), actual.getRequestedObjectKey());
        assertEquals(expected.getRequestIdHeader(), actual.getRequestIdHeader());
        assertEquals(expected.getOperationCount(), actual.getOperationCount());
        assertEquals(expected.getRequesterIPAddress(), actual.getRequesterIPAddress());
        assertEquals(expected.getRequestVersionHeader(), actual.getRequestVersionHeader());
        assertEquals(expected.getRequestHeaderSize(), actual.getRequestHeaderSize());
        assertEquals(expected.getRequestPacketSize(), actual.getRequestPacketSize());
        assertEquals(expected.getResponseHeaderSize(), actual.getResponseHeaderSize());
        assertEquals(expected.getResponsePacketSize(), actual.getResponsePacketSize());
        assertEquals(expected.getRequestContentLength(), actual.getRequestContentLength());
        assertEquals(expected.getRequestMD5(), actual.getRequestMD5());
        assertEquals(expected.getServerMD5(), actual.getServerMD5());
        assertEquals(expected.getETagIdentifier(), actual.getETagIdentifier());
        assertEquals(expected.getLastModifiedTime(), actual.getLastModifiedTime());
        assertEquals(expected.getConditionsUsed(), actual.getConditionsUsed());
        assertEquals(expected.getUserAgentHeader(), actual.getUserAgentHeader());
        assertEquals(expected.getReferrerHeader(), actual.getReferrerHeader());
        assertEquals(expected.getClientRequestId(), actual.getClientRequestId());
    }
}
