// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public interface FeedResponseListValidator<T> {

    void validate(List<FeedResponse<T>> feedList);

    class Builder<T> {
        private List<FeedResponseListValidator<?>> validators = new ArrayList<>();

        public FeedResponseListValidator<T> build() {
            return new FeedResponseListValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    for (FeedResponseListValidator validator : validators) {
                        validator.validate(feedList);
                    }
                }
            };
        }

        public Builder<T> totalSize(final int expectedCount) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    int resultCount = feedList.stream().mapToInt(f -> f.getResults().size()).sum();
                    assertThat(resultCount)
                    .describedAs("total number of results").isEqualTo(expectedCount);
                }
            });
            return this;
        }

        public Builder<T> containsExactly(List<String> expectedRids) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<String> actualIds = feedList
                            .stream()
                            .flatMap(f -> f.getResults().stream())
                            .map(r -> getResource(r).getResourceId())
                            .collect(Collectors.toList());
                    assertThat(actualIds)
                    .describedAs("Resource IDs of results")
                    .containsExactlyElementsOf(expectedRids);
                }
            });
            return this;
        }

        public Builder<T> containsExactlyValues(List<T> expectedValues) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<T> actualValues = feedList.stream()
                                            .flatMap(f -> f.getResults().stream())
                                            .collect(Collectors.toList());
                    assertThat(actualValues)
                        .describedAs("Result values")
                        .containsExactlyElementsOf(expectedValues);
                }
            });
            return this;
        }

        public Builder<T> containsExactlyIds(List<String> expectedIds) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<String> actualIds = feedList
                            .stream()
                            .flatMap(f -> f.getResults().stream())
                            .map(r -> getResource(r).getId())
                            .collect(Collectors.toList());
                    assertThat(actualIds)
                    .describedAs("IDs of results")
                    .containsExactlyElementsOf(expectedIds);
                }
            });
            return this;
        }

        public Builder<T> validateAllResources(Map<String, ResourceValidator<T>> resourceIDToValidator) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<T> resources = feedList
                            .stream()
                            .flatMap(f -> f.getResults().stream())
                            .collect(Collectors.toList());

                    for(T r: resources) {
                        ResourceValidator<T> validator = resourceIDToValidator.get(getResource(r).getResourceId());
                        assertThat(validator).isNotNull();
                        validator.validate(r);
                    }
                }
            });
            return this;
        }

        public Builder<T> exactlyContainsInAnyOrder(List<String> expectedIds) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<String> actualIds = feedList
                            .stream()
                            .flatMap(f -> f.getResults().stream())
                            .map(r -> getResource(r).getResourceId())
                            .collect(Collectors.toList());
                    assertThat(actualIds)
                    .describedAs("Resource IDs of results")
                    .containsOnlyElementsOf(expectedIds);
                }
            });
            return this;
        }

        public Builder<T> exactlyContainsIdsInAnyOrder(List<String> expectedIds) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<String> actualIds = feedList
                        .stream()
                        .flatMap(f -> f.getResults().stream())
                        .map(r -> getResource(r).getId())
                        .collect(Collectors.toList());
                    assertThat(actualIds)
                        .describedAs("IDs of results")
                        .containsOnlyElementsOf(expectedIds);
                }
            });
            return this;
        }

        public Builder<T> numberOfPages(int expectedNumberOfPages) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    assertThat(feedList)
                    .describedAs("number of pages")
                    .hasSize(expectedNumberOfPages);
                }
            });
            return this;
        }

        public Builder<T> numberOfPagesIsGreaterThanOrEqualTo(int leastNumber) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    assertThat(feedList.size())
                            .describedAs("number of pages")
                            .isGreaterThanOrEqualTo(leastNumber);
                }
            });
            return this;
        }

        public Builder<T> totalRequestChargeIsAtLeast(double minimumCharge) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    assertThat(feedList.stream().mapToDouble(p -> p.getRequestCharge()).sum())
                    .describedAs("total request charge")
                    .isGreaterThanOrEqualTo(minimumCharge);
                }
            });
            return this;
        }

        public Builder<T> pageSatisfy(int pageNumber, FeedResponseValidator<T> pageValidator) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    assertThat(feedList.size()).isGreaterThan(pageNumber);
                    pageValidator.validate(feedList.get(pageNumber));
                }
            });
            return this;
        }

        public Builder<T> allPagesSatisfy(FeedResponseValidator<T> pageValidator) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {

                    for(FeedResponse<T> fp: feedList) {
                        pageValidator.validate(fp);
                    }
                }
            });
            return this;
        }

        public Builder<T> withAggregateValue(Object value) {
            validators.add(new FeedResponseListValidator<JsonNode>() {
                @Override
                public void validate(List<FeedResponse<JsonNode>> feedList) {
                    List<JsonNode> list = feedList.get(0).getResults();
                    JsonNode result = list.size() > 0 ? list.get(0) : null;

                    if (result != null) {
                        if (value instanceof Double) {

                            Double d = result.asDouble();
                            assertThat(d).isEqualTo(value);
                        } else if (value instanceof Integer) {

                            Integer d = result.asInt();
                            assertThat(d).isEqualTo(value);
                        } else if (value instanceof String) {

                            String d = result.asText();
                            assertThat(d).isEqualTo(value);
                        } else if (value instanceof Document) {

                            assertThat(result.toString()).isEqualTo(value.toString());
                        } else {

                            assertThat(result.isNull()).isTrue();
                            assertThat(value).isNull();
                        }
                    } else {

                        assertThat(value).isNull();
                    }

                }
            });
            return this;
        }

        public Builder<T> withOrderedResults(List<InternalObjectNode> expectedOrderedList,
                List<CompositePath> compositeIndex) {
            validators.add(new FeedResponseListValidator<InternalObjectNode>() {
                @Override
                public void validate(List<FeedResponse<InternalObjectNode>> feedList) {

                    List<InternalObjectNode> resultOrderedList = feedList.stream()
                                                                         .flatMap(f -> f.getResults().stream())
                                                                         .collect(Collectors.toList());
                    assertThat(expectedOrderedList.size()).isEqualTo(resultOrderedList.size());

                    ArrayList<String> paths = new ArrayList<String>();
                    Iterator<CompositePath> compositeIndexIterator = compositeIndex.iterator();
                    while (compositeIndexIterator.hasNext()) {
                        paths.add(compositeIndexIterator.next().getPath().replace("/", ""));
                    }
                    for (int i = 0; i < resultOrderedList.size(); i ++) {
                        ArrayNode resultValues = (ArrayNode) ModelBridgeInternal.getObjectFromJsonSerializable(resultOrderedList.get(i), "$1");
                        assertThat(resultValues.size()).isEqualTo(paths.size());
                        for (int j = 0; j < paths.size(); j++) {
                            if (paths.get(j).contains("number")) {
                                assertThat(ModelBridgeInternal.getIntFromJsonSerializable(expectedOrderedList.get(i), paths.get(j)))
                                    .isEqualTo(resultValues.get(j).intValue());
                            } else if (paths.get(j).toLowerCase().contains("string")) {
                                assertThat(ModelBridgeInternal.getStringFromJsonSerializable(expectedOrderedList.get(i), paths.get(j)))
                                    .isEqualTo(resultValues.get(j).asText());
                            } else if (paths.get(j).contains("bool")) {
                                assertThat(ModelBridgeInternal.getBooleanFromJsonSerializable(expectedOrderedList.get(i), paths.get(j))).isEqualTo(resultValues.get(j).asBoolean());
                            } else {
                                assertThat(resultValues.get(j).isNull()).isTrue();
                                assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(expectedOrderedList.get(i), "nullField")).isNull();
                            }
                        }
                    }

                }
            });
            return this;
        }

        public Builder<T> pageLengths(int[] pageLengths) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    assertThat(feedList).hasSize(pageLengths.length);
                    for (int i = 0; i < pageLengths.length; i++)
                        assertThat(feedList.get(i).getResults().size()).isEqualTo(pageLengths[i]);
                }
            });
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder<T> hasValidQueryMetrics(Boolean shouldHaveMetrics) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    for(FeedResponse<T> feedPage: feedList) {
                        if (shouldHaveMetrics ==  null || shouldHaveMetrics) {
                            QueryMetrics queryMetrics = BridgeInternal.createQueryMetricsFromCollection(BridgeInternal.queryMetricsFromFeedResponse(feedPage).values());
                            assertThat(queryMetrics.getIndexHitDocumentCount()).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getRetrievedDocumentSize()).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getTotalQueryExecutionTime().compareTo(Duration.ZERO)).isGreaterThan(0);
                            assertThat(queryMetrics.getOutputDocumentCount()).isGreaterThan(0);
                            assertThat(queryMetrics.getRetrievedDocumentCount()).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getDocumentLoadTime().compareTo(Duration.ZERO)).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getDocumentWriteTime().compareTo(Duration.ZERO)).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getVMExecutionTime().compareTo(Duration.ZERO)).isGreaterThan(0);
                            assertThat(queryMetrics.getQueryPreparationTimes().getLogicalPlanBuildTime().compareTo(Duration.ZERO)).isGreaterThan(0);
                            assertThat(queryMetrics.getQueryPreparationTimes().getPhysicalPlanBuildTime().compareTo(Duration.ZERO)).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getQueryPreparationTimes().getQueryCompilationTime().compareTo(Duration.ZERO)).isGreaterThan(0);
                            assertThat(queryMetrics.getRuntimeExecutionTimes().getQueryEngineExecutionTime().compareTo(Duration.ZERO)).isGreaterThanOrEqualTo(0);
                            assertThat(BridgeInternal.getClientSideMetrics(queryMetrics).getRequestCharge()).isGreaterThan(0);
                        } else {
                            assertThat(BridgeInternal.queryMetricsFromFeedResponse(feedPage).isEmpty());
                        }
                    }
                }
            });
            return this;
        }

        private <T> Resource getResource(T response) {
            if (response instanceof Resource
                || response instanceof CosmosConflictProperties
                || response instanceof CosmosContainerProperties
                || response instanceof CosmosDatabaseProperties
                || response instanceof CosmosPermissionProperties
                || response instanceof CosmosStoredProcedureProperties
                || response instanceof CosmosTriggerProperties
                || response instanceof CosmosUserDefinedFunctionProperties
                || response instanceof CosmosUserProperties) {
                return ModelBridgeInternal.getResource(response);
            }
            if (response instanceof ObjectNode) {
                return new Document((ObjectNode)response);
            }
            return null;
        }
    }
}
