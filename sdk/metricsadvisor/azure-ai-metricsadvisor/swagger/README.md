# Azure Cognitive Service - Metric Advisor for Java

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
input-file: ./metricsadvisor_1.20200903_openapi.v2.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.ai.metricsadvisor
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
custom-types-subpackage: models
custom-types: AnomalyDetectorDirection,AnomalyStatus,AnomalyValue,ChangePointValue,DataFeedIngestionProgress,EnrichmentStatus,FeedbackType,AnomalyIncidentStatus,IngestionStatusType,PeriodType,AnomalySeverity,SnoozeScope,AlertQueryTimeMode,DataFeedIngestionStatus,MetricSeriesDefinition,FeedbackQueryTimeMode,AnomalyAlert,DataFeedGranularityType,DataFeedRollupType,DataFeedAutoRollUpMethod,DataFeedStatus,MetricsAdvisorErrorCodeException,MetricsAdvisorErrorCode
```

### Generated types renamed and moved to model

#### ErrorCode -> MetricsAdvisorErrorCode
```yaml
directive:
  - rename-model:
      from: ErrorCode
      to: MetricsAdvisorErrorCode
```

#### TimeMode -> AlertQueryTimeMode
```yaml
directive:
  - rename-model:
      from: TimeMode
      to: AlertQueryTimeMode
```

#### Severity -> AnomalySeverity
```yaml
directive:
  - rename-model:
      from: Severity
      to: AnomalySeverity
```

#### IncidentStatus -> AnomalyIncidentStatus
```yaml
directive:
  - rename-model:
      from: IncidentStatus
      to: AnomalyIncidentStatus
```

#### AlertResult -> AnomalyAlert
```yaml
directive:
  - rename-model:
      from: AlertResult
      to: AnomalyAlert
```

#### IngestionStatus -> DataFeedIngestionStatus
```yaml
directive:
  - rename-model:
      from: IngestionStatus
      to: DataFeedIngestionStatus
```

#### AlertSnoozeCondition -> MetricAnomalyAlertSnoozeCondition
```yaml
directive:
  - rename-model:
      from: AlertSnoozeCondition
      to: MetricAnomalyAlertSnoozeCondition
```

#### MetricDataItem -> MetricSeriesData
```yaml
directive:
  - rename-model:
      from: MetricDataItem
      to: MetricSeriesData
```

#### Granularity -> DataFeedGranularityType
```yaml
directive:
  - rename-model:
      from: Granularity
      to: DataFeedGranularityType
```

#### NeedRollupEnum -> DataFeedRollupType
```yaml
directive:
  - rename-model:
      from: NeedRollupEnum
      to: DataFeedRollupType
```

#### DataFeedDetailRollUpMethod -> DataFeedRollUpMethod
```yaml
directive:
  - rename-model:
      from: DataFeedDetailRollUpMethod
      to: DataFeedRollUpMethod
```

#### EntityStatus -> DataFeedStatus
```yaml
directive:
  - rename-model:
      from: EntityStatus
      to: DataFeedStatus
```

#### Metric properties rename

``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.Metric) {
        const metricId = $.Metric.properties.metricId;
        if (metricId && !metricId["x-ms-client-name"]) {
            metricId["x-ms-client-name"] = "id";
            $.Metric.properties.metricId = metricId;
        }
        const metricName = $.Metric.properties.metricName;
        if (metricName && !metricId["x-ms-client-name"]) {
            metricName["x-ms-client-name"] = "name";
            $.Metric.properties.metricName = metricName;
        }
        const metricDisplayName = $.Metric.properties.metricDisplayName;
        if (metricDisplayName && !metricDisplayName["x-ms-client-name"]) {
            metricDisplayName["x-ms-client-name"] = "displayName";
            $.Metric.properties.metricDisplayName = metricDisplayName;
        }
        const metricDescription = $.Metric.properties.metricDescription;
        if (metricDescription && !metricDescription["x-ms-client-name"]) {
            metricDescription["x-ms-client-name"] = "description";
            $.Metric.properties.metricDescription = metricDescription;
        }
    }
```

#### Dimension properties rename

``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.Dimension) {
        const dimensionName = $.Metric.properties.dimensionName;
        if (dimensionName && !dimensionName["x-ms-client-name"]) {
            dimensionName["x-ms-client-name"] = "name";
            $.Metric.properties.dimensionName = dimensionName;
        }
        const dimensionDisplayName = $.Metric.properties.dimensionDisplayName;
        if (dimensionDisplayName && !dimensionDisplayName["x-ms-client-name"]) {
            dimensionDisplayName["x-ms-client-name"] = "displayName";
            $.Metric.properties.dimensionDisplayName = dimensionDisplayName;
        }
    }
```

#### Alert properties rename

``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.AlertResult) {
        const alertId = $.AlertResult.properties.alertId;
        if (alertId && !alertId["x-ms-client-name"]) {
            alertId["x-ms-client-name"] = "id";
            $.AlertResult.properties.alertId = alertId;
        }
    }
```

### Expose MetricId as String
``` yaml
directive:
- from: swagger-document
  where: $.definitions.Metric
  transform: >
    delete $.properties.metricId["format"];
```

### Rename Metric to DataFeedMetric
```yaml
directive:
  - rename-model:
      from: Metric
      to: DataFeedMetric
```

### Rename Dimension to DataFeedDimension
```yaml
directive:
  - rename-model:
      from: Dimension
      to: DataFeedDimension
```

