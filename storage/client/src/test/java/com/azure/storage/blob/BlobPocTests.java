package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.BlockLookupList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class BlobPocTests {

    @Test
    public void testCreateBlob() {
        AzureBlobStorage client = new AzureBlobStorageImpl(new HttpPipeline(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888)))/*,
            new HttpPipelinePolicy() {
                @Override
                public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                    String url = context.httpRequest().url().toString();
                    String sasToken = System.getenv("AZURE_STORAGE_SAS_TOKEN");
                    if (url.contains("?")) {
                        sasToken = sasToken.replaceFirst("\\?", "&");
                    }
                    url += sasToken;
                    try {
                        context.withHttpRequest(context.httpRequest().withUrl(new URL(url)));
                    } catch (MalformedURLException e) {
                        return Mono.error(e);
                    }
                    return next.process();
                }
        }*/)).withUrl("https://" + System.getenv("AZURE_STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/mycontainer/random" + System.getenv("AZURE_STORAGE_SAS_TOKEN"));

        Random random = new Random();

        byte[] randomBytes = new byte[4096];
        random.nextBytes(randomBytes);
        ByteBuf bb = Unpooled.wrappedBuffer(randomBytes);
        String base64 = Base64.encodeBase64String("0001".getBytes(StandardCharsets.UTF_8));
        client.blockBlobs().stageBlockWithRestResponseAsync(null, null, base64, 4096, Flux.just(bb)).block();
        client.blockBlobs().commitBlockListWithRestResponseAsync(null, null, new BlockLookupList().withLatest(Arrays.asList(base64))).block();
    }
}
