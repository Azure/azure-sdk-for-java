// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.quickquery;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryDelimitedSerialization;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryError;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryProgress;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryResponse;
import com.azure.storage.blob.quickquery.models.BlobQuickQueryResultData;
import com.azure.storage.blob.quickquery.models.BlobQuickQuerySerialization;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

public class BlobQuickQueryClient {
    private final ClientLogger logger = new ClientLogger(BlobQuickQueryClient.class);

    private BlobQuickQueryAsyncClient client;

    BlobQuickQueryClient(BlobQuickQueryAsyncClient client) {
        this.client = client;
    }

    public void query(OutputStream stream, String expression) {
        queryWithResponse(stream, expression, null, null, null, Context.NONE);
    }

    public BlobQuickQueryResponse queryWithResponse(OutputStream stream, String expression,
        BlobQuickQuerySerialization input, BlobQuickQuerySerialization output, Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("stream", stream);
        Mono<BlobQuickQueryResponse> download = client
            .queryWithResponse(expression, input, output, context)
            .flatMap(response -> response.getValue().reduce(stream, (outputStream, buffer) -> {
                try {
                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
                    return outputStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new BlobQuickQueryResponse(response)));

        return blockWithOptionalTimeout(download, timeout);
    }

    public static void main(String[] args) throws IOException {
        String accountName = "0cpxscnapsat09prde01f";
        String accountKey = "EDy+1m2BEi7arksSm5UBabPgNfpkv8/nxWdKbi2geqPeTAxEw5eqtViNPNLQmXqY26RFlZDspyMQF1aDGblRrw==";
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
            .wiretap(true)
            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)));

        BlobServiceClient sc = new BlobServiceClientBuilder()
            .endpoint("https://" + accountName + ".blob.preprod.core.windows.net")
            .credential(credential)
            .httpClient(builder.build())
            .buildClient();

        BlobContainerClient cc = sc.getBlobContainerClient("myquickquerycontainer");

        BlobClient bc = cc.getBlobClient("myquickqueryblob.csv");

        byte[] csvData = "100,200,300,400\n300,400,500,600\n".getBytes();

        byte[] data = new byte[1024];

        for (int i = 0; i < 32; i++) {
            int o = i * csvData.length;
            System.arraycopy(csvData, 0, data, o, csvData.length);
        }

        InputStream inputStream = new ByteArrayInputStream(data);

        bc.upload(inputStream, data.length, true);

        String expression = "SELECT _2 from BlobStorage WHERE _1 > 250;";

        BlobQuickQueryDelimitedSerialization input = new BlobQuickQueryDelimitedSerialization()
            .setEscapeChar('\0')
            .setColumnSeparator(',')
            .setRecordSeparator('\n')
            .setFieldQuote('\'')
            .setHeadersPresent(true);

        BlobQuickQueryDelimitedSerialization output = new BlobQuickQueryDelimitedSerialization()
            .setEscapeChar('\0')
            .setColumnSeparator(',')
            .setRecordSeparator('.')
            .setFieldQuote('\'')
            .setHeadersPresent(true);

        BlobQuickQueryClient qqc = new BlobQuickQueryClientBuilder(bc).buildClient();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        qqc.queryWithResponse(os, expression, input, output, null, null);

        ByteArrayOutputStream realOutputStream = new ByteArrayOutputStream();

        DataFileReader<GenericRecord> reader = new DataFileReader<>(
            new SeekableByteArrayInput(os.toByteArray()),
            new GenericDatumReader<>());

        while(reader.hasNext())
        {
            GenericRecord record = reader.next();

            if (record.getSchema().getName().equals("resultData")) {
                BlobQuickQueryResultData resultData = new BlobQuickQueryResultData(new String(((ByteBuffer) record.get("data")).array()));
                realOutputStream.write(((ByteBuffer) record.get("data")).array());
            } else if (record.getSchema().getName().equals("end")) {
                System.out.println("end");
                break;
            } else if (record.getSchema().getName().equals("progress")) {
                BlobQuickQueryProgress progress = new BlobQuickQueryProgress((Long) record.get("bytesScanned"), (Long) record.get("totalBytes"));
                System.out.println("progress: " + progress.getBytesScanned() + "/" + progress.getTotalBytes());
            }  else if (record.getSchema().getName().equals("error")) {
                BlobQuickQueryError error =
                    new BlobQuickQueryError((Boolean) record.get("fatal"), record.get("name").toString(), record.get("description").toString(), (Long) record.get("position"));
                System.out.println("error: " + error.isFatal() + " " + error.getName() + " " + error.getDescription() + " " + error.getPosition());
            } else {
                System.out.println("error parsing stream");
            }
        }
        System.out.println("real output");
        System.out.println(new String(realOutputStream.toByteArray()));
        realOutputStream.close();
        inputStream.close();
        os.close();

    }

}
