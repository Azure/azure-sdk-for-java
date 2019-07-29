// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public interface FeedResponseValidator<T extends Resource> {
    
    void validate(FeedResponse<T> feedList);

    public class Builder<T extends Resource> {
        private List<FeedResponseValidator<? extends Resource>> validators = new ArrayList<>();

        public FeedResponseValidator<T> build() {
            return new FeedResponseValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    for (FeedResponseValidator validator : validators) {
                        validator.validate(feedPage);
                    }
                }
            };
        }

        public Builder<T> pageSizeIsLessThanOrEqualTo(final int maxPageSize) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.results().size()).isLessThanOrEqualTo(maxPageSize);
                }
            });
            return this;
        }

        public Builder<T> pageSizeOf(final int expectedCount) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.results()).hasSize(expectedCount);
                }
            });
            return this;
        }
        
        public Builder<T> positiveRequestCharge() {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.requestCharge()).isPositive();
                }
            });
            return this;
        }

        public Builder<T> requestChargeGreaterThanOrEqualTo(double minRequestCharge) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.requestCharge()).isGreaterThanOrEqualTo(minRequestCharge);
                }
            });
            return this;
        }
        
        public Builder<T> requestChargeLessThanOrEqualTo(double maxRequestCharge) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.requestCharge()).isLessThanOrEqualTo(maxRequestCharge);
                }
            });
            return this;
        }
        
        public Builder<T> hasHeader(String headerKey) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.responseHeaders()).containsKey(headerKey);
                }
            });
            return this;
        }
        
        public Builder<T> hasRequestChargeHeader() {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.responseHeaders()).containsKey(HttpConstants.HttpHeaders.REQUEST_CHARGE);
                }
            });
            return this;
        }
        
        public Builder<T> idsExactlyAre(final List<String> expectedIds) {
            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage
                            .results().stream()
                            .map(r -> r.resourceId())
                            .collect(Collectors.toList()))
                            .containsExactlyElementsOf(expectedIds);
                }
            });
            return this;
        }
    }
}