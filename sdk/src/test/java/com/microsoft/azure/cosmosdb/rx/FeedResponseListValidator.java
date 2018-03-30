/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.Resource;

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
                    int resultCount = feedList.stream().mapToInt(f -> f.getResults().size()).sum();
                    assertThat(resultCount)
                    .describedAs("total number of results").isEqualTo(expectedCount);
                }
            });
            return this;
        }

        public Builder<T> containsExactly(List<String> expectedIds) {
            validators.add(new FeedResponseListValidator<T>() {
                @Override
                public void validate(List<FeedResponse<T>> feedList) {
                    List<String> actualIds = feedList
                            .stream()
                            .flatMap(f -> f.getResults().stream())
                            .map(r -> r.getResourceId())
                            .collect(Collectors.toList());
                    assertThat(actualIds)
                    .describedAs("Resource IDs of results")
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
                        ResourceValidator<T> validator = resourceIDToValidator.get(r.getResourceId());
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
                            .map(r -> r.getResourceId())
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
            validators.add(new FeedResponseListValidator<Document>() {
                @Override
                public void validate(List<FeedResponse<Document>> feedList) {
                    List<Document> list = feedList.get(0).getResults();
                    Document result = list.size() > 0 ? list.get(0) : null;

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
    }
}
