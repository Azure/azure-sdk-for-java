
# Anomaly Detector champion scenarios

## Scenario 0: Creating a `AnomalyDetectorAsyncClient `

```java
MetricsMonitorCredential credential
    = new MetricsMonitorCredential("<subscription_key>", "<api_key>");

AnomalyDetectorAsyncClient client = new AnomalyDetectorClientBuilder()
    .endpoint("<service_endpoint>")
    .credential(credential)
    .buildAsyncClient();
```

## Scenario 1: Get the incident(s) for an alert

We got an alert! Let's look at the incidents.

The payload sent to hooks contains: alerting configuration id, metric id, timestamp, etc.

```java
PagedFlux<Incident> incidentsAlerted = client.listIncidentsForAlert("<alertingConfigId>",
    "<alertId>",
    new ListIncidentsAlertedOptions()
        .setSkipCount(1)
        .setTopCount(100));

final DimensionKey redmondSeriesGroupId = new DimensionKey()
    .put("city", "redmond");

Incident[] incidentInRedmond = new Incident[1];

incidentsAlerted
    .doOnNext(incident -> {
        logger.info("Id:" + incident.getId());
        logger.info("MetricId" + incident.getMetricId());
        logger.info("DetectionConfigId:" + incident.getDetectionConfigurationId());
        logger.info("StartTime:" + incident.getStartTime());
        logger.info("EndTime:" + incident.getEndTime());
        logger.info("Status:"+ incident.getIncidentStatus());
        logger.info("Severity:" + incident.getSeverity());
        DimensionKey seriesOrGroupId = incident.getDimensionKey();
        logger.info("SeriesOrGroupId:" + seriesOrGroupId);

        if (seriesOrGroupId.equals(redmondSeriesGroupId)) {
            incidentInRedmond[0] = incident;
        }

    }).blockLast();
```

## Scenario 2: Root Cause analysis for an incident

What are the root causes of an incident?

```java
PagedFlux<IncidentRootCause> IncidentRootCauses
    = client.listIncidentRootCauses(incidentInRedmond[0]);

IncidentRootCauses
    .doOnNext(rootCause -> {
        logger.info("Description:" + rootCause.getDescription());
        logger.info("Score:" + rootCause.getScore());
        List<String> paths =  rootCause.getPaths();
        int i = 0;
        for (String path : paths) {
            logger.info(String.format("Path#%d:%s", i, path));
            i++;
        }
        DimensionKey dimensionKey = rootCause.getDimensionKey();
        logger.info("DimensionKey:" + dimensionKey); // { category: "Toy", city: "Redmond" }
    });
```

## Scenario 3: Providing feedback to a metric

Anomalies are looked at, and some are actually expected. So we mark them as not Anomaly.

```java
MetricFeedback feedback = new MetricAnomalyFeedback(
    OffsetDateTime.parse("2020/08/05"),
    OffsetDateTime.parse("2020/08/07"),
    AnomalyValue.NOT_ANOMALY);

client.createMetricFeedback("<metricId>", feedback);
```

## Scenario 4: Listing incidents

Other than listing incidents from an alert, one can also query the incidents from a detection configuration


```java
PagedFlux<Incident> incidentsDetected =  client.listIncidentsForDetectionConfiguration(
    "<detection-config-id>",
    new ListIncidentsDetectedOptions(
            OffsetDateTime.parse("2020/08/05"),
            OffsetDateTime.parse("2020/08/07")
    ).setDimensionToFilter(
        new DimensionKey()
            .put("city", "redmond")
    ));

incidentsDetected
    .doOnNext(incident -> {
        logger.info("Id:" + incident.getId());
        logger.info("MetricId" + incident.getMetricId());
        logger.info("DetectionConfigId:" + incident.getDetectionConfigurationId());
        logger.info("StartTime:" + incident.getStartTime());
        logger.info("EndTime:" + incident.getEndTime());
        logger.info("Status:"+ incident.getIncidentStatus());
        logger.info("Severity:" + incident.getSeverity());
        DimensionKey seriesOrGroupId = incident.getDimensionKey();
        logger.info("SeriesOrGroupId:" + seriesOrGroupId);
    }).blockLast();
```

