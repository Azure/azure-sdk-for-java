// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery

import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.quickquery.models.BlobQuickQueryDelimitedSerialization
import spock.lang.Unroll

class BlobQQInputStreamTest extends APISpec {
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

    def "Query input stream min"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(true)
        uploadSmallCsv(ser, false)

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
            .setRecordSeparator(recordSeparator as char)
            .setColumnSeparator(columnSeparator as char)
            .setEscapeChar(fieldQuote as char)
            .setFieldQuote(escapeChar as char)
            .setHeadersPresent(headersPresent)
        uploadSmallCsv(ser, false)

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage", ser, ser, null, null, null)

        qqStream.read(new byte[10], 0, 10)

        qqStream.close()

        then:
        notThrown(BlobStorageException)

        where:
        recordSeparator | columnSeparator | fieldQuote  | escapeChar    | headersPresent
        '\n'            | ','             | '\0'        | '\0'          | true
        '\n'            | '.'             | '\0'        | '\0'          | false
    }

    @Unroll
    def "Query input output IA"() {
        when:
        InputStream stream = qqClient.openInputStream("SELECT * from BlobStorage", input, output, null, null, null)

        stream.read()

        stream.close()

        then:
        thrown(IllegalArgumentException)

        where:
        input                                                    | output                                                   || _
        new MockSerialization().setRecordSeparator('\n' as char) | null                                                     || _
        null                                                     | new MockSerialization().setRecordSeparator('\n' as char) || _
    }

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
}
