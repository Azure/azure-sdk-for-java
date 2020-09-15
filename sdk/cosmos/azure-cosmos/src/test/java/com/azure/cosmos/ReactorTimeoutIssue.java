package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ReactorTimeoutIssue {
    private static final Logger logger = LoggerFactory.getLogger(ReactorTimeoutIssue.class);
    static final String DATABASE_NAME = "test_database";
    static final String CONTAINER_NAME = "test_container";
    private static final Properties properties = System.getProperties();
    public final static String MASTER_KEY =
        properties.getProperty("ACCOUNT_KEY",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("ACCOUNT_KEY")),
                ""));
    public final static String HOST =
        properties.getProperty("ACCOUNT_HOST",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("ACCOUNT_HOST")),
                ""));
    private CosmosAsyncClient asyncClient;
    private CosmosAsyncContainer asyncContainer;
    private Meter successMeter;
    private Meter failureMeter;
    private Meter operationsMeter;
    private Meter launchedMeter;

    private List<MyItem> itemList = new ArrayList<>();

    private static final int REPORT_SECS = 20;

    public static void main(String[] args) {
        ReactorTimeoutIssue m = new ReactorTimeoutIssue();
        int numOps = 100000;
        if (args.length >= 1) {
            numOps = Integer.parseInt(args[0]);
        }
        m.runTest(numOps);
        logger.info("Started");
    }

    private void runTest(int numOps) {
        logger.info("Initializing client");
        initializeClient();
        logger.info("Initializing metrics");
        initMetrics();

        logger.info("Running test: Metrics will be printed periodically every " + REPORT_SECS + " seconds");
        Random random = new Random();
        for (int i = 0; i < numOps; i++) {
            launchedMeter.mark();
            int operation = random.nextInt(4);
            int listIndex = random.nextInt(itemList.size());
            executeOperation(operation, itemList.get(listIndex));
        }
    }

    private void trySetupData(CosmosAsyncClient client) {
        client.createDatabaseIfNotExists(DATABASE_NAME)
              .flatMap(cosmosDatabaseResponse -> {
                  CosmosAsyncDatabase database = client.getDatabase(cosmosDatabaseResponse
                      .getProperties()
                      .getId());
                  return database.createContainerIfNotExists(CONTAINER_NAME,
                      "/myPk", 10000);
              }).block();
        asyncContainer = asyncClient.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
        //  Adding sample documents
        for (int i = 0; i < 100; i++) {
            MyItem myItem = new MyItem(UUID.randomUUID().toString(), "item - " + i, i, String.valueOf(i));
            CosmosItemResponse<MyItem> cosmosItemResponse = asyncContainer.createItem(myItem).block();
            assert cosmosItemResponse != null;
            itemList.add(cosmosItemResponse.getItem());
        }
        logger.info("Item list : {}", itemList.size());
    }

    private void initMetrics() {

        MetricRegistry metricsRegistry = new MetricRegistry();
        successMeter = metricsRegistry.meter("success-meter");
        failureMeter = metricsRegistry.meter("failure-meter");
        operationsMeter = metricsRegistry.meter("completed-meter");
        launchedMeter = metricsRegistry.meter("launched-meter");

        ScheduledReporter reporter = ConsoleReporter.forRegistry(metricsRegistry)
                                                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                    .convertRatesTo(TimeUnit.SECONDS)
                                                    .build();
        reporter.start(REPORT_SECS, TimeUnit.SECONDS);

    }

    private void initializeClient() {
        asyncClient = new CosmosClientBuilder()
            .endpoint(HOST)
            .key(MASTER_KEY)
            .directMode(DirectConnectionConfig.getDefaultConfig().setIdleEndpointTimeout(Duration.ofSeconds(10)))
            .consistencyLevel(ConsistencyLevel.SESSION)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();
        logger.info("Trying to setup data");
        trySetupData(asyncClient);
    }

    private void executeOperation(int operation, MyItem item) {
        Subscriber<MyItem> responseSubscriber =
            new BaseSubscriber<>() {
                @Override
                protected void hookOnNext(MyItem myItem) {
                    successMeter.mark();
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    logger.error("Error occurred : ", throwable);
                    failureMeter.mark();
                }

                @Override
                protected void hookOnComplete() {
                    operationsMeter.mark();
                }
            };
        Flux<MyItem> itemResponseFlux;
        switch (operation) {
            case 0:
                //  read operation
                logger.info("Read operation");
                itemResponseFlux = asyncContainer.readItem(item.getId(), new PartitionKey(item.getMyPk()), MyItem.class)
                                                 .flatMap(cosmosItemResponse -> Mono.just(cosmosItemResponse.getItem()))
                                                 .timeout(Duration.ofSeconds(5))
                                                 .flux();
                itemResponseFlux.subscribe(responseSubscriber);
                return;
            case 1:
                //  write operation
                logger.info("Write operation");
                itemResponseFlux = asyncContainer.createItem(new MyItem(item))
                                                 .flatMap(cosmosItemResponse -> Mono.just(cosmosItemResponse.getItem()))
                                                 .timeout(Duration.ofSeconds(5))
                                                 .flux();
                itemResponseFlux.subscribe(responseSubscriber);
                return;
            case 2:
                //  upsert operation
                logger.info("Upsert operation");
                itemResponseFlux = asyncContainer.upsertItem(item)
                                                 .flatMap(cosmosItemResponse -> Mono.just(cosmosItemResponse.getItem()))
                                                 .timeout(Duration.ofSeconds(5))
                                                 .flux();
                itemResponseFlux.subscribe(responseSubscriber);
                return;
            case 3:
                //  query operation
                logger.info("Query operation");
                CosmosPagedFlux<MyItem> myItemCosmosPagedFlux = asyncContainer.queryItems("select * from c where c.id"
                    + " = '" + item.getId() + "' and c.myPk = '" + item.getMyPk() + "'", MyItem.class);
                itemResponseFlux = myItemCosmosPagedFlux.byPage()
                                                        .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                                                        .timeout(Duration.ofSeconds(5));
                itemResponseFlux.subscribe(responseSubscriber);
                return;
        }
    }

    private static class MyItem {
        private String id;
        private String itemName;
        private Integer itemCount;
        private String myPk;

        public MyItem() {

        }

        public MyItem(String id, String itemName, Integer itemCount, String myPk) {
            this.id = id;
            this.itemName = itemName;
            this.itemCount = itemCount;
            this.myPk = myPk;
        }

        public MyItem(MyItem item) {
            this.id = UUID.randomUUID().toString();
            this.itemName = item.getItemName();
            this.itemCount = item.getItemCount();
            this.myPk = item.getMyPk();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public Integer getItemCount() {
            return itemCount;
        }

        public void setItemCount(Integer itemCount) {
            this.itemCount = itemCount;
        }

        public String getMyPk() {
            return myPk;
        }

        public void setMyPk(String myPk) {
            this.myPk = myPk;
        }

        @Override
        public String toString() {
            return "MyItem{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemCount=" + itemCount +
                ", myPk='" + myPk + '\'' +
                '}';
        }
    }
}
