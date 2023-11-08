// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.ModelBridgeInternal;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface CosmosItemResponseValidator {
    @SuppressWarnings("rawtypes")
    void validate(CosmosItemResponse itemResponse);

    class Builder<T> {
        private List<CosmosItemResponseValidator> validators = new ArrayList<>();

        public Builder<T> withId(final String resourceId) {
            validators.add(new CosmosItemResponseValidator() {

                @Override
                @SuppressWarnings("rawtypes")
                public void validate(CosmosItemResponse itemResponse) {
                    assertThat(itemResponse.getItem()).isNotNull();
                    // This could be validated for potential improvement by remove fromObject
                    assertThat(InternalObjectNode.fromObject(itemResponse.getItem())
                                                 .getId()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }

        public Builder<T> withProperty(String propertyName, String value) {
            validators.add(new CosmosItemResponseValidator() {

                @Override
                @SuppressWarnings("rawtypes")
                public void validate(CosmosItemResponse itemResponse) {
                    assertThat(itemResponse.getItem()).isNotNull();
                    assertThat(ModelBridgeInternal
                        .getObjectFromJsonSerializable(InternalObjectNode.fromObject(itemResponse.getItem()), propertyName))
                        .as("check property")
                        .isEqualTo(value);
                }
            });
            return this;
        }

        public CosmosItemResponseValidator build() {
            return new CosmosItemResponseValidator() {
                @Override
                @SuppressWarnings("rawtypes")
                public void validate(CosmosItemResponse itemResponse) {
                    for (CosmosItemResponseValidator validator : validators) {
                        validator.validate(itemResponse);
                    }
                }
            };
        }

        public Builder<T> nullResource() {
            validators.add(new CosmosItemResponseValidator() {

                @Override
                @SuppressWarnings("rawtypes")
                public void validate(CosmosItemResponse itemResponse) {
                    assertThat(itemResponse.getItem()).isNull();
                }
            });
            return this;
        }
    }
}
