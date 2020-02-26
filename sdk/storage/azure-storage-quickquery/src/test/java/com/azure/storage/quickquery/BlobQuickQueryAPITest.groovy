// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery

import com.azure.storage.blob.BlobServiceVersion
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import spock.lang.Unroll

class BlobQuickQueryAPITest extends APISpec {

    // Generates and uploads a small 1KB CSV file
    def uploadSmallCsv(char recordSeparator, char columnSeparator, char fieldQuote, char escapeChar, boolean headersPresent) {
        byte[] headers = ("rn1,rn2,rn3,rn4" + recordSeparator).getBytes()
        byte[] csvData = ("100" + columnSeparator + "200" + columnSeparator + "300" + columnSeparator + "400" +
            recordSeparator + "300" + columnSeparator + "400" + columnSeparator + "500" + columnSeparator + "600" +
            recordSeparator).getBytes()

        int headerLength = headersPresent ? headers.length : 0
        byte[] data = new byte[headerLength + csvData.length * 32]
        if (headersPresent) {
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
        uploadSmallCsv('\n' as char, ',' as char, '\0' as char, '\0' as char, true)

        when:
        qqClient.query(new ByteArrayOutputStream(), "SELECT * from BlobStorage")

        then:
        notThrown(BlobStorageException)
    }
//
//    def "Query empty file"() {
//        qqClient.query()
//    }
//
//    def "Query input"() {
//        qqClient.query()
//    }
//
//    def "Query input non fatal error"() {
//        qqClient.query()
//    }
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
        def response = qqClient.queryWithResponse(new ByteArrayOutputStream(), "SELECT *", null, null, bac, null, null)

        then:
        response.getStatusCode() == 200

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
        qqClient.queryWithResponse(new ByteArrayOutputStream(), "SELECT *", null, null, bac, null, null)

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
