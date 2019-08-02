// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CompositePath;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public interface FeedResponseListValidator<T extends Resource> {

    void validate(List<FeedResponse<T>> feedList);

    class Builder<T extends Resource> {
        private List<FeedResponseListValidator<? extends Resource>> validators = new ArrayList<>();

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
                    int resultCount = feedList.stream().mapToInt(f -> f.results().size()).sum();
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
                            .flatMap(f -> f.results().stream())
                            .map(r -> r.resourceId())
                            .collect(Collectors.toList());
                    assertThat(actualIds)
                    .describedAs("Resource IDs of results")
                    .containsExactlyElementsOf(expectedRids);
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
                            .flatMap(f -> f.results().stream())
                            .map(r -> r.id())
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
                            .flatMap(f -> f.results().stream())
                            .collect(Collectors.toList());

                    for(T r: resources) {
                        ResourceValidator<T> validator = resourceIDToValidator.get(r.resourceId());
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
                            .flatMap(f -> f.results().stream())
                            .map(Resource::resourceId)
                            .collect(Collectors.toList());
                    assertThat(actualIds)
                    .describedAs("Resource IDs of results")
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
                    assertThat(feedList.stream().mapToDouble(p -> p.requestCharge()).sum())
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
            validators.add(new FeedResponseListValidator<CosmosItemProperties>() {
                @Override
                public void validate(List<FeedResponse<CosmosItemProperties>> feedList) {
                    List<CosmosItemProperties> list = feedList.get(0).results();
                    CosmosItemProperties result = list.size() > 0 ? list.get(0) : null;

                    if (result != null) {
                        if (value instanceof Double) {

                            Double d = result.getDouble("_aggregate");
                            assertThat(d).isEqualTo(value);                
                        } else if (value instanceof Integer) {

                            Integer d = result.getInt("_aggregate");
                            assertThat(d).isEqualTo(value);                
                        } else if (value instanceof String) {

                            String d = result.getString("_aggregate");
                            assertThat(d).isEqualTo(value);                
                        } else if (value instanceof Document){

                            assertThat(result.toString()).isEqualTo(value.toString());
                        } else {

                            assertThat(result.get("_aggregate")).isNull();
                            assertThat(value).isNull();
                        }
                    } else {

                        assertThat(value).isNull();
                    }

                }
            });
            return this;
        }

        public Builder<T> withOrderedResults(List<CosmosItemProperties> expectedOrderedList,
                List<CompositePath> compositeIndex) {
            validators.add(new FeedResponseListValidator<CosmosItemProperties>() {
                @Override
                public void validate(List<FeedResponse<CosmosItemProperties>> feedList) {

                    List<CosmosItemProperties> resultOrderedList = feedList.stream()
                            .flatMap(f -> f.results().stream())
                            .collect(Collectors.toList());
                    assertThat(expectedOrderedList.size()).isEqualTo(resultOrderedList.size());

                    ArrayList<String> paths = new ArrayList<String>();
                    Iterator<CompositePath> compositeIndexIterator = compositeIndex.iterator();
                    while (compositeIndexIterator.hasNext()) {
                        paths.add(compositeIndexIterator.next().path().replace("/", ""));
                    }
                    for (int i = 0; i < resultOrderedList.size(); i ++) {
                        ArrayNode resultValues = (ArrayNode) resultOrderedList.get(i).get("$1");
                        assertThat(resultValues.size()).isEqualTo(paths.size());
                        for (int j = 0; j < paths.size(); j++) {
                            if (paths.get(j).contains("number")) {
                                assertThat(expectedOrderedList.get(i).getInt(paths.get(j))).isEqualTo(resultValues.get(j).intValue());
                            } else if (paths.get(j).toLowerCase().contains("string")) {
                                assertThat(expectedOrderedList.get(i).getString(paths.get(j))).isEqualTo(resultValues.get(j).asText());
                            } else if (paths.get(j).contains("bool")) {
                                assertThat(expectedOrderedList.get(i).getBoolean(paths.get(j))).isEqualTo(resultValues.get(j).asBoolean());
                            } else {
                                assertThat(resultValues.get(j).isNull()).isTrue();
                                assertThat(expectedOrderedList.get(i).get("nullField")).isNull();
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
                        assertThat(feedList.get(i).results().size()).isEqualTo(pageLengths[i]);
                }
            });
            return this;
        }

        public Builder<T> hasValidQueryMetrics(boolean shouldHaveMetrics) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    for(FeedResponse feedPage: feedList) {
                        if (shouldHaveMetrics) {
                            QueryMetrics queryMetrics = BridgeInternal.createQueryMetricsFromCollection(BridgeInternal.queryMetricsFromFeedResponse(feedPage).values());
                            assertThat(queryMetrics.getIndexHitDocumentCount()).isGreaterThanOrEqualTo(0);
                            assertThat(queryMetrics.getRetrievedDocumentSize()).isGreaterThan(0);
                            assertThat(queryMetrics.getTotalQueryExecutionTime().compareTo(Duration.ZERO)).isGreaterThan(0);
                            assertThat(queryMetrics.getOutputDocumentCount()).isGreaterThan(0);
                            assertThat(queryMetrics.getRetrievedDocumentCount()).isGreaterThan(0);
                            assertThat(queryMetrics.getDocumentLoadTime().compareTo(Duration.ZERO)).isGreaterThan(0);
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
    }
}
