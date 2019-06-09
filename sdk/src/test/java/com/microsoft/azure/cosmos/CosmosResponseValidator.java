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

import com.microsoft.azure.cosmosdb.CompositePath;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SpatialSpec;
import com.microsoft.azure.cosmosdb.SpatialType;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.Trigger;
import com.microsoft.azure.cosmosdb.TriggerOperation;
import com.microsoft.azure.cosmosdb.TriggerType;
import com.microsoft.azure.cosmosdb.UserDefinedFunction;

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

        private  Resource getResource(T resourceResponse) {
            if (resourceResponse instanceof CosmosDatabaseResponse) {
                return ((CosmosDatabaseResponse)resourceResponse).getCosmosDatabaseSettings();
            } else if (resourceResponse instanceof CosmosContainerResponse) {
                return ((CosmosContainerResponse)resourceResponse).getCosmosContainerSettings();
            } else if (resourceResponse instanceof CosmosItemResponse) {
                return ((CosmosItemResponse)resourceResponse).getCosmosItemSettings();
            } else if (resourceResponse instanceof CosmosStoredProcedureResponse) {
                return ((CosmosStoredProcedureResponse)resourceResponse).getStoredProcedureSettings();
            } else if (resourceResponse instanceof CosmosTriggerResponse) {
                return ((CosmosTriggerResponse)resourceResponse).getCosmosTriggerSettings();
            } else if (resourceResponse instanceof CosmosUserDefinedFunctionResponse) {
                return ((CosmosUserDefinedFunctionResponse)resourceResponse).getCosmosUserDefinedFunctionSettings();
            } else if (resourceResponse instanceof CosmosUserResponse) {
                return ((CosmosUserResponse)resourceResponse).getCosmosUserSettings();
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

        public Builder<T> withCompositeIndexes(Collection<ArrayList<CompositePath>> compositeIndexesWritten) {
            validators.add(new CosmosResponseValidator<CosmosContainerResponse>() {

                @Override
                public void validate(CosmosContainerResponse resourceResponse) {
                    Iterator<ArrayList<CompositePath>> compositeIndexesReadIterator = resourceResponse.getCosmosContainerSettings()
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
            validators.add(new CosmosResponseValidator<CosmosContainerResponse>() {

                @Override
                public void validate(CosmosContainerResponse resourceResponse) {
                    Iterator<SpatialSpec> spatialIndexesReadIterator = resourceResponse.getCosmosContainerSettings()
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

        public Builder<T> withStoredProcedureBody(String storedProcedureBody) {
            validators.add(new CosmosResponseValidator<CosmosStoredProcedureResponse>() {

                @Override
                public void validate(CosmosStoredProcedureResponse resourceResponse) {
                    assertThat(resourceResponse.getStoredProcedureSettings().getBody()).isEqualTo(storedProcedureBody);
                }
            });
            return this;
        }
        
        public Builder<T> notNullEtag() {
            validators.add(new CosmosResponseValidator<T>() {

                @Override
                public void validate(T resourceResponse) {
                    assertThat(resourceResponse.getResourceSettings()).isNotNull();
                    assertThat(resourceResponse.getResourceSettings().getETag()).isNotNull();
                }
            });
            return this;
        }

        public Builder<T> withTriggerBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosTriggerResponse>() {

                @Override
                public void validate(CosmosTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.getCosmosTriggerSettings().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }

        public Builder<T> withTriggerInternals(TriggerType type, TriggerOperation op) {
            validators.add(new CosmosResponseValidator<CosmosTriggerResponse>() {

                @Override
                public void validate(CosmosTriggerResponse resourceResponse) {
                    assertThat(resourceResponse.getCosmosTriggerSettings().getTriggerType()).isEqualTo(type);
                    assertThat(resourceResponse.getCosmosTriggerSettings().getTriggerOperation()).isEqualTo(op);
                }
            });
            return this;
        }

        public Builder<T> withUserDefinedFunctionBody(String functionBody) {
            validators.add(new CosmosResponseValidator<CosmosUserDefinedFunctionResponse>() {

                @Override
                public void validate(CosmosUserDefinedFunctionResponse resourceResponse) {
                    assertThat(resourceResponse.getCosmosUserDefinedFunctionSettings().getBody()).isEqualTo(functionBody);
                }
            });
            return this;
        }
    }
}
