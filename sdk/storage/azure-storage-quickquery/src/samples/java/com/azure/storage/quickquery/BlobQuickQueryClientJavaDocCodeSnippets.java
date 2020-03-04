// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.ErrorReceiver;
import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryDelimitedSerialization;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryJsonSerialization;
import com.azure.storage.quickquery.models.BlobQuickQuerySerialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;

public class BlobQuickQueryClientJavaDocCodeSnippets {

    private BlobQuickQueryClient client = new BlobQuickQueryClientBuilder(new BlobClientBuilder().buildClient())
        .buildClient();
    private Duration timeout = Duration.ofSeconds(30);
    private String key = "key";
    private String value = "value";

    /**
     * Code snippet for {@link BlobQuickQueryClient#openInputStream(String)}
     */
    public void openInputStream() {
        // BEGIN: com.azure.storage.quickquery.BlobQuickQueryClient.openInputStream#String
        String expression = "SELECT c1,c2 from blob1 WHERE c1 > 20 LIMIT 100;";
        InputStream inputStream = client.openInputStream(expression);
        // Now you can read from the input stream like you would normally.
        // END: com.azure.storage.quickquery.BlobQuickQueryClient.openInputStream#String
    }

    /**
     * Code snippet for {@link BlobQuickQueryClient#openInputStream(String, BlobQuickQuerySerialization, BlobQuickQuerySerialization, BlobRequestConditions, ErrorReceiver, ProgressReceiver)}
     */
    public void openInputStreamMaxOverload() {
        // BEGIN: com.azure.storage.quickquery.BlobQuickQueryClient.openInputStream#String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-ErrorReceiver-ProgressReceiver
        String expression = "SELECT c1,c2 from blob1 WHERE c1 > 20 LIMIT 100;";
        BlobQuickQuerySerialization input = new BlobQuickQueryDelimitedSerialization()
            .setColumnSeparator(',')
            .setEscapeChar('\n')
            .setRecordSeparator('\n')
            .setHeadersPresent(true)
            .setFieldQuote('"');
        BlobQuickQuerySerialization output = new BlobQuickQueryJsonSerialization()
            .setRecordSeparator('\n');
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId("leaseId");

        ErrorReceiver<BlobQuickQueryError> nonFatalErrorHander = System.out::println;
        ProgressReceiver progressReceiver = bytesTransferred -> System.out.println("total blob bytes read: "
            + bytesTransferred);

        InputStream inputStream = client.openInputStream(expression, input, output, requestConditions,
            nonFatalErrorHander, progressReceiver);
        // Now you can read from the input stream like you would normally.
        // END: com.azure.storage.quickquery.BlobQuickQueryClient.openInputStream#String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-ErrorReceiver-ProgressReceiver
    }

    /**
     * Code snippet for {@link BlobQuickQueryClient#query(OutputStream, String)}
     */
    public void query() {
        // BEGIN: com.azure.storage.quickquery.BlobQuickQueryClient.query#OutputStream-String
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT c1,c2 from blob1 WHERE c1 > 20 LIMIT 100;";
        client.query(queryData, expression);
        System.out.println("Query completed.");
        // END: com.azure.storage.quickquery.BlobQuickQueryClient.query#OutputStream-String
    }

    /**
     * Code snippet for {@link BlobQuickQueryClient#queryWithResponse(OutputStream, String, BlobQuickQuerySerialization, BlobQuickQuerySerialization, BlobRequestConditions, Duration, Context)}
     */
    public void queryWithResponse() {
        // BEGIN: com.azure.storage.quickquery.BlobQuickQueryClient.queryWithResponse#OutputStream-String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-Duration-Context
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT c1,c2 from blob1 WHERE c1 > 20 LIMIT 100;";
        BlobQuickQueryJsonSerialization input = new BlobQuickQueryJsonSerialization()
            .setRecordSeparator('\n');
        BlobQuickQueryDelimitedSerialization output = new BlobQuickQueryDelimitedSerialization()
            .setEscapeChar('\0')
            .setColumnSeparator(',')
            .setRecordSeparator('\n')
            .setFieldQuote('\'')
            .setHeadersPresent(true);
        System.out.printf("Query completed with status %d%n",
            client.queryWithResponse(queryData, expression, input, output, null,
                timeout, new Context(key, value)).getStatusCode());
        // END: com.azure.storage.quickquery.BlobQuickQueryClient.queryWithResponse#OutputStream-String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-Duration-Context
    }
}
