// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.assertj.core.api.Assertions;

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
                    assertThat(getResource(resourceResponse).getId()).as("check Resource Id").isEqualTo(resourceId);
                }
            });
            return this;
        }

        private Resource getResource(T resourceResponse) {
            if (resourceResponse instanceof CosmosAsyncDatabaseResponse) {
                return ((CosmosAsyncDatabaseResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncContainerResponse) {
                return ((CosmosAsyncContainerResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncItemResponse) {
                return ((CosmosAsyncItemResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncStoredProcedureResponse) {
                return ((CosmosAsyncStoredProcedureResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncTriggerResponse) {
                return ((CosmosAsyncTriggerResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncUserDefinedFunctionResponse) {
                return ((CosmosAsyncUserDefinedFunctionResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncUserResponse) {
                return ((CosmosAsyncUserResponse)resourceResponse).getProperties();
            } else if (resourceResponse instanceof CosmosAsyncPermissionResponse) {
                return ((CosmosAsyncPermissionResponse) resourceResponse).getProperties();
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
            validators.add(new CosmosResponseValidator<CosmosAsyncContainerResponse>() {
                
                @Override
                public void validate(CosmosAsyncContainerResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties()).isNotNull();
                    assertThat(resourceResponse.getProperties().getIndexingPolicy()).isNotNull();
                    assertThat(resourceResponse.getProperties().getIndexingPolicy().getIndexingMode()).isEqualTo(mode);
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
            validators.add(new CosmosResponseValidator<CosmosAsyncContainerResponse>() {

                @Override
                public void validate(CosmosAsyncContainerResponse resourceResponse) {
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
            validators.add(new CosmosResponseValidator<CosmosAsyncContainerResponse>() {

                @Override
                public void validate(CosmosAsyncContainerResponse resourceResponse) {
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
            validators.add(new CosmosResponseValidator<CosmosAsyncStoredProcedureResponse>() {

                @Override
                public void validate(CosmosAsyncStoredProcedureResponse resourceResponse) {
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
                    assertThat(resourceResponse.getProperties().getETag()).isNotNull();
                }
            });
            return this;
        }

        public Builder<T> withTriggerBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosAsyncTriggerResponse>() {

                @Override
                public void validate(CosmosAsyncTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withTriggerInternals(TriggerType type, TriggerOperation op) {
            validators.add(new CosmosResponseValidator<CosmosAsyncTriggerResponse>() {

                @Override
                public void validate(CosmosAsyncTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getTriggerType()).isEqualTo(type);
                    assertThat(resourceResponse.getProperties().getTriggerOperation()).isEqualTo(op);
                }
            });
            return this;
        }

        public Builder<T> withUserDefinedFunctionBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosAsyncUserDefinedFunctionResponse>() {

                @Override
                public void validate(CosmosAsyncUserDefinedFunctionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withPermissionMode(PermissionMode mode) {
            validators.add(new CosmosResponseValidator<CosmosAsyncPermissionResponse>() {

                @Override
                public void validate(CosmosAsyncPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getPermissionMode()).isEqualTo(mode);
                }
            });
            return this;

        }

        public Builder<T> withPermissionResourceLink(String resourceLink) {
            validators.add(new CosmosResponseValidator<CosmosAsyncPermissionResponse>() {

                @Override
                public void validate(CosmosAsyncPermissionResponse resourceResponse) {
                    assertThat(resourceResponse.getProperties().getResourceLink()).isEqualTo(resourceLink);
                }
            });
            return this;
        }
    }
}
