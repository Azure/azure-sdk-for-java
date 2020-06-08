package com.azure.data.tables;

import com.azure.data.tables.implementation.AzureTableImpl;
import com.azure.data.tables.implementation.AzureTableImplBuilder;
import com.azure.data.tables.implementation.TablesImpl;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class autorestTest {

    private AzureTableImpl createClient() {
        final String connectionString = System.getenv("azure_tables_connection_string");
        final ServiceBusSharedKeyCredential credential = new ServiceBusSharedKeyCredential(
            properties.getSharedAccessKeyName(), properties.getSharedAccessKey());
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new ServiceBusTokenCredentialHttpPolicy(credential));
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        final HttpClient httpClientToUse;
        if (interceptorManager.isPlaybackMode()) {
            httpClientToUse = interceptorManager.getPlaybackClient();
        } else {
            httpClientToUse = httpClient;
            policies.add(interceptorManager.getRecordPolicy());
            policies.add(new RetryPolicy());
        }

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClientToUse)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();

        return new ServiceBusManagementClientImplBuilder()
            .serializer(serializer)
            .endpoint(properties.getEndpoint().getHost())
            .apiVersion("2017-04")
            .pipeline(pipeline)
            .buildClient();
    }






    @Test
    void createTableTest() {
        AzureTableImplBuilder atib = new AzureTableImplBuilder()
            .url()
            .pipeline()

    }


    @Test
    void createAndUpdateTable() throws InterruptedException {
    }

    /**
     * We've fixed this by caching the result of the `createTable` operation if it is successful. So we don't try to
     * create the table again.
     *
     * See {@link #createAndUpdateTable()}
     */
    @Test
    void createAndUpdateTableFixed() throws InterruptedException {

    }
}
