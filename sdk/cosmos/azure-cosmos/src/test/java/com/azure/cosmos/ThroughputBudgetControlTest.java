package com.azure.cosmos;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.UUID;

public class ThroughputBudgetControlTest extends TestSuiteBase {
    private final static int TIMEOUT = 300000;

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ThroughputBudgetControlTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }


    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public <T> void readItem() throws Exception {
        CosmosAsyncContainer controllerContainer = this.client.getDatabase("controllerDatabase").getContainer("controllerContainer");

        ThroughputBudgetGroupConfig group1 =
            new ThroughputBudgetGroupConfig()
                .groupName("group-1")
                .targetContainer(container)
                .throughputLimit(1)
                .localControlMode()
                .useByDefault(); //change to use as default // necessary parameters in the constructors

        ThroughputBudgetGroupConfig group2 =
            new ThroughputBudgetGroupConfig()
                .groupName("group-2")
                .targetContainer(container)
                .throughputLimit(1)
                .distributedControlMode(
                    new ThroughputBudgetDistributedControlConfig()
                        .controllerContainer(controllerContainer)
                        .documentRenewalInterval(Duration.ofSeconds(1))
                        .documentExpireInterval(Duration.ofSeconds(10))
                );

        this.client.enableThroughputBudgetControl("localHost", group1, group2);


        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setThroughputBudgetGroupName(group1.getGroupName());

        for (int i = 0; i < 5; i++ ) {
            TestObject docDefinition = getDocumentDefinition();
            container.createItem(docDefinition, requestOptions).block();
        }

//        for (int i = 0 ; i < 5; i++) {
//            container.readItem(docDefinition.getId(),
//                new PartitionKey(docDefinition.getMypk()),
//                new CosmosItemRequestOptions(),
//                TestObject.class).block();
//        }

        // requestOptions.setThroughputBudgetGroupName(group1.getGroupName());

//        container.readItem(docDefinition.getId(),
//            new PartitionKey(docDefinition.getMypk()),
//            new CosmosItemRequestOptions(),
//            TestObject.class).block();
    }

    @BeforeClass(groups = { "simple", "non-emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ThroughputBudgetControllerTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);

        container = getSharedMultiPartitionCosmosContainer(client);
    }

    private static TestObject getDocumentDefinition() {
        return new TestObject(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
    }

    static class TestObject {
        String id;
        String mypk;
        String prop;

        public TestObject() {
        }

        public TestObject(String id, String mypk, String prop) {
            this.id = id;
            this.mypk = mypk;
            this.prop = prop;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getProp() {
            return prop;
        }

        public void setProp(String prop) {
            this.prop = prop;
        }
    }
}
