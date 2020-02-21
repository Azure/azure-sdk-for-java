package com.azure.storage.blob.quickquery;


import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryDelimitedSerialization;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryJsonSerialization;
import com.azure.storage.blob.quickquery.models.BlobQuickQuerySerialization;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.Duration;

public class BlobQuickQueryClientJavaDocCodeSnippets {

    private BlobQuickQueryClient client = new BlobQuickQueryClientBuilder(new BlobClientBuilder().buildClient())
        .buildClient();
    private Duration timeout = Duration.ofSeconds(30);
    private String key = "key";
    private String value = "value";

    /**
     * Code snippet for {@link BlobQuickQueryClient#query(OutputStream, String)}
     */
    public void query() {
        // BEGIN: com.azure.storage.blob.quickquery.BlobQuickQueryClient.query#OutputStream-String
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT c1,c2 from blob1 WHERE c1 > 20 LIMIT 100;";
        client.query(queryData, expression);
        System.out.println("Query completed.");
        // END: com.azure.storage.blob.quickquery.BlobQuickQueryClient.query#OutputStream-String
    }

    /**
     * Code snippet for {@link BlobQuickQueryClient#queryWithResponse(OutputStream, String, BlobQuickQuerySerialization, BlobQuickQuerySerialization, BlobRequestConditions, Duration, Context)}
     */
    public void queryWithResponse() {
        // BEGIN: com.azure.storage.blob.quickquery.BlobQuickQueryClient.queryWithResponse#OutputStream-String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-Duration-Context
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
        // END: com.azure.storage.blob.quickquery.BlobQuickQueryClient.queryWithResponse#OutputStream-String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-Duration-Context
    }
}
