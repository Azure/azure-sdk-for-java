// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery

import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import com.azure.storage.quickquery.implementation.util.NetworkInputStream
import com.azure.storage.quickquery.models.BlobQuickQueryDelimitedSerialization
import com.azure.storage.quickquery.models.BlobQuickQueryError
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver
import reactor.core.publisher.Flux
import spock.lang.Unroll

import java.nio.ByteBuffer

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

//    def "Query min"() {
//        setup:
//        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
//        .setRecordSeparator('\n' as char)
//        .setColumnSeparator(',' as char)
//        .setEscapeChar('\0' as char)
//        .setFieldQuote('\0' as char)
//        .setHeadersPresent(true)
//        uploadSmallCsv(ser, false)
//
//        ByteArrayOutputStream queryData = new ByteArrayOutputStream()
//        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
//
//        when:
//        qqClient.parsedQueryWithResponseSample(queryData, "SELECT * from BlobStorage", null, null, null, null, null, null, null)
//
//        bc.download(downloadData)
//
//        then:
//        queryData.toByteArray() == downloadData.toByteArray()
//    }

//    def "Query empty file"() {
//        when:
//        qqClient.query(new ByteArrayOutputStream(), "SELECT * from BlobStorage")
//
//        then:
//        notThrown(BlobStorageException)
//    }

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


        qqStream.read(new byte[Constants.MB], 0, Constants.MB)

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

    def "test network reader"() {
        setup:
        BlobQuickQueryDelimitedSerialization base = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(true)
        uploadSmallCsv(base.setColumnSeparator('.' as char), true)

        when:
        Flux<ByteBuffer> data = qqAsyncClient.query("SELECT *")
        NetworkInputStream is = new NetworkInputStream(data, new ClientLogger("meh"))

        is.read()

        then:
        System.out.println("done")
    }
}
