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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.assertj.core.api.Condition;

import com.microsoft.azure.cosmosdb.Attachment;
import com.microsoft.azure.cosmosdb.CompositePath;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.PermissionMode;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SpatialSpec;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.Trigger;
import com.microsoft.azure.cosmosdb.TriggerOperation;
import com.microsoft.azure.cosmosdb.TriggerType;
import com.microsoft.azure.cosmosdb.UserDefinedFunction;
import com.microsoft.azure.cosmosdb.SpatialType;

public interface ResourceResponseValidator<T extends Resource> {

    static <T extends Resource> Builder builder() {
        return new Builder();
    }

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


        public Builder<T> withTimestampIsAfterOrEqualTo(Instant time) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().getTimestamp()).isNotNull();
                    Date d = resourceResponse.getResource().getTimestamp();
                    System.out.println(d.toString());
                    assertThat(d.toInstant()).isAfterOrEqualTo(time);
                }
            });
            return this;
        }

        public Builder<T> withTimestampIsBeforeOrEqualTo(Instant time) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().getTimestamp()).isNotNull();
                    Date d = resourceResponse.getResource().getTimestamp();
                    assertThat(d.toInstant()).isBeforeOrEqualTo(time);
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

        public Builder<T> notEmptySelfLink() {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(resourceResponse.getResource().getSelfLink()).isNotEmpty();
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
                    assertThat(resourceResponse.getResource().getThroughput())
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

        public Builder<T> withCompositeIndexes(Collection<ArrayList<CompositePath>> compositeIndexesWritten) {
            validators.add(new ResourceResponseValidator<DocumentCollection>() {

                @Override
                public void validate(ResourceResponse<DocumentCollection> resourceResponse) {
                    Iterator<ArrayList<CompositePath>> compositeIndexesReadIterator = resourceResponse.getResource()
                            .getIndexingPolicy().getCompositeIndexes().iterator();
                    Iterator<ArrayList<CompositePath>> compositeIndexesWrittenIterator = compositeIndexesWritten.iterator();
                    
                    ArrayList<String> readIndexesStrings = new ArrayList<String>();
                    ArrayList<String> writtenIndexesStrings = new ArrayList<String>();
                    
                    while (compositeIndexesReadIterator.hasNext() && compositeIndexesWrittenIterator.hasNext()) {
                        Iterator<CompositePath> compositeIndexReadIterator = compositeIndexesReadIterator.next().iterator();
                        Iterator<CompositePath> compositeIndexWrittenIterator = compositeIndexesWrittenIterator.next().iterator();

                        StringBuilder readIndexesString = new StringBuilder();
                        StringBuilder writtenIndexesString = new StringBuilder();
                        
                        while (compositeIndexReadIterator.hasNext() && compositeIndexWrittenIterator.hasNext()) {
                            CompositePath compositePathRead = compositeIndexReadIterator.next();
                            CompositePath compositePathWritten = compositeIndexWrittenIterator.next();
                            
                            readIndexesString.append(compositePathRead.getPath() + ":" + compositePathRead.getOrder() + ";");
                            writtenIndexesString.append(compositePathWritten.getPath() + ":" + compositePathRead.getOrder() + ";");
                        }
                        
                        readIndexesStrings.add(readIndexesString.toString());
                        writtenIndexesStrings.add(writtenIndexesString.toString());
                    }
                    
                    assertThat(readIndexesStrings).containsExactlyInAnyOrderElementsOf(writtenIndexesStrings);
                }
            });
            return this;
        }

        public Builder<T> withSpatialIndexes(Collection<SpatialSpec> spatialIndexes) {
            validators.add(new ResourceResponseValidator<DocumentCollection>() {

                @Override
                public void validate(ResourceResponse<DocumentCollection> resourceResponse) {
                    Iterator<SpatialSpec> spatialIndexesReadIterator = resourceResponse.getResource()
                            .getIndexingPolicy().getSpatialIndexes().iterator();
                    Iterator<SpatialSpec> spatialIndexesWrittenIterator = spatialIndexes.iterator();

                    HashMap<String, ArrayList<SpatialType>> readIndexMap = new HashMap<String, ArrayList<SpatialType>>();
                    HashMap<String, ArrayList<SpatialType>> writtenIndexMap = new HashMap<String, ArrayList<SpatialType>>();

                    while (spatialIndexesReadIterator.hasNext() && spatialIndexesWrittenIterator.hasNext()) {
                        SpatialSpec spatialSpecRead = spatialIndexesReadIterator.next();
                        SpatialSpec spatialSpecWritten = spatialIndexesWrittenIterator.next();

                        String readPath = spatialSpecRead.getPath() + ":";
                        String writtenPath = spatialSpecWritten.getPath() + ":";

                        ArrayList<SpatialType> readSpatialTypes = new ArrayList<SpatialType>();
                        ArrayList<SpatialType> writtenSpatialTypes = new ArrayList<SpatialType>();
                        
                        Iterator<SpatialType> spatialTypesReadIterator = spatialSpecRead.getSpatialTypes().iterator();
                        Iterator<SpatialType> spatialTypesWrittenIterator = spatialSpecWritten.getSpatialTypes().iterator();

                        while (spatialTypesReadIterator.hasNext() && spatialTypesWrittenIterator.hasNext()) {
                            readSpatialTypes.add(spatialTypesReadIterator.next());
                            writtenSpatialTypes.add(spatialTypesWrittenIterator.next());
                        }
                        
                        readIndexMap.put(readPath, readSpatialTypes);
                        writtenIndexMap.put(writtenPath, writtenSpatialTypes);
                    }
                    
                    for (Entry<String, ArrayList<SpatialType>> entry : readIndexMap.entrySet()) {
                        assertThat(entry.getValue())
                        .containsExactlyInAnyOrderElementsOf(writtenIndexMap.get(entry.getKey()));
                    }
                }
            });
            return this;
        }
    }
}
