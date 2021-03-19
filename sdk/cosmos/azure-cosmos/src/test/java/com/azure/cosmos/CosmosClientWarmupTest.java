package com.azure.cosmos;

import com.azure.core.http.ProxyOptions;
import com.azure.cosmos.implementation.TestConfigurations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class CosmosClientWarmupTest {

    private CosmosAsyncClient cosmosAsyncClient;

    @BeforeClass(groups = {"simple"})
    public void beforeClass() {

    }
    @AfterClass(groups = {"simple"},  alwaysRun = true)
    public void afterClass() {
        if (this.cosmosAsyncClient != null) {
            this.cosmosAsyncClient.close();
        }
    }
    @Test(groups = {"simple"})
    public void buildAsyncClientAndInitializeContainers() {

        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        gatewayConnectionConfig.setProxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)));

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder();
        cosmosClientBuilder.key(TestConfigurations.MASTER_KEY);
        cosmosClientBuilder.endpoint(TestConfigurations.HOST);
        cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig(), gatewayConnectionConfig);

        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"TestDB1", "TestColl1-1"});
        list.add(new String[]{"TestDB1", "TestColl1-2"});

        list.add(new String[]{"TestDB2", "TestColl2-1"});
        list.add(new String[]{"TestDB2", "TestColl2-1"});

        Mono<CosmosAsyncClient> cosmosClientMono = cosmosClientBuilder.buildAsyncClientAndInitializeContainers(list);
        cosmosClientMono.block();
    }
}