## Scenario 5: Create data feeds

```java
List<Metric> metrics = new ArrayList<>();
metrics.add(new Metric()
    .setMetricName("cost")
    .setMetricDisplayName("Cost")
    .setMetricDescription("Cost description"));

metrics.add(new Metric()
    .setMetricName("revenue")
    .setMetricDisplayName("Revenue")
    .setMetricDescription("Revenue description"));
//
List<Dimension> dimensions = new ArrayList<>();
dimensions.add(new Dimension()
    .setDimensionName("category")
    .setDimensionDisplayName("Category"));

dimensions.add(new Dimension()
    .setDimensionName("city")
    .setDimensionDisplayName("City"));
//
DataFeedSchema dataFeedSchema = new DataFeedSchema(metrics)
    .setDimensionColumns(dimensions);
//
DataFeedIngestionSettings dataFeedIngestionSettings
    = new DataFeedIngestionSettings(OffsetDateTime.parse("2019-10-01T00:00:00Z"));
//
DataFeed dataFeed = client.createDataFeed("sql data source 1",
    dataFeedSource,
    new DataFeedGranularity()
        .setGranularity(DataFeedGranularityType.DAILY),
    dataFeedSchema,
    dataFeedIngestionSettings,
    new DataFeedOptions()).block();

logger.info("DataFeedId" + dataFeed.getId());

```

## Scenario 6: Check ingestion status

```java
PagedFlux<DataFeedIngestionStatus> ingestionStatusFlux = client.listDataFeedIngestionStatus(dataFeed.getId(),
    new ListDataFeedIngestionOptions(
        OffsetDateTime.parse("2019-10-01T00:00:00Z"),
        OffsetDateTime.parse("2019-10-04T00:00:00Z")));

ingestionStatusFlux.doOnNext(status -> {
    logger.info("TimeStamp:" + status.getTimestamp());
    logger.info("Status:" + status.getStatus());
    logger.info("Message:" + status.getMessage());
}).blockLast();
```

## Scenario 7: Create and update detection configuration

```java
MetricAnomalyDetectionConditions detectionConditions
    = new MetricAnomalyDetectionConditions(DetectionConditionsOperator.AND)
    .setHardThresholdCondition(new HardThresholdCondition()
        .setAnomalyDetectorDirection(AnomalyDetectorDirection.UP)
        .setLowerBound(200.0)
        .setSuppressCondition(new SuppressCondition()
            .setMinNumber(100)
            .setMinRatio(50)));

MetricAnomalyDetectionConfiguration detectionConfigurationToCreate
    = new MetricAnomalyDetectionConfiguration("<config_name>")
    .setWholeSeriesDetectionConditions(detectionConditions)
    .setDescription("description")
    .setName("name");

MetricAnomalyDetectionConfiguration detectionConfiguration = client
    .createMetricAnomalyDetectionConfiguration("<metric_id>", detectionConfigurationToCreate)
    .block();

detectionConfiguration.setDescription("new description");
detectionConfiguration.setName("New name");

client.updateMetricAnomalyDetectionConfiguration(detectionConfiguration);
```

## Scenario 8: Create Webhook hook

```java
WebHook webHookToCreate = new WebHook("htttps://alert.contoso.com")
    .setDescription("hook for category series group");

WebHook webHook = (WebHook) client.createHook("hook_name", webHookToCreate).block();

```

## Scenario 8: Create alerting configuration

```java
DimensionKey dimensionKey = new DimensionKey()
    .put("category", "men/shoes");

MetricAnomalyAlertConfiguration metricAnomalyAlertConfiguration
    = new MetricAnomalyAlertConfiguration(
    detectionConfiguration.getId(),
    MetricAnomalyAlertScope.forSeriesGroup(dimensionKey));

AnomalyAlertConfiguration anomalyAlertConfigurationToCreate
    = new AnomalyAlertConfiguration("<alert_config_name>",
    MetricAnomalyAlertConfigurationsOperator.OR)
    .addMetricAlertConfiguration(metricAnomalyAlertConfiguration)
    .addIdOfHookToAlert(webHook.getId());

client.createAnomalyAlertConfiguration(anomalyAlertConfigurationToCreate);
```
