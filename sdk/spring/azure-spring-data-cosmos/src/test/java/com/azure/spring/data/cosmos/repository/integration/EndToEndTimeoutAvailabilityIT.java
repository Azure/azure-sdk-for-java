// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.BasicItem;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.repository.query.Param;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runnable cross-partition query sample with a client-wide timeout and availability strategy.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EndToEndTimeoutAvailabilityIT.SampleConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Execution(ExecutionMode.SAME_THREAD)
public class EndToEndTimeoutAvailabilityIT {

    @Autowired
    private SampleRepository repository;

    @Autowired
    private SampleReactiveRepository reactiveRepository;

    @Autowired
    private CosmosFactory cosmosFactory;

    @Autowired
    private CosmosEndToEndOperationLatencyPolicyConfig latencyPolicy;

    @Autowired
    private CosmosTemplate template;

    @Value("${cosmos.preferredRegions:${PREFERRED_LOCATIONS:}}")
    private String preferredRegions;

    @Autowired
    private ResponseDiagnosticsTestUtils diagnostics;

    private BasicItem item;
    private BasicItem otherPartitionItem;
    private String idPrefix;

    @BeforeEach
    public void setUp() {
        idPrefix = "timeout-availability-" + UUID.randomUUID();
        // BasicItem uses id as its partition key, so these items belong to different logical partitions.
        item = new BasicItem(idPrefix + "-one");
        otherPartitionItem = new BasicItem(idPrefix + "-two");
        repository.save(item);
        repository.save(otherPartitionItem);
        repository.findById(item.getId(), new PartitionKey(item.getId()));
    }

    @AfterEach
    public void cleanUp() {
        try {
            repository.deleteById(item.getId(), new PartitionKey(item.getId()));
        } finally {
            repository.deleteById(otherPartitionItem.getId(), new PartitionKey(otherPartitionItem.getId()));
        }
    }

    @Test
    public void repositoriesExecuteCrossPartitionQueryWithLatencyPolicy() {
        assertThat(repository.findByIdPrefix(idPrefix)).containsExactlyInAnyOrder(item, otherPartitionItem);

        StepVerifier.create(reactiveRepository.findByIdPrefix(idPrefix).collectList())
            .assertNext(items -> assertThat(items).containsExactlyInAnyOrder(item, otherPartitionItem))
            .expectComplete()
            .verify(operationTimeout().plusSeconds(5));
    }

    @Test
    public void configuredTimeoutCancelsCrossPartitionQueryWhenDataResponsesAreDelayed() {
        FaultInjectionRule delayRule = delayRule(new FaultInjectionConditionBuilder()
            .operationType(FaultInjectionOperationType.QUERY_ITEM)
            .connectionType(FaultInjectionConnectionType.DIRECT), FaultInjectionServerErrorType.RESPONSE_DELAY);
        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container(), Collections.singletonList(delayRule))
                .block();

            long start = System.nanoTime();
            assertOperationTimeout(assertThrows(CosmosAccessException.class,
                () -> repository.findByIdPrefix(idPrefix)));
            assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(operationTimeout().plusSeconds(5));

