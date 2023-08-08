package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ThroughputProperties;

public class ThroughputPropertiesCodeSnippet {
    public static void main(String[] args) throws Exception {

        CosmosAsyncClient client = new CosmosClientBuilder()
                                       .endpoint(TestConfigurations.HOST)
                                       .key(TestConfigurations.MASTER_KEY)
                                       .buildAsyncClient();

        final String databaseName = "testDB";
        int throughput = 5000;
        ThroughputProperties properties = ThroughputProperties.createAutoscaledThroughput(throughput);
        client.createDatabase(databaseName, properties).block();
        client.close();
    }
}
