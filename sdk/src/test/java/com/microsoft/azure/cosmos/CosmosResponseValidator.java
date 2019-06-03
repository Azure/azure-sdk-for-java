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
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.Resource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface CosmosResponseValidator<T extends CosmosResponse> {
    void validate(T cosmosResponse);

    class Builder<T extends CosmosResponse> {
        private List<CosmosResponseValidator<? extends CosmosResponse>> validators = new ArrayList<>();

        public CosmosResponseValidator<T> build() {
            return new CosmosResponseValidator<T>() {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public void validate(T resourceResponse) {
                    for (CosmosResponseValidator validator : validators) {
                        validator.validate(resourceResponse);
                    }
                }
            };
        }

        public Builder<T> withId(final String resourceId) {
            validators.add(new CosmosResponseValidator<T>() {

                @Override
                public void validate(T resourceResponse) {
                    assertThat(getResource(resourceResponse)).isNotNull();
                    assertThat(getResource(resourceResponse).getId()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }

        private  Resource getResource(T resourceResponse) {
            if(resourceResponse instanceof CosmosDatabaseResponse){
                return ((CosmosDatabaseResponse)resourceResponse).getCosmosDatabaseSettings();
            }else if(resourceResponse instanceof CosmosContainerResponse){
                return ((CosmosContainerResponse)resourceResponse).getCosmosContainerSettings();
            }else if(resourceResponse instanceof CosmosItemResponse){
                return ((CosmosItemResponse)resourceResponse).getCosmosItemSettings();
            }
            return null;
        }

        public Builder<T> nullResource() {
            validators.add(new CosmosResponseValidator<T>() {

                @Override
                public void validate(T resourceResponse) {
                    assertThat(getResource(resourceResponse)).isNull();
                }
            });
            return this;
        }

        public Builder<T> indexingMode(IndexingMode mode) {
            validators.add(new CosmosResponseValidator<CosmosContainerResponse>() {
                
                @Override
                public void validate(CosmosContainerResponse resourceResponse) {
                    assertThat(resourceResponse.getCosmosContainerSettings()).isNotNull();
                    assertThat(resourceResponse.getCosmosContainerSettings().getIndexingPolicy()).isNotNull();
                    assertThat(resourceResponse.getCosmosContainerSettings().getIndexingPolicy().getIndexingMode()).isEqualTo(mode);
                }
            });
            return this;
        }

        public Builder<T> withProperty(String propertyName, String value) {
            validators.add(new CosmosResponseValidator<T>() {
                @Override
                public void validate(T cosmosResponse) {
                    assertThat(getResource(cosmosResponse)).isNotNull();
                    assertThat(getResource(cosmosResponse).get(propertyName)).isEqualTo(value);
                }
            });
            return this;
        }
    }
}
