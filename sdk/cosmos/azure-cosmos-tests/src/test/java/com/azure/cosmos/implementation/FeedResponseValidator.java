// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public interface FeedResponseValidator<T> {

    void validate(FeedResponse<T> feedList);

    public class Builder<T> {
        private List<FeedResponseValidator<T>> validators = new ArrayList<>();

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
                    assertThat(feedPage.getResults().size()).isLessThanOrEqualTo(maxPageSize);
                }
            });
            return this;
        }

        public Builder<T> pageSizeOf(final int expectedCount) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.getResults()).hasSize(expectedCount);
                }
            });
            return this;
        }

        public Builder<T> positiveRequestCharge() {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.getRequestCharge()).isPositive();
                }
            });
            return this;
        }

        public Builder<T> requestChargeGreaterThanOrEqualTo(double minRequestCharge) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.getRequestCharge()).isGreaterThanOrEqualTo(minRequestCharge);
                }
            });
            return this;
        }

        public Builder<T> requestChargeLessThanOrEqualTo(double maxRequestCharge) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.getRequestCharge()).isLessThanOrEqualTo(maxRequestCharge);
                }
            });
            return this;
        }

        public Builder<T> hasHeader(String headerKey) {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.getResponseHeaders()).containsKey(headerKey);
                }
            });
            return this;
        }

        public Builder<T> hasRequestChargeHeader() {

            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage.getResponseHeaders()).containsKey(HttpConstants.HttpHeaders.REQUEST_CHARGE);
                }
            });
            return this;
        }

        public Builder<T> idsExactlyAre(final List<String> expectedIds) {
            validators.add(new FeedResponseValidator<T>() {
                @Override
                public void validate(FeedResponse<T> feedPage) {
                    assertThat(feedPage
                            .getResults().stream()
                            .map(r -> getResource(r).getResourceId())
                            .collect(Collectors.toList()))
                            .containsExactlyElementsOf(expectedIds);
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

            return null;
        }
    }
}
