// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.models.SpatialSpec;
import com.azure.cosmos.models.SpatialType;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import org.assertj.core.api.Condition;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;

public interface ResourceResponseValidator<T extends Resource> {

    static <T extends Resource> Builder<T> builder() {
        return new Builder<>();
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
                    assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), propertyName)).is(validatingCondition);

                }
            });
            return this;
        }

        public Builder<T> withProperty(String propertyName, Object value) {
            validators.add(new ResourceResponseValidator<T>() {

                @Override
                public void validate(ResourceResponse<T> resourceResponse) {
                    assertThat(resourceResponse.getResource()).isNotNull();
                    assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), propertyName)).isEqualTo(value);

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
                    Instant d = resourceResponse.getResource().getTimestamp();
                    System.out.println(d.toString());
                    assertThat(d).isAfterOrEqualTo(time);
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
                    Instant d = resourceResponse.getResource().getTimestamp();
                    assertThat(d).isBeforeOrEqualTo(time);
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
                    assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(resourceResponse.getResource(), key)).is(condition);

                }
            });
            return this;
        }

        public Builder<T> withCompositeIndexes(Collection<ArrayList<CompositePath>> compositeIndexesWritten) {
            validators.add(new ResourceResponseValidator<DocumentCollection>() {

                @Override
                public void validate(ResourceResponse<DocumentCollection> resourceResponse) {
                    Iterator<List<CompositePath>> compositeIndexesReadIterator = resourceResponse.getResource()
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