            StepVerifier.create(reactiveRepository.findByIdPrefix(idPrefix).collectList())
                .expectErrorSatisfies(EndToEndTimeoutAvailabilityIT::assertOperationTimeout)
                .verify(operationTimeout().plusSeconds(5));
            assertThat(delayRule.getHitCount()).isGreaterThanOrEqualTo(2);
        } finally {
            delayRule.disable();
        }
    }

    @ParameterizedTest(name = "QueryPlan recovery with {0} in the first preferred region")
    @EnumSource(value = FaultInjectionServerErrorType.class, names = {"CONNECTION_DELAY", "RESPONSE_DELAY"})
    public void availabilityStrategyHedgesQueryPlanForCrossPartitionQuery(FaultInjectionServerErrorType delayType) {
        List<String> regions = Arrays.asList(StringUtils.tokenizeToStringArray(preferredRegions, ","));
        assumeTrue(regions.size() > 1, "Set cosmos.preferredRegions to at least two account read regions.");

        // Warm routing metadata; cross-partition queries still retrieve their query plans on subsequent executions.
        assertThat(repository.findByIdPrefix(idPrefix)).containsExactlyInAnyOrder(item, otherPartitionItem);

        FaultInjectionRule delayRule = delayRule(new FaultInjectionConditionBuilder()
            .region(regions.get(0))
            .operationType(FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN)
            .connectionType(FaultInjectionConnectionType.GATEWAY), delayType);
        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container(), Collections.singletonList(delayRule))
                .block();

            long previousHitCount = delayRule.getHitCount();
            long start = System.nanoTime();
            assertThat(repository.findByIdPrefix(idPrefix)).containsExactlyInAnyOrder(item, otherPartitionItem);
            assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(operationTimeout().plusSeconds(5));
            assertQueryPlanRecovery(delayRule, previousHitCount, regions);

            previousHitCount = delayRule.getHitCount();
            StepVerifier.create(reactiveRepository.findByIdPrefix(idPrefix).collectList())
                .assertNext(items -> assertThat(items).containsExactlyInAnyOrder(item, otherPartitionItem))
                .expectComplete()
                .verify(operationTimeout().plusSeconds(5));
            assertQueryPlanRecovery(delayRule, previousHitCount, regions);
        } finally {
            delayRule.disable();
        }
    }

    private Duration operationTimeout() {
        return latencyPolicy.getEndToEndOperationTimeout();
    }

    private CosmosAsyncContainer container() {
        return cosmosFactory.getCosmosAsyncClient()
            .getDatabase(cosmosFactory.getDatabaseName())
            .getContainer(template.getContainerName(BasicItem.class));
    }

    private void assertQueryPlanRecovery(FaultInjectionRule delayRule, long previousHitCount, List<String> regions) {
        assertThat(delayRule.getHitCount()).isGreaterThan(previousHitCount);
        assertThat(delayRule.getHitCountDetails()).containsOnlyKeys("QueryPlan-Document");
        assertThat(diagnostics.getCosmosDiagnostics()).isNotNull();
        assertThat(diagnostics.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames())
            .anyMatch(region -> !region.equalsIgnoreCase(regions.get(0))
                && regions.stream().anyMatch(preferred -> preferred.equalsIgnoreCase(region)));
    }

    private FaultInjectionRule delayRule(
        FaultInjectionConditionBuilder condition, FaultInjectionServerErrorType delayType) {
        return new FaultInjectionRuleBuilder("slow-query-" + UUID.randomUUID())
            .condition(condition.build())
            .result(FaultInjectionResultBuilders.getResultBuilder(delayType)
                .delay(operationTimeout().plusSeconds(10))
                .build())
            .duration(operationTimeout().multipliedBy(3).plusSeconds(30))
            .build();
    }

    private static void assertOperationTimeout(Throwable error) {
        assertThat(error).isInstanceOf(CosmosAccessException.class);
        CosmosException cosmosException = ((CosmosAccessException) error).getCosmosException();
        assertThat(cosmosException).isNotNull();
        assertThat(cosmosException.getStatusCode()).isEqualTo(408);
        // Distinguishes the client-side end-to-end timeout from a service request timeout.
        assertThat(cosmosException.getSubStatusCode()).isEqualTo(20008);
        assertThat(cosmosException.getDiagnostics()).isNotNull();
    }

    public interface SampleRepository extends CosmosRepository<BasicItem, String> {
        @Query("SELECT * FROM c WHERE STARTSWITH(c.id, @prefix)")
        List<BasicItem> findByIdPrefix(@Param("prefix") String prefix);
    }

    public interface SampleReactiveRepository extends ReactiveCosmosRepository<BasicItem, String> {
        @Query("SELECT * FROM c WHERE STARTSWITH(c.id, @prefix)")
        Flux<BasicItem> findByIdPrefix(@Param("prefix") String prefix);
    }

    @Configuration
    @PropertySource("classpath:application.properties")
    @PropertySource(value = "file:${cosmos.test.properties:${user.home}/cosmos-v4.properties}",
        ignoreResourceNotFound = true)
    @EnableCosmosRepositories(basePackageClasses = EndToEndTimeoutAvailabilityIT.class,
        considerNestedRepositories = true)
    @EnableReactiveCosmosRepositories(basePackageClasses = EndToEndTimeoutAvailabilityIT.class,
        considerNestedRepositories = true)
    public static class SampleConfiguration extends AbstractCosmosConfiguration {

        @Value("${cosmos.uri}")
        private String uri;

        @Value("${cosmos.key}")
        private String key;

        @Value("${cosmos.database:testdb}")
        private String database;

        @Value("${cosmos.preferredRegions:${PREFERRED_LOCATIONS:}}")
        private String preferredRegions;

        @Bean
        public CosmosEndToEndOperationLatencyPolicyConfig latencyPolicy(
            @Value("${cosmos.endToEndTimeout:PT5S}") String timeout,
            @Value("${cosmos.availability.threshold:PT0.5S}") String threshold,
            @Value("${cosmos.availability.thresholdStep:PT0.1S}") String thresholdStep) {

            Duration operationTimeout = Duration.parse(timeout);
            Duration availabilityThreshold = Duration.parse(threshold);
            Assert.isTrue(operationTimeout.compareTo(availabilityThreshold) > 0,
                "cosmos.endToEndTimeout must exceed cosmos.availability.threshold");
            return new CosmosEndToEndOperationLatencyPolicyConfigBuilder(operationTimeout)
                .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(
                    availabilityThreshold, Duration.parse(thresholdStep)))
                .build();
        }

        @Bean
        public CosmosClientBuilder cosmosClientBuilder(CosmosEndToEndOperationLatencyPolicyConfig latencyPolicy) {
            return new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .directMode()
                .preferredRegions(Arrays.asList(StringUtils.tokenizeToStringArray(preferredRegions, ",")))
                .endToEndOperationLatencyPolicyConfig(latencyPolicy);
        }

        @Bean
        public ResponseDiagnosticsTestUtils responseDiagnostics() {
            return new ResponseDiagnosticsTestUtils();
        }

        @Bean
        @Override
        public CosmosConfig cosmosConfig() {
            return CosmosConfig.builder()
                .responseDiagnosticsProcessor(responseDiagnostics().getResponseDiagnosticsProcessor())
                .build();
        }

        @Override
        protected String getDatabaseName() {
            return database;
        }
    }
}
