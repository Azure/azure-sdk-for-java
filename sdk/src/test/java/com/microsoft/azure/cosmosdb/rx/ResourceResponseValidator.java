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

import org.assertj.core.api.Condition;

import com.microsoft.azure.cosmosdb.Attachment;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.PermissionMode;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.Trigger;
import com.microsoft.azure.cosmosdb.TriggerOperation;
import com.microsoft.azure.cosmosdb.TriggerType;
import com.microsoft.azure.cosmosdb.UserDefinedFunction;

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

        public Builder<T> withPermissionMode(PermissionMode mode) {
            validators.add(new ResourceResponseValidator<Permission>() {

                @Override
                public void validate(ResourceResponse<Permission> resourceResponse) {
                    assertThat(resourceResponse.getResource().getPermissionMode()).isEqualTo(mode);
                }
            });
            return this;
        }
        
        public Builder<T> withPermissionResourceLink(String link) {
            validators.add(new ResourceResponseValidator<Permission>() {

                @Override
                public void validate(ResourceResponse<Permission> resourceResponse) {
                    assertThat(resourceResponse.getResource().getResourceLink()).isEqualTo(link);
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

        public Builder<T> withStoredProcedureBody(String functionBody) {
            validators.add(new ResourceResponseValidator<StoredProcedure>() {

                @Override
                public void validate(ResourceResponse<StoredProcedure> resourceResponse) {
                    assertThat(resourceResponse.getResource().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }
        
        public Builder<T> withUserDefinedFunctionBody(String functionBody) {
            validators.add(new ResourceResponseValidator<UserDefinedFunction>() {

                @Override
                public void validate(ResourceResponse<UserDefinedFunction> resourceResponse) {
                    assertThat(resourceResponse.getResource().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        
        public Builder<T> withTriggerBody(String functionBody) {
            validators.add(new ResourceResponseValidator<Trigger>() {

                @Override
                public void validate(ResourceResponse<Trigger> resourceResponse) {
                    assertThat(resourceResponse.getResource().getBody()).isEqualTo(functionBody);
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

        public Builder<T> withTriggerInternals(TriggerType type, TriggerOperation op) {
            validators.add(new ResourceResponseValidator<Trigger>() {

                @Override
                public void validate(ResourceResponse<Trigger> resourceResponse) {
                    assertThat(resourceResponse.getResource().getTriggerType()).isEqualTo(type);
                    assertThat(resourceResponse.getResource().getTriggerOperation()).isEqualTo(op);
                }
            });
            return this;
        }

        public Builder<T> withContentType(final String contentType) {
            validators.add(new ResourceResponseValidator<Attachment>() {

                @Override
                public void validate(ResourceResponse<Attachment> resourceResponse) {
                    assertThat(resourceResponse.getResource().getContentType()).isEqualTo(contentType);
                }
            });
            return this;
        }

        public Builder<T> withOfferThroughput(int throughput) {
            validators.add(new ResourceResponseValidator<Offer>() {

                @Override
                public void validate(ResourceResponse<Offer> resourceResponse) {
                    assertThat(resourceResponse.getResource().getContent().getInt("offerThroughput"))
                            .isEqualTo(throughput);
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