### Add x-ms-paths section if not exists

``` yaml
directive:
- from: swagger-document
  where: $
  transform: >
    if (!$["x-ms-paths"]) {
      $["x-ms-paths"] = {}
    }
```

### Enable Post based pagination for AlertsByAnomalyAlertingConfiguration.

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/alert/anomaly/configurations/{configurationId}/alerts/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getAlertsByAnomalyAlertingConfigurationNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getAlertsByAnomalyAlertingConfigurationNext"] = {
      "post": {
        "tags": [
          "AnomalyAlerting"
        ],
        "summary": "Query alerts under anomaly alerting configuration",
        "operationId": "getAlertsByAnomalyAlertingConfigurationNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "query alerting result request",
            "required": true,
            "schema": {
              "$ref": "#/definitions/AlertingResultQuery"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/AlertResultList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination for AnomaliesByAnomalyDetectionConfiguration

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/enrichment/anomalyDetection/configurations/{configurationId}/anomalies/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getAnomaliesByAnomalyDetectionConfigurationNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getAnomaliesByAnomalyDetectionConfigurationNext"] = {
      "post": {
        "tags": [
          "AnomalyDetection"
        ],
        "summary": "Query anomalies under anomaly detection configuration",
        "operationId": "getAnomaliesByAnomalyDetectionConfigurationNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "query detection anomaly result request",
            "required": true,
            "schema": {
              "$ref": "#/definitions/DetectionAnomalyResultQuery"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/AnomalyResultList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination DimensionOfAnomaliesByAnomalyDetectionConfiguration

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/enrichment/anomalyDetection/configurations/{configurationId}/anomalies/dimension/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getDimensionOfAnomaliesByAnomalyDetectionConfigurationNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getDimensionOfAnomaliesByAnomalyDetectionConfigurationNext"] = {
      "post": {
        "tags": [
          "AnomalyDetection"
        ],
        "summary": "Query dimension values of anomalies",
        "operationId": "getDimensionOfAnomaliesByAnomalyDetectionConfigurationNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "query dimension values request",
            "required": true,
            "schema": {
              "$ref": "#/definitions/AnomalyDimensionQuery"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/AnomalyDimensionList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination for MetricFeedbacks

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/feedback/metric/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "listMetricFeedbacksNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?listMetricFeedbacksNext"] = {
      "post": {
        "tags": [
          "Feedback"
        ],
        "summary": "List feedback on the given metric",
        "operationId": "listMetricFeedbacksNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "metric feedback filter",
            "required": true,
            "schema": {
              "$ref": "#/definitions/MetricFeedbackFilter"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/MetricFeedbackList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination for DataFeedIngestionStatus

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/dataFeeds/{dataFeedId}/ingestionStatus/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getDataFeedIngestionStatusNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getDataFeedIngestionStatusNext"] = {
      "post": {
        "tags": [
          "IngestionStatus"
        ],
        "summary": "Get data ingestion status by data feed",
        "operationId": "getDataFeedIngestionStatusNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "The query time range",
            "required": true,
            "schema": {
              "$ref": "#/definitions/IngestionStatusQueryOptions"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/IngestionStatusList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination for MetricSeries

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/metrics/{metricId}/series/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getMetricSeriesNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getMetricSeriesNext"] = {
      "post": {
        "tags": [
          "Metric"
        ],
        "summary": "List series (dimension combinations) from metric",
        "operationId": "getMetricSeriesNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "filter to query series",
            "required": true,
            "schema": {
              "$ref": "#/definitions/MetricSeriesQueryOptions"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/MetricSeriesList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination for MetricDimension

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/metrics/{metricId}/dimension/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getMetricDimensionNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getMetricDimensionNext"] = {
      "post": {
        "tags": [
          "Metric"
        ],
        "summary": "List dimension from certain metric",
        "operationId": "getMetricDimensionNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "query dimension option",
            "required": true,
            "schema": {
              "$ref": "#/definitions/MetricDimensionQueryOptions"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/MetricDimensionList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```

### Enable Post based pagination for EnrichmentStatusByMetric

``` yaml
directive:
- from: swagger-document
  where: $["paths"]["/metrics/{metricId}/status/enrichment/anomalyDetection/query"]
  transform: >
    let pageExt = $.post["x-ms-pageable"];
    if (!pageExt) {
      pageExt["operationName"] = "getEnrichmentStatusByMetricNext"
      $.post["x-ms-pageable"] = pageExt
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    $["/{nextLink}?getEnrichmentStatusByMetricNext"] = {
      "post": {
        "tags": [
          "Metric"
        ],
        "summary": "Query anomaly detection status",
        "operationId": "getEnrichmentStatusByMetricNext",
        "consumes": [
          "application/json"
        ],
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "in": "path",
            "name": "nextLink",
            "description": "the next link",
            "required": true,
            "type": "string",
            "x-ms-skip-url-encoding": true
          },
          {
            "in": "body",
            "name": "body",
            "description": "query options",
            "required": true,
            "schema": {
              "$ref": "#/definitions/EnrichmentStatusQueryOption"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "Success",
            "schema": {
              "$ref": "#/definitions/EnrichmentStatusList"
            }
          },
          "default": {
            "description": "Client error or server error (4xx or 5xx)",
            "schema": {
              "$ref": "#/definitions/MetricsAdvisorErrorCode"
            }
          }
        },
        "x-ms-pageable": {
          "nextLinkName": null
        }
      }
    }
```
