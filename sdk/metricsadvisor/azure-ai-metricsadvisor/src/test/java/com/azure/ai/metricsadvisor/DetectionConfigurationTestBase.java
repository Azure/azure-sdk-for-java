// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectorDirection;
import com.azure.ai.metricsadvisor.administration.models.ChangeThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedDimension;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DetectionConditionOperator;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.administration.models.HardThresholdCondition;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMetric;
import com.azure.ai.metricsadvisor.administration.models.MetricSeriesGroupDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.MetricWholeSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.MetricSingleSeriesDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SqlServerDataFeedSource;
import com.azure.ai.metricsadvisor.administration.models.SmartDetectionCondition;
import com.azure.ai.metricsadvisor.administration.models.SuppressCondition;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.azure.ai.metricsadvisor.TestUtils.INGESTION_START_TIME;
import static com.azure.ai.metricsadvisor.TestUtils.SQL_SERVER_CONNECTION_STRING;
import static com.azure.ai.metricsadvisor.TestUtils.TEMPLATE_QUERY;

public abstract class DetectionConfigurationTestBase extends MetricsAdvisorAdministrationClientTestBase {
    public abstract void createDetectionConfigurationForWholeSeries(HttpClient httpClient,
                                                                     MetricsAdvisorServiceVersion serviceVersion);

    protected static class CreateDetectionConfigurationForWholeSeriesInput {
        static final CreateDetectionConfigurationForWholeSeriesInput INSTANCE
            = new CreateDetectionConfigurationForWholeSeriesInput();

        final String detectionConfigName = "testdetectionconfig" + UUID.randomUUID();
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                50,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(13, 50)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final AnomalyDetectionConfiguration detectionConfiguration
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription("test metric anomaly detection configuration")
            .setWholeSeriesDetectionCondition(wholeSeriesCondition);
    }

    protected void assertCreateDetectionConfigurationForWholeSeriesOutput(
        AnomalyDetectionConfiguration config, String expectedMetricId) {

        Assertions.assertNotNull(config.getId());
        Assertions.assertEquals(expectedMetricId, config.getMetricId());
        Assertions.assertEquals(config.getDescription(),
            CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.detectionConfiguration.getDescription());
        Assertions.assertNotNull(config.getName());
        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertNotNull(config.getSeriesGroupDetectionConditions());
        Assertions.assertEquals(0, config.getSeriesDetectionConditions().size());
        Assertions.assertEquals(0, config.getSeriesGroupDetectionConditions().size());

        final MetricWholeSeriesDetectionCondition createdWholeSeriesCondition
            = config.getWholeSeriesDetectionCondition();
        final MetricWholeSeriesDetectionCondition expectedWholeSeriesCondition
            = CreateDetectionConfigurationForWholeSeriesInput.INSTANCE.wholeSeriesCondition;

        Assertions.assertNotNull(createdWholeSeriesCondition);
        Assertions.assertEquals(expectedWholeSeriesCondition.getConditionOperator(),
            createdWholeSeriesCondition.getConditionOperator());
        // WholeSeries::SmartDetection
        Assertions.assertNotNull(createdWholeSeriesCondition.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getSmartDetectionCondition().getSensitivity(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::ChangeThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedWholeSeriesCondition.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::HardThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getLowerBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getUpperBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio());
    }

    public abstract void createDetectionConfigurationForSeriesAndGroup(HttpClient httpClient,
                                                                    MetricsAdvisorServiceVersion serviceVersion);

    protected static class CreateDetectionConfigurationForSeriesAndGroupInput {
        static final CreateDetectionConfigurationForSeriesAndGroupInput INSTANCE
            = new CreateDetectionConfigurationForSeriesAndGroupInput();

        final String detectionConfigName = "testdetectionconfig" + UUID.randomUUID();
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setConditionOperator(DetectionConditionOperator.AND)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                50,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(13, 50)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final DimensionKey seriesKey = new DimensionKey()
            .put("city", "Shenzhen")
            .put("category", "Jewelry");

        final MetricSingleSeriesDetectionCondition seriesCondition
            = new MetricSingleSeriesDetectionCondition(seriesKey)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)));

        final DimensionKey seriesGroupKey = new DimensionKey()
            .put("city", "Sao Paulo");

        final MetricSeriesGroupDetectionCondition seriesGroupCondition
            = new MetricSeriesGroupDetectionCondition(seriesGroupKey)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)));

        final AnomalyDetectionConfiguration detectionConfiguration
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription("test metric anomaly detection configuration")
            .setWholeSeriesDetectionCondition(wholeSeriesCondition)
            .addSingleSeriesDetectionCondition(seriesCondition)
            .addSeriesGroupDetectionCondition(seriesGroupCondition);
    }

    protected void assertCreateDetectionConfigurationForSeriesAndGroupOutput(
        AnomalyDetectionConfiguration config, String expectedMetricId) {

        Assertions.assertNotNull(config.getId());
        Assertions.assertEquals(expectedMetricId, config.getMetricId());
        Assertions.assertEquals(config.getDescription(),
            CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.detectionConfiguration.getDescription());
        Assertions.assertNotNull(config.getName());
        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertNotNull(config.getSeriesGroupDetectionConditions());

        final MetricWholeSeriesDetectionCondition createdWholeSeriesCondition = config.getWholeSeriesDetectionCondition();
        final MetricWholeSeriesDetectionCondition expectedWholeSeriesCondition
            = CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.wholeSeriesCondition;

        Assertions.assertNotNull(createdWholeSeriesCondition);
        Assertions.assertEquals(expectedWholeSeriesCondition.getConditionOperator(),
            createdWholeSeriesCondition.getConditionOperator());
        // WholeSeries::SmartDetection
        Assertions.assertNotNull(createdWholeSeriesCondition.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getSmartDetectionCondition().getSensitivity(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::ChangeThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedWholeSeriesCondition.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::HardThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getLowerBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getUpperBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertEquals(1, config.getSeriesDetectionConditions().size());
        final MetricSingleSeriesDetectionCondition createdSeriesCondition = config
            .getSeriesDetectionConditions()
            .get(0);
        Assertions.assertNotNull(createdSeriesCondition);
        final DimensionKey expectedSeriesKey
            = CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.seriesKey;
        Assertions.assertEquals(expectedSeriesKey, createdSeriesCondition.getSeriesKey());

        final MetricSingleSeriesDetectionCondition expectedSeriesCondition
            = CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.seriesCondition;
        // Series::SmartDetection
        Assertions.assertNotNull(createdSeriesCondition.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesCondition.getSmartDetectionCondition().getSensitivity(),
            createdSeriesCondition.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesCondition.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber());

        Assertions.assertNotNull(config.getSeriesGroupDetectionConditions());
        Assertions.assertEquals(1, config.getSeriesGroupDetectionConditions().size());
        final MetricSeriesGroupDetectionCondition createdSeriesGroupCondition = config
            .getSeriesGroupDetectionConditions()
            .get(0);
        Assertions.assertNotNull(createdSeriesGroupCondition);
        final DimensionKey expectedSeriesGroupKey
            = CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.seriesGroupKey;
        Assertions.assertEquals(expectedSeriesGroupKey, createdSeriesGroupCondition.getSeriesGroupKey());

        final MetricSeriesGroupDetectionCondition expectedSeriesGroupCondition
            = CreateDetectionConfigurationForSeriesAndGroupInput.INSTANCE.seriesGroupCondition;
        // SeriesGroup::SmartDetection
        Assertions.assertNotNull(createdSeriesGroupCondition.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition.getSmartDetectionCondition().getSensitivity(),
            createdSeriesGroupCondition.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesGroupCondition.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesGroupCondition.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesGroupCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
    }

    public abstract void createDetectionConfigurationForMultipleSeriesAndGroup(HttpClient httpClient,
                                                                       MetricsAdvisorServiceVersion serviceVersion);

    protected static class CreateDetectionConfigurationForMultipleSeriesAndGroupInput {
        static final CreateDetectionConfigurationForMultipleSeriesAndGroupInput INSTANCE
            = new CreateDetectionConfigurationForMultipleSeriesAndGroupInput();

        final String detectionConfigName = "testdetectionconfig" + UUID.randomUUID();
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setConditionOperator(DetectionConditionOperator.AND)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                50,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(13, 50)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final DimensionKey seriesKey1 = new DimensionKey()
            .put("city", "Shenzhen")
            .put("category", "Jewelry");

        final MetricSingleSeriesDetectionCondition seriesCondition1
            = new MetricSingleSeriesDetectionCondition(seriesKey1)
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final DimensionKey seriesKey2 = new DimensionKey()
            .put("city", "Osaka")
            .put("category", "Cell Phones");

        final MetricSingleSeriesDetectionCondition seriesCondition2
            = new MetricSingleSeriesDetectionCondition(seriesKey2)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)));

        final DimensionKey seriesGroupKey1 = new DimensionKey()
            .put("city", "Sao Paulo");

        final MetricSeriesGroupDetectionCondition seriesGroupCondition1
            = new MetricSeriesGroupDetectionCondition(seriesGroupKey1)
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final DimensionKey seriesGroupKey2 = new DimensionKey()
            .put("city", "Seoul");

        final MetricSeriesGroupDetectionCondition seriesGroupCondition2
            = new MetricSeriesGroupDetectionCondition(seriesGroupKey2)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)));

        final AnomalyDetectionConfiguration detectionConfiguration
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription("test metric anomaly detection configuration")
            .setWholeSeriesDetectionCondition(wholeSeriesCondition)
            .addSingleSeriesDetectionCondition(seriesCondition1)
            .addSingleSeriesDetectionCondition(seriesCondition2)
            .addSeriesGroupDetectionCondition(seriesGroupCondition1)
            .addSeriesGroupDetectionCondition(seriesGroupCondition2);
    }

    protected void assertCreateDetectionConfigurationForMultipleSeriesAndGroupOutput(
        AnomalyDetectionConfiguration config, String expectedMetricId) {

        Assertions.assertNotNull(config.getId());
        Assertions.assertEquals(expectedMetricId, config.getMetricId());
        Assertions.assertEquals(config.getDescription(),
            CreateDetectionConfigurationForMultipleSeriesAndGroupInput
                .INSTANCE.detectionConfiguration.getDescription());
        Assertions.assertNotNull(config.getName());
        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertNotNull(config.getSeriesGroupDetectionConditions());

        final MetricWholeSeriesDetectionCondition createdWholeSeriesCondition
            = config.getWholeSeriesDetectionCondition();
        final MetricWholeSeriesDetectionCondition expectedWholeSeriesCondition
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.wholeSeriesCondition;

        Assertions.assertNotNull(createdWholeSeriesCondition);
        Assertions.assertEquals(expectedWholeSeriesCondition.getConditionOperator(),
            createdWholeSeriesCondition.getConditionOperator());
        // WholeSeries::SmartDetection
        Assertions.assertNotNull(createdWholeSeriesCondition.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getSmartDetectionCondition().getSensitivity(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::ChangeThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedWholeSeriesCondition.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::HardThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getLowerBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getUpperBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertEquals(2, config.getSeriesDetectionConditions().size());

        final DimensionKey expectedSeriesKey1
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesKey1;

        Optional<MetricSingleSeriesDetectionCondition> createdSeriesCondition1Opt
            = config.getSeriesDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesKey().equals(expectedSeriesKey1))
            .findFirst();

        Assertions.assertTrue(createdSeriesCondition1Opt.isPresent());
        final MetricSingleSeriesDetectionCondition createdSeriesCondition1 = createdSeriesCondition1Opt.get();
        Assertions.assertNotNull(createdSeriesCondition1);

        final MetricSingleSeriesDetectionCondition expectedSeriesCondition1
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesCondition1;
        Assertions.assertEquals(createdSeriesCondition1.getConditionOperator(),
            expectedSeriesCondition1.getConditionOperator());
        // Series1::SmartDetection
        Assertions.assertNotNull(createdSeriesCondition1.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getSensitivity(),
            createdSeriesCondition1.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesCondition1.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        // Series1::ChangeThreshold
        Assertions.assertNotNull(createdSeriesCondition1.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesCondition1.getChangeThresholdCondition().getChangePercentage(),
            createdSeriesCondition1.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedSeriesCondition1.getChangeThresholdCondition().getShiftPoint(),
            createdSeriesCondition1.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedSeriesCondition1.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // Series1::HardThreshold
        Assertions.assertNotNull(createdSeriesCondition1.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition1.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesCondition1.getHardThresholdCondition().getLowerBound(),
            createdSeriesCondition1.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedSeriesCondition1.getHardThresholdCondition().getUpperBound(),
            createdSeriesCondition1.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        final DimensionKey expectedSeriesKey2
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesKey2;

        Optional<MetricSingleSeriesDetectionCondition> createdSeriesCondition2Opt
            = config.getSeriesDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesKey().equals(expectedSeriesKey2))
            .findFirst();

        Assertions.assertTrue(createdSeriesCondition2Opt.isPresent());
        final MetricSingleSeriesDetectionCondition createdSeriesCondition2 = createdSeriesCondition2Opt.get();
        Assertions.assertNotNull(createdSeriesCondition2);
        final MetricSingleSeriesDetectionCondition expectedSeriesCondition2
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesCondition2;
        // Series2::SmartDetection
        Assertions.assertNotNull(createdSeriesCondition2.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesCondition2.getSmartDetectionCondition().getSensitivity(),
            createdSeriesCondition2.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesCondition2.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition2.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesCondition2.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesCondition2.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition2.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesCondition2.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition2.getSmartDetectionCondition().getSuppressCondition().getMinNumber());

        Assertions.assertNotNull(config.getSeriesGroupDetectionConditions());
        Assertions.assertEquals(2, config.getSeriesGroupDetectionConditions().size());

        final DimensionKey expectedSeriesGroupKey1
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesGroupKey1;

        Optional<MetricSeriesGroupDetectionCondition> createdSeriesGroupCondition1Opt = config
            .getSeriesGroupDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesGroupKey().equals(expectedSeriesGroupKey1))
            .findFirst();

        Assertions.assertTrue(createdSeriesGroupCondition1Opt.isPresent());

        final MetricSeriesGroupDetectionCondition createdSeriesGroupCondition1 = createdSeriesGroupCondition1Opt.get();
        Assertions.assertNotNull(createdSeriesGroupCondition1);

        final MetricSeriesGroupDetectionCondition expectedSeriesGroupCondition1
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesGroupCondition1;
        Assertions.assertEquals(createdSeriesGroupCondition1.getConditionOperator(),
            expectedSeriesGroupCondition1.getConditionOperator());
        // SeriesGroup1::SmartDetection
        Assertions.assertNotNull(createdSeriesGroupCondition1.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getSensitivity(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection());
        Assertions.assertNotNull(createdSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        // SeriesGroup1::ChangeThreshold
        Assertions.assertNotNull(createdSeriesGroupCondition1.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getChangeThresholdCondition().getChangePercentage(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getChangeThresholdCondition().getShiftPoint(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedSeriesGroupCondition1.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // SeriesGroup1::HardThreshold
        Assertions.assertNotNull(createdSeriesGroupCondition1.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getHardThresholdCondition().getLowerBound(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getHardThresholdCondition().getUpperBound(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        final DimensionKey expectedSeriesGroupKey2
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesGroupKey2;

        Optional<MetricSeriesGroupDetectionCondition> createdSeriesGroupCondition2Opt = config
            .getSeriesGroupDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesGroupKey().equals(expectedSeriesGroupKey2))
            .findFirst();

        Assertions.assertTrue(createdSeriesGroupCondition2Opt.isPresent());

        final MetricSeriesGroupDetectionCondition createdSeriesGroupCondition2 = createdSeriesGroupCondition2Opt.get();
        Assertions.assertNotNull(createdSeriesGroupCondition2);

        final MetricSeriesGroupDetectionCondition expectedSeriesGroupCondition2
            = CreateDetectionConfigurationForMultipleSeriesAndGroupInput.INSTANCE.seriesGroupCondition2;
        // SeriesGroup1::SmartDetection
        Assertions.assertNotNull(createdSeriesGroupCondition2.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getSensitivity(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
    }

    public abstract void updateDetectionConfiguration(HttpClient httpClient,
                                                      MetricsAdvisorServiceVersion serviceVersion);

    protected static class UpdateDetectionConfigurationInput {
        static final UpdateDetectionConfigurationInput INSTANCE
            = new UpdateDetectionConfigurationInput();

        final String detectionConfigName = "testdetectionconfig" + UUID.randomUUID();
        final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition()
            .setConditionOperator(DetectionConditionOperator.AND)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                50,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(13, 50)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final DimensionKey seriesKey1 = new DimensionKey()
            .put("city", "Shenzhen")
            .put("category", "Jewelry");

        final MetricSingleSeriesDetectionCondition seriesCondition1
            = new MetricSingleSeriesDetectionCondition(seriesKey1)
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final DimensionKey seriesKey2 = new DimensionKey()
            .put("city", "Osaka")
            .put("category", "Cell Phones");

        final MetricSingleSeriesDetectionCondition seriesCondition2
            = new MetricSingleSeriesDetectionCondition(seriesKey2)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)));

        final DimensionKey seriesGroupKey1 = new DimensionKey()
            .put("city", "Sao Paulo");

        final MetricSeriesGroupDetectionCondition seriesGroupCondition1
            = new MetricSeriesGroupDetectionCondition(seriesGroupKey1)
            .setConditionOperator(DetectionConditionOperator.OR)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)))
            .setHardThresholdCondition(new HardThresholdCondition(
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(5, 5))
                .setLowerBound(0.0)
                .setUpperBound(100.0))
            .setChangeThresholdCondition(new ChangeThresholdCondition(
                50,
                30,
                true,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(2, 2)));

        final AnomalyDetectionConfiguration detectionConfiguration
            = new AnomalyDetectionConfiguration(detectionConfigName)
            .setDescription("test metric anomaly detection configuration")
            .setWholeSeriesDetectionCondition(wholeSeriesCondition)
            .addSingleSeriesDetectionCondition(seriesCondition1)
            .addSingleSeriesDetectionCondition(seriesCondition2)
            .addSeriesGroupDetectionCondition(seriesGroupCondition1);

        // The updates
        final DimensionKey seriesKeyToRemoveOnUpdate = seriesKey2;

        final DimensionKey seriesGroupKeyToAddOnUpdate = new DimensionKey()
            .put("city", "Seoul");

        final MetricSeriesGroupDetectionCondition seriesGroupConditionToAddOnUpdate
            = new MetricSeriesGroupDetectionCondition(seriesGroupKeyToAddOnUpdate)
            .setSmartDetectionCondition(new SmartDetectionCondition(
                63,
                AnomalyDetectorDirection.BOTH,
                new SuppressCondition(1, 100)));
    }

    protected DataFeed createDataFeed(HttpClient httpClient,
                                      MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        DataFeed dataFeed = new DataFeed().setSource(SqlServerDataFeedSource.fromBasicCredential(
            SQL_SERVER_CONNECTION_STRING,
            TEMPLATE_QUERY));
        dataFeed.setSchema(new DataFeedSchema(Arrays.asList(
            new DataFeedMetric("cost").setDisplayName("cost"),
            new DataFeedMetric("revenue").setDisplayName("revenue")))
            .setDimensions(Arrays.asList(
                new DataFeedDimension("city").setDisplayName("city"),
                new DataFeedDimension("category").setDisplayName("category"))))
            .setName("java_data_feed_for_detection" + UUID.randomUUID())
            .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
            .setIngestionSettings(new DataFeedIngestionSettings(INGESTION_START_TIME));

        return client.createDataFeed(
                dataFeed);
    }

    protected void assertUpdateDetectionConfigurationOutput(
        AnomalyDetectionConfiguration config, String expectedMetricId) {

        Assertions.assertNotNull(config.getId());
        Assertions.assertEquals(expectedMetricId, config.getMetricId());
        Assertions.assertEquals(config.getDescription(),
            UpdateDetectionConfigurationInput
                .INSTANCE.detectionConfiguration.getDescription());
        Assertions.assertNotNull(config.getName());
        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertNotNull(config.getSeriesGroupDetectionConditions());

        final MetricWholeSeriesDetectionCondition createdWholeSeriesCondition
            = config.getWholeSeriesDetectionCondition();
        final MetricWholeSeriesDetectionCondition expectedWholeSeriesCondition
            = UpdateDetectionConfigurationInput.INSTANCE.wholeSeriesCondition;

        Assertions.assertNotNull(createdWholeSeriesCondition);
        Assertions.assertEquals(expectedWholeSeriesCondition.getConditionOperator(),
            createdWholeSeriesCondition.getConditionOperator());
        // WholeSeries::SmartDetection
        Assertions.assertNotNull(createdWholeSeriesCondition.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getSmartDetectionCondition().getSensitivity(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::ChangeThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedWholeSeriesCondition.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // WholeSeries::HardThreshold
        Assertions.assertNotNull(createdWholeSeriesCondition.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdWholeSeriesCondition.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getLowerBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedWholeSeriesCondition.getHardThresholdCondition().getUpperBound(),
            createdWholeSeriesCondition.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdWholeSeriesCondition.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        Assertions.assertNotNull(config.getSeriesDetectionConditions());
        Assertions.assertEquals(1, config.getSeriesDetectionConditions().size());

        final DimensionKey expectedSeriesKey1
            = UpdateDetectionConfigurationInput.INSTANCE.seriesKey1;

        Optional<MetricSingleSeriesDetectionCondition> createdSeriesCondition1Opt
            = config.getSeriesDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesKey().equals(expectedSeriesKey1))
            .findFirst();

        Assertions.assertTrue(createdSeriesCondition1Opt.isPresent());
        final MetricSingleSeriesDetectionCondition createdSeriesCondition1 = createdSeriesCondition1Opt.get();
        Assertions.assertNotNull(createdSeriesCondition1);

        final MetricSingleSeriesDetectionCondition expectedSeriesCondition1
            = UpdateDetectionConfigurationInput.INSTANCE.seriesCondition1;
        Assertions.assertEquals(createdSeriesCondition1.getConditionOperator(),
            expectedSeriesCondition1.getConditionOperator());
        // Series1::SmartDetection
        Assertions.assertNotNull(createdSeriesCondition1.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getSensitivity(),
            createdSeriesCondition1.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesCondition1.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        // Series1::ChangeThreshold
        Assertions.assertNotNull(createdSeriesCondition1.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesCondition1.getChangeThresholdCondition().getChangePercentage(),
            createdSeriesCondition1.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedSeriesCondition1.getChangeThresholdCondition().getShiftPoint(),
            createdSeriesCondition1.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedSeriesCondition1.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // Series1::HardThreshold
        Assertions.assertNotNull(createdSeriesCondition1.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesCondition1.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesCondition1.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesCondition1.getHardThresholdCondition().getLowerBound(),
            createdSeriesCondition1.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedSeriesCondition1.getHardThresholdCondition().getUpperBound(),
            createdSeriesCondition1.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        final DimensionKey unExpectedSeriesKey2
            = UpdateDetectionConfigurationInput.INSTANCE.seriesKeyToRemoveOnUpdate;

        Optional<MetricSingleSeriesDetectionCondition> unexpectedSeriesCondition1Opt
            = config.getSeriesDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesKey().equals(unExpectedSeriesKey2))
            .findFirst();

        Assertions.assertFalse(unexpectedSeriesCondition1Opt.isPresent());

        final DimensionKey expectedSeriesGroupKey1
            = UpdateDetectionConfigurationInput.INSTANCE.seriesGroupKey1;

        Optional<MetricSeriesGroupDetectionCondition> createdSeriesGroupCondition1Opt = config
            .getSeriesGroupDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesGroupKey().equals(expectedSeriesGroupKey1))
            .findFirst();

        Assertions.assertTrue(createdSeriesGroupCondition1Opt.isPresent());

        final MetricSeriesGroupDetectionCondition createdSeriesGroupCondition1 = createdSeriesGroupCondition1Opt.get();
        Assertions.assertNotNull(createdSeriesGroupCondition1);

        final MetricSeriesGroupDetectionCondition expectedSeriesGroupCondition1
            = UpdateDetectionConfigurationInput.INSTANCE.seriesGroupCondition1;
        Assertions.assertEquals(createdSeriesGroupCondition1.getConditionOperator(),
            expectedSeriesGroupCondition1.getConditionOperator());
        // SeriesGroup1::SmartDetection
        Assertions.assertNotNull(createdSeriesGroupCondition1.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getSensitivity(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getAnomalyDetectorDirection());
        Assertions.assertNotNull(createdSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition1.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
        // SeriesGroup1::ChangeThreshold
        Assertions.assertNotNull(createdSeriesGroupCondition1.getChangeThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getChangeThresholdCondition().getChangePercentage(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getChangePercentage());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getChangeThresholdCondition().getShiftPoint(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getShiftPoint());
        Assertions.assertTrue(expectedSeriesGroupCondition1.getChangeThresholdCondition().isWithinRange());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition1.getChangeThresholdCondition().getSuppressCondition().getMinRatio());
        // SeriesGroup1::HardThreshold
        Assertions.assertNotNull(createdSeriesGroupCondition1.getHardThresholdCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getHardThresholdCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getAnomalyDetectorDirection());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getHardThresholdCondition().getLowerBound(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getLowerBound());
        Assertions.assertEquals(expectedSeriesGroupCondition1.getHardThresholdCondition().getUpperBound(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getUpperBound());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinNumber());
        Assertions.assertEquals(
            expectedSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition1.getHardThresholdCondition().getSuppressCondition().getMinRatio());

        final DimensionKey expectedSeriesGroupKey2
            = UpdateDetectionConfigurationInput.INSTANCE.seriesGroupKeyToAddOnUpdate;

        Optional<MetricSeriesGroupDetectionCondition> createdSeriesGroupCondition2Opt = config
            .getSeriesGroupDetectionConditions()
            .stream()
            .filter(c -> c.getSeriesGroupKey().equals(expectedSeriesGroupKey2))
            .findFirst();

        Assertions.assertTrue(createdSeriesGroupCondition2Opt.isPresent());

        final MetricSeriesGroupDetectionCondition createdSeriesGroupCondition2 = createdSeriesGroupCondition2Opt.get();
        Assertions.assertNotNull(createdSeriesGroupCondition2);

        final MetricSeriesGroupDetectionCondition expectedSeriesGroupCondition2
            = UpdateDetectionConfigurationInput.INSTANCE.seriesGroupConditionToAddOnUpdate;
        // SeriesGroup1::SmartDetection
        Assertions.assertNotNull(createdSeriesGroupCondition2.getSmartDetectionCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getSensitivity(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getSensitivity());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getAnomalyDetectorDirection(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getAnomalyDetectorDirection());

        Assertions.assertNotNull(createdSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinRatio(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinRatio());
        Assertions.assertEquals(
            expectedSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinNumber(),
            createdSeriesGroupCondition2.getSmartDetectionCondition().getSuppressCondition().getMinNumber());
    }

    protected void deleteDateFeed(DataFeed dataFeed,
                                  HttpClient httpClient,
                                  MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        client.deleteDataFeed(dataFeed.getId());
    }
}
