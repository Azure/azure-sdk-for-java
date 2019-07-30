// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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
                    assertThat(getResource(resourceResponse).id()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }

        private Resource getResource(T resourceResponse) {
            if (resourceResponse instanceof CosmosDatabaseResponse) {
                return ((CosmosDatabaseResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosContainerResponse) {
                return ((CosmosContainerResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosItemResponse) {
                return ((CosmosItemResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosStoredProcedureResponse) {
                return ((CosmosStoredProcedureResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosTriggerResponse) {
                return ((CosmosTriggerResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosUserDefinedFunctionResponse) {
                return ((CosmosUserDefinedFunctionResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosUserResponse) {
                return ((CosmosUserResponse)resourceResponse).properties();
            } else if (resourceResponse instanceof CosmosPermissionResponse) {
                return ((CosmosPermissionResponse) resourceResponse).properties();
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
                    assertThat(resourceResponse.properties()).isNotNull();
                    assertThat(resourceResponse.properties().indexingPolicy()).isNotNull();
                    assertThat(resourceResponse.properties().indexingPolicy().indexingMode()).isEqualTo(mode);
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
                    Iterator<List<CompositePath>> compositeIndexesReadIterator = resourceResponse.properties()
                            .indexingPolicy().compositeIndexes().iterator();
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
                            
                            readIndexesString.append(compositePathRead.path() + ":" + compositePathRead.order() + ";");
                            writtenIndexesString.append(compositePathWritten.path() + ":" + compositePathRead.order() + ";");
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
                    Iterator<SpatialSpec> spatialIndexesReadIterator = resourceResponse.properties()
                            .indexingPolicy().spatialIndexes().iterator();
                    Iterator<SpatialSpec> spatialIndexesWrittenIterator = spatialIndexes.iterator();

                    HashMap<String, ArrayList<SpatialType>> readIndexMap = new HashMap<String, ArrayList<SpatialType>>();
                    HashMap<String, ArrayList<SpatialType>> writtenIndexMap = new HashMap<String, ArrayList<SpatialType>>();

                    while (spatialIndexesReadIterator.hasNext() && spatialIndexesWrittenIterator.hasNext()) {
                        SpatialSpec spatialSpecRead = spatialIndexesReadIterator.next();
                        SpatialSpec spatialSpecWritten = spatialIndexesWrittenIterator.next();

                        String readPath = spatialSpecRead.path() + ":";
                        String writtenPath = spatialSpecWritten.path() + ":";

                        ArrayList<SpatialType> readSpatialTypes = new ArrayList<SpatialType>();
                        ArrayList<SpatialType> writtenSpatialTypes = new ArrayList<SpatialType>();
                        
                        Iterator<SpatialType> spatialTypesReadIterator = spatialSpecRead.spatialTypes().iterator();
                        Iterator<SpatialType> spatialTypesWrittenIterator = spatialSpecWritten.spatialTypes().iterator();

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

        public Builder<T> withStoredProcedureBody(String storedProcedureBody) {
            validators.add(new CosmosResponseValidator<CosmosStoredProcedureResponse>() {

                @Override
                public void validate(CosmosStoredProcedureResponse resourceResponse) {
                    assertThat(resourceResponse.properties().body()).isEqualTo(storedProcedureBody);
                }
            });
            return this;
        }
        
        public Builder<T> notNullEtag() {
            validators.add(new CosmosResponseValidator<T>() {

                @Override
                public void validate(T resourceResponse) {
                    assertThat(resourceResponse.resourceSettings()).isNotNull();
                    assertThat(resourceResponse.resourceSettings().etag()).isNotNull();
                }
            });
            return this;
        }

        public Builder<T> withTriggerBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosTriggerResponse>() {

                @Override
                public void validate(CosmosTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.properties().body()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withTriggerInternals(TriggerType type, TriggerOperation op) {
            validators.add(new CosmosResponseValidator<CosmosTriggerResponse>() {

                @Override
                public void validate(CosmosTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.properties().triggerType()).isEqualTo(type);
                    assertThat(resourceResponse.properties().triggerOperation()).isEqualTo(op);
                }
            });
            return this;
        }

        public Builder<T> withUserDefinedFunctionBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosUserDefinedFunctionResponse>() {

                @Override
                public void validate(CosmosUserDefinedFunctionResponse resourceResponse) {
                    assertThat(resourceResponse.properties().body()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withPermissionMode(PermissionMode mode) {
            validators.add(new CosmosResponseValidator<CosmosPermissionResponse>() {

                @Override
                public void validate(CosmosPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.properties().permissionMode()).isEqualTo(mode);
                }
            });
            return this;

        }

        public Builder<T> withPermissionResourceLink(String resourceLink) {
            validators.add(new CosmosResponseValidator<CosmosPermissionResponse>() {

                @Override
                public void validate(CosmosPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.properties().resourceLink()).isEqualTo(resourceLink);
                }
            });
            return this;
        }
    }
}
