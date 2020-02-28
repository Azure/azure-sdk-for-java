// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery

import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import com.azure.storage.quickquery.models.BlobQuickQueryDelimitedSerialization
import com.azure.storage.quickquery.models.BlobQuickQueryError
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver
import com.azure.storage.quickquery.models.BlobQuickQuerySerialization
import spock.lang.Unroll

class BlobQuickQueryAPITest extends APISpec {

    // Generates and uploads a small 1KB CSV file
    def uploadSmallCsv(BlobQuickQueryDelimitedSerialization ser, boolean nonFatalError) {
        byte[] headers = ("rn1,rn2,rn3,rn4" + ser.getRecordSeparator()).getBytes()
        char randomColSep = '.'
        if (nonFatalError) {
            if (ser.getColumnSeparator() == randomColSep) {
                randomColSep = ','
            }
        }
        byte[] csvData = ("100" + ser.getColumnSeparator() + "200" + ser.getColumnSeparator() + "300" + ser.getColumnSeparator() + "400" +
            ser.getRecordSeparator() + "300" + ser.getColumnSeparator() + "400" + randomColSep + "500" + ser.getColumnSeparator() + "600" +
            ser.getRecordSeparator()).getBytes()

        int headerLength = ser.isHeadersPresent() ? headers.length : 0
        byte[] data = new byte[headerLength + csvData.length * 32]
        if (ser.isHeadersPresent()) {
            System.arraycopy(headers, 0, data, 0, headers.length)
        }

        for (int i = 0; i < 32; i++) {
            int o = i * csvData.length + headerLength;
            System.arraycopy(csvData, 0, data, o, csvData.length)
        }

        InputStream inputStream = new ByteArrayInputStream(data)

        bc.upload(inputStream, data.length, true)

    }

    def uploadSmallJson() {

    }

    BlobQuickQueryClient qqClient
    BlobQuickQueryAsyncClient qqAsyncClient

    def setup() {
        qqClient = new BlobQuickQueryClientBuilder(bc).buildClient()
        qqAsyncClient = new BlobQuickQueryClientBuilder(bcAsync).buildAsyncClient()
    }

    def "Query min"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
        .setRecordSeparator('\n')
        .setColumnSeparator(',')
        .setEscapeChar('\0')
        .setFieldQuote('\0')
        .setHeadersPresent(true)
        uploadSmallCsv(ser)

        ByteArrayOutputStream queryData = new ByteArrayOutputStream()
        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()

        when:
        qqClient.parsedQueryWithResponseSample(queryData, "SELECT * from BlobStorage", null, null, null, null, null, null, null)

        bc.download(downloadData)

        then:
        queryData.toByteArray() == downloadData.toByteArray()
    }

    def "Query empty file"() {
        when:
        qqClient.query(new ByteArrayOutputStream(), "SELECT * from BlobStorage")

        then:
        notThrown(BlobStorageException)
    }

    def "Query input stream min"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(true)
        uploadSmallCsv(ser)

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage")

        bc.download(downloadData)

        byte[] queryData = new byte[downloadData.toByteArray().length]

        qqStream.read(queryData, 0, downloadData.toByteArray().length)

        qqStream.close()

        then:
        queryData == downloadData.toByteArray()
    }

    @Unroll
    def "Query input delimited"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator)
            .setColumnSeparator(columnSeparator)
            .setEscapeChar(fieldQuote)
            .setFieldQuote(escapeChar)
            .setHeadersPresent(headersPresent)
        uploadSmallCsv(ser)

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage", ser, ser, null, null, null)

        qqStream.read(new byte[10], 0, 10)

        qqStream.close()

        then:
        notThrown(BlobStorageException)

        where:
        recordSeparator | columnSeparator | fieldQuote    | escapeChar      | headersPresent
        '\n' as char    | ',' as char     | '\0' as char  | '\0' as char    | true
        '\n' as char    | '.' as char     | '\0' as char  | '\0' as char    | false
    }

//    @Unroll
//    def "Query input json"() {
//        setup:
//        ByteArrayOutputStream queryData = new ByteArrayOutputStream()
//
//        uploadSmallCsv('\n' as char, ',' as char, '\0' as char, '\0' as char, true)
//        when:
//        qqClient.queryWithResponse(new ByteArrayOutputStream(), "SELECT * from BlobStorage")
//
//        then:
//        notThrown(BlobStorageException)
//
//        where:
//
//    }

    @Unroll
    def "Query input output IA"() {
        when:
        qqClient.openInputStream("SELECT * from BlobStorage", input, output, null, null, null)

        then:
        thrown(IllegalArgumentException)

        where:
        input                                                    | output                                                   || _
        new MockSerialization().setRecordSeparator('\n' as char) | null                                                     || _
        null                                                     | new MockSerialization().setRecordSeparator('\n' as char) || _

    }

    class MockSerialization extends BlobQuickQuerySerialization<MockSerialization> {
    }

    def "Query input non fatal error"() {
        setup:
        BlobQuickQueryDelimitedSerialization base = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(true)

        uploadSmallCsv(base.setColumnSeparator('.' as char), true)

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT _2 from BlobStorage WHERE _1 > 250;",
            base.setColumnSeparator(',' as char), base.setColumnSeparator(',' as char), null,
            new MockErrorReceiver("InvalidTypeConversion"), null)


        qqStream.read(new byte[Constants.KB], 0, Constants.KB)

        qqStream.close()

        then:
        notThrown(BlobStorageException)
    }

    class MockErrorReceiver implements BlobQuickQueryErrorReceiver {

        String expectedType

        MockErrorReceiver(String expectedType) {
            this.expectedType = expectedType
        }

        @Override
        void reportError(BlobQuickQueryError nonFatalError) {
            assert !nonFatalError.isFatal()
            assert nonFatalError.getName() == expectedType
        }
    }
//
//    def "Query output"() {
//        qqClient.query()
//    }

    // Query illegal arguments

    // Query non fatal error handling

    // Query fatal error handling

    // Query progress receiver

    // Query snapshot

    // Query CPK

    // Query encryption scope

    @Unroll
    def "Query AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        InputStream stream = qqClient.openInputStream("SELECT * from BlobStorage", null, null, bac, null, null)

        stream.read()

        stream.close()

        then:
        notThrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Query AC fail"() {
        setup:
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        qqClient.openInputStream("SELECT * from BlobStorage", null, null, bac, null, null).read()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }
}
