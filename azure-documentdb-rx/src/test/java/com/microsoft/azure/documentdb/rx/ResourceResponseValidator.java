/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Condition;

import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.IndexingMode;
import com.microsoft.azure.documentdb.Resource;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.StoredProcedure;

public interface ResourceResponseValidator<T extends Resource> {

    void validate(ResourceResponse<T> resourceResponse);

    class Builder<T extends Resource> {
        private List<ResourceResponseValidator<? extends Resource>> validators = new ArrayList<>();

        public ResourceResponseValidator<T> build() {
            return new ResourceResponseValidator<T>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    for (ResourceResponseValidator validator : validators) {
                        validator.validate(resourceResponse);
                    }
                }
            };
        }

        public Builder<T> withId(final String resourceId) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().getId()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }

        public Builder<T> nullResource() {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNull();
                }
            });
            return this;
        }

        public Builder<T> withProperty(String propertyName, Condition<Object> validatingCondition) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().get(propertyName)).is(validatingCondition);

                }
            });
            return this;
        }
        
        public Builder<T> withProperty(String propertyName, Object value) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().get(propertyName)).isEqualTo(value);

                }
            });
            return this;
        }


        public Builder<T> indexingMode(IndexingMode mode) {
            validators.add(new ResourceResponseValidator<DocumentCollection>() {

                @Override
                public void validate(ResourceResponse<DocumentCollection> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().getIndexingPolicy()).isNotNull();
                    assertThat(resourceResponse.getResource().getIndexingPolicy().getIndexingMode()).isEqualTo(mode);
                }
            });
            return this;
        }

        public Builder<T> withBody(String storedProcedureFunction) {
            validators.add(new ResourceResponseValidator<StoredProcedure>() {

                @Override
                public void validate(ResourceResponse<StoredProcedure> resourceResponse) {
                    assertThat(resourceResponse.getResource().getBody()).isEqualTo(storedProcedureFunction);
                }
            });
            return this;
        }

        public Builder<T> notNullEtag() {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().getETag()).isNotNull();
                }
            });
            return this;
        }
        
        public Builder<T> validatePropertyCondition(String key, Condition<Object> condition) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().get(key)).is(condition);

                }
            });
            return this;
        }
    }
}
