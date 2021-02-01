// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.dotnet.benchmark.Main;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicOperationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicOperationTest.class.getSimpleName());
    private static final int TIMEOUT = 600000;

    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdTestContainer;
    private CosmosContainer createdResultsContainer;

    @BeforeClass(groups = {"emulator"}, timeOut = TIMEOUT)
    public void before_BasicOperationTest() {
        assertThat(this.client).isNull();
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
           .credential(new AzureKeyCredential(TestConfigurations.MASTER_KEY))
           .directMode();

        this.client = clientBuilder.buildClient();
        String suffix = "_" + UUID.randomUUID().toString();
        String testContainerName = "test" + suffix;
        String resultsContainerName = "results" + suffix;
        this.client.createDatabase("db" + suffix);
        this.createdDatabase = this.client.getDatabase("db" + suffix);
        this.createdDatabase.createContainer(
            "test" + suffix,
            "/pk",
            ThroughputProperties.createManualThroughput(20000));
        this.createdTestContainer = this.createdDatabase.getContainer("test" + suffix);
        this.createdDatabase.createContainer(
            "results" + suffix,
            "/pk",
            ThroughputProperties.createManualThroughput(20000));
        this.createdResultsContainer = this.createdDatabase.getContainer("results" + suffix);
    }

    @AfterClass(groups = {"emulator"}, timeOut = TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        assertThat(this.createdDatabase).isNotNull();
        this.createdDatabase.delete();
        this.client.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void Run_InsertWithoutExplicitPKAndId() throws Exception {
        this.runTest(
            "InsertWithoutExplicitPKAndId",
            2, 2000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void Run_Insert() throws Exception {
        this.runTest(
            "Insert",
            2, 2000);
    }

    private void runTest(String workload, int concurrency, int numberOfIterations) throws Exception {
        String runId = workload + "_" + UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));

        String[] args = new String[] {
            "-e", TestConfigurations.HOST,
            "-k", TestConfigurations.MASTER_KEY,
            "--database", this.createdDatabase.getId(),
            "--container", this.createdTestContainer.getId(),
            "-pl", String.valueOf(concurrency),
            "-n", String.valueOf(2000),
            "-w", "InsertWithoutExplicitPKAndId",
            "--enableLatencyPercentiles",
            "--traceFailures",
            "--runId", runId,
            "--commitId", runId,
            "--commitDate", String.format(
                "%d-%02d-%02d",
                 now.getYear(),
                 now.getMonthValue(),
                 now.getDayOfMonth()),
            "--commitTime", String.format(
                "%02d:%02d:%02d.%d",
                now.getHour(),
                now.getMinute(),
                now.getSecond(),
                now.getNano()),
            "--resultsPartitionKeyValue", runId,
            "--resultsContainer", createdResultsContainer.getId(),
            "--publishResults"
        };

        Main.main(args);

        LOGGER.info("Finished test run '" + runId + "'");

        List<ObjectNode> results = this
            .createdResultsContainer
            .queryItems(
                "SELECT * FROM c WHERE c.pk = '" + runId + "'",
                null,
                ObjectNode.class)
            .stream()
            .collect(Collectors.toList());

        assertThat(results)
            .isNotNull()
            .hasSize(1);

        ObjectNode result = results.get(0);

        assertThat(result)
            .isNotNull();

        assertThat(result.get("TotalOps"))
            .isNotNull();
        assertThat(result.get("TotalOps").asInt(-1))
            .isPositive();

        LOGGER.info("Results for test run '" + runId + "' ...");
        LOGGER.info(String.format("Total ops: %,.2f", result.get("TotalOps").asDouble()));
        LOGGER.info(String.format("Top10 Rps: %,.2f", result.get("Top10PercentAverageRps").asDouble()));
        LOGGER.info(String.format("Top20 Rps: %,.2f", result.get("Top20PercentAverageRps").asDouble()));
        LOGGER.info(String.format("Top30 Rps: %,.2f", result.get("Top30PercentAverageRps").asDouble()));
        LOGGER.info(String.format("P50 latency: %,.2f", result.get("Top50PercentLatencyInMs").asDouble()));
        LOGGER.info(String.format("P75 latency: %,.2f", result.get("Top75PercentLatencyInMs").asDouble()));
        LOGGER.info(String.format("P95 latency Rps: %,.2f", result.get("Top95PercentAverageRps").asDouble()));
    }
}
