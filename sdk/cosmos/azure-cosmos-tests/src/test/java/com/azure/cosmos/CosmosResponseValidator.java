// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.ContainerChildResourceType;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosPermissionResponse;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.models.SpatialSpec;
import com.azure.cosmos.models.SpatialType;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
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

        private Resource getResource(T resourceResponse) {
            if (resourceResponse instanceof CosmosDatabaseResponse) {
                return ModelBridgeInternal.getResource(((CosmosDatabaseResponse)resourceResponse).getProperties());
            } else if (resourceResponse instanceof CosmosContainerResponse) {
                return ModelBridgeInternal.getResource(((CosmosContainerResponse)resourceResponse).getProperties());
            } else if (resourceResponse instanceof CosmosStoredProcedureResponse) {
                return ModelBridgeInternal.getResource(((CosmosStoredProcedureResponse)resourceResponse).getProperties());
            } else if (resourceResponse instanceof CosmosTriggerResponse) {
                return ModelBridgeInternal.getResource(((CosmosTriggerResponse)resourceResponse).getProperties());
            } else if (resourceResponse instanceof CosmosUserDefinedFunctionResponse) {
                return ModelBridgeInternal.getResource(((CosmosUserDefinedFunctionResponse)resourceResponse).getProperties());
            } else if (resourceResponse instanceof CosmosUserResponse) {
                return ModelBridgeInternal.getResource(((CosmosUserResponse)resourceResponse).getProperties());
            } else if (resourceResponse instanceof CosmosPermissionResponse) {
                return ModelBridgeInternal.getResource(((CosmosPermissionResponse) resourceResponse).getProperties());
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
                    assertThat(resourceResponse.getProperties()).isNotNull();
                    assertThat(resourceResponse.getProperties().getIndexingPolicy()).isNotNull();
                    assertThat(resourceResponse.getProperties().getIndexingPolicy().getIndexingMode()).isEqualTo(mode);
                }
            });
            return this;
        }

        public Builder<T> withDefaultTimeToLive(Integer timeToLive) {
            validators.add(new CosmosResponseValidator<CosmosContainerResponse>() {

                @Override
                public void validate(CosmosContainerResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties()).isNotNull();
                    assertThat(resourceResponse.getProperties().getDefaultTimeToLiveInSeconds()).isNotNull();
                    assertThat(resourceResponse.getProperties().getDefaultTimeToLiveInSeconds()).isEqualTo(timeToLive);
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

        public Builder<T> withCompositeIndexes(List<List<CompositePath>> compositeIndexesWritten) {
            validators.add(new CosmosResponseValidator<CosmosContainerResponse>() {

                @Override
                public void validate(CosmosContainerResponse resourceResponse) {
                    Iterator<List<CompositePath>> compositeIndexesReadIterator = resourceResponse.getProperties()
                            .getIndexingPolicy().getCompositeIndexes().iterator();
                    Iterator<List<CompositePath>> compositeIndexesWrittenIterator = compositeIndexesWritten.iterator();

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
            validators.add(new CosmosResponseValidator<CosmosContainerResponse>() {

                @Override
                public void validate(CosmosContainerResponse resourceResponse) {
                    Iterator<SpatialSpec> spatialIndexesReadIterator = resourceResponse.getProperties()
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
                        Assertions.assertThat(entry.getValue())
                        .containsExactlyInAnyOrderElementsOf(writtenIndexMap.get(entry.getKey()));
                    }
                }
            });
            return this;
        }

        public Builder<T> withStoredProcedureBody(String storedProcedureBody) {
            validators.add(new CosmosResponseValidator<CosmosStoredProcedureResponse>() {

                @Override
                public void validate(CosmosStoredProcedureResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getBody()).isEqualTo(storedProcedureBody);
                }
            });
            return this;
        }

        public Builder<T> notNullEtag() {
            validators.add(new CosmosResponseValidator<T>() {

                @Override
                public void validate(T resourceResponse) {
                    assertThat(resourceResponse.getProperties()).isNotNull();
                    if (resourceResponse.getProperties() instanceof Resource) {
                        assertThat(((Resource)resourceResponse.getProperties()).getETag()).isNotNull();
                    }
                    if (resourceResponse.getProperties() instanceof CosmosConflictProperties
                        || resourceResponse.getProperties() instanceof CosmosContainerProperties
                        || resourceResponse.getProperties() instanceof CosmosDatabaseProperties
                        || resourceResponse.getProperties() instanceof CosmosPermissionProperties
                        || resourceResponse.getProperties() instanceof CosmosStoredProcedureProperties
                        || resourceResponse.getProperties() instanceof CosmosTriggerProperties
                        || resourceResponse.getProperties() instanceof CosmosUserDefinedFunctionProperties
                        || resourceResponse.getProperties() instanceof CosmosUserProperties) {

                        assertThat(
                            ModelBridgeInternal.getResource(resourceResponse.getProperties()).getETag())
                            .isNotNull();
                    }
                }
            });
            return this;
        }

        public Builder<T> withTriggerBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosTriggerResponse>() {

                @Override
                public void validate(CosmosTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withTriggerInternals(TriggerType type, TriggerOperation op) {
            validators.add(new CosmosResponseValidator<CosmosTriggerResponse>() {

                @Override
                public void validate(CosmosTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getTriggerType()).isEqualTo(type);
                    assertThat(resourceResponse.getProperties().getTriggerOperation()).isEqualTo(op);
                }
            });
            return this;
        }

        public Builder<T> withUserDefinedFunctionBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosUserDefinedFunctionResponse>() {

                @Override
                public void validate(CosmosUserDefinedFunctionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withPermissionMode(PermissionMode mode) {
            validators.add(new CosmosResponseValidator<CosmosPermissionResponse>() {

                @Override
                public void validate(CosmosPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getPermissionMode()).isEqualTo(mode);
                }
            });
            return this;

        }

        public Builder<T> withPermissionContainerName(String containerName) {
            validators.add(new CosmosResponseValidator<CosmosPermissionResponse>() {

                @Override
                public void validate(CosmosPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getContainerName()).isEqualTo(containerName);
                }
            });
            return this;
        }

        public Builder<T> withPermissionResourceName(String resourceName) {
            validators.add(new CosmosResponseValidator<CosmosPermissionResponse>() {

                @Override
                public void validate(CosmosPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getResourceName()).isEqualTo(resourceName);
                }
            });
            return this;
        }

        public Builder<T> withPermissionResourceKind(ContainerChildResourceType resourceKind) {
            validators.add(new CosmosResponseValidator<CosmosPermissionResponse>() {

                @Override
                public void validate(CosmosPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getResourceKind()).isEqualTo(resourceKind);
                }
            });
            return this;
        }
    }
}
