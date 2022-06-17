// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.ModelBridgeInternal;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface ResourceValidator<T> {

    void validate(T v);

    class Builder<T extends Resource> {
        private List<ResourceValidator<?>> validators = new ArrayList<>();

        public ResourceValidator<T> build() {
            return new ResourceValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(T v) {
                    for (ResourceValidator validator : validators) {
                        validator.validate(v);
                    }
                }
            };
        }

        public Builder<T> areEqual(T expectedValue) {
            validators.add(new ResourceValidator<T>() {
                @Override
                public void validate(T v) {

                    assertThat(v.getMap().keySet())
                        .describedAs("number of fields")
                        .hasSize(expectedValue.getMap().keySet().size());
                    for(String key : expectedValue.getMap().keySet()) {
                        assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(expectedValue, key))
                        .describedAs("value for " + key)
                        .isEqualTo(ModelBridgeInternal.getObjectFromJsonSerializable(expectedValue, key));
                    }
                }
            });
            return this;
        }

    }
}
