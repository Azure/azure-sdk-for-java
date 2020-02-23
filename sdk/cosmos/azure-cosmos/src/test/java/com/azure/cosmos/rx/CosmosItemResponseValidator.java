// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.implementation.CosmosItemProperties;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface CosmosItemResponseValidator {
    void validate(CosmosAsyncItemResponse itemResponse);

    class Builder<T> {
        private List<CosmosItemResponseValidator> validators = new ArrayList<>();

        public Builder withId(final String resourceId) {
            validators.add(new CosmosItemResponseValidator() {

                @Override
                public void validate(CosmosAsyncItemResponse itemResponse) {
                    assertThat(itemResponse.getResource()).isNotNull();
                    // This could be validated for potential improvement by remove fromObject 
                    assertThat(CosmosItemProperties.fromObject(itemResponse.getResource())
                                   .getId()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }

        public Builder withProperty(String propertyName, String value) {
            validators.add(new CosmosItemResponseValidator() {

                @Override
                public void validate(CosmosAsyncItemResponse itemResponse) {
                    assertThat(itemResponse.getResource()).isNotNull();
                    assertThat(CosmosItemProperties.fromObject(itemResponse.getResource())
                                   .get(propertyName)).as("check property").isEqualTo(value);
                }
            });
            return this;
        }

        public CosmosItemResponseValidator build() {
            return new CosmosItemResponseValidator() {
                @Override
                public void validate(CosmosAsyncItemResponse itemResponse) {
                    for (CosmosItemResponseValidator validator : validators) {
                        validator.validate(itemResponse);
                    }
                }
            };
        }

        public Builder nullResource() {
            validators.add(new CosmosItemResponseValidator() {

                @Override
                public void validate(CosmosAsyncItemResponse itemResponse) {
                    assertThat(itemResponse.getResource()).isNull();
                }
            });
            return this; 
        }
    }
}
