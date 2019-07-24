// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.FailureValidator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;


public interface StoreResultValidator {

    static Builder create() {
        return new Builder();
    }

    void validate(StoreResult storeResult);

    class Builder {
        private List<StoreResultValidator> validators = new ArrayList<>();

        public StoreResultValidator build() {
            return new StoreResultValidator() {

                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public void validate(StoreResult storeResult) {
                    for (StoreResultValidator validator : validators) {
                        validator.validate(storeResult);
                    }
                }
            };
        }

        public Builder withStoreResponse(StoreResponseValidator storeResponseValidator) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    try {
                        storeResponseValidator.validate(storeResult.toResponse());
                    }catch (CosmosClientException e) {
                        fail(e.getMessage());
                    }
                }
            });
            return this;
        }

        public Builder withException(FailureValidator failureValidator) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    try {
                        failureValidator.validate(storeResult.getException());
                    }catch (CosmosClientException e) {
                        fail(e.getMessage());
                    }
                }
            });
            return this;
        }

        public Builder withLSN(long lsn) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.lsn).isEqualTo(lsn);
                }
            });
            return this;
        }

        public Builder withMinLSN(long minLSN) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.lsn).isGreaterThanOrEqualTo(minLSN);
                }
            });
            return this;
        }

        public Builder withGlobalCommitedLSN(long globalLsn) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.globalCommittedLSN).isEqualTo(globalLsn);
                }
            });
            return this;
        }

        public Builder withQuorumAckedLsn(long quorumAckedLsn) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.quorumAckedLSN).isEqualTo(quorumAckedLsn);
                }
            });
            return this;
        }

        public Builder noException() {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult).hasFieldOrPropertyWithValue("exception", null);
                    assertThat(storeResult.isGoneException).isFalse();
                }
            });
            return this;
        }

        public Builder isValid() {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.isValid).isTrue();
                }
            });
            return this;
        }

        public Builder withReplicaSize(int count) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.currentReplicaSetSize).isEqualTo(count);
                }
            });
            return this;
        }

        public Builder withStorePhysicalURI(URI expectedURi) {
            validators.add(new StoreResultValidator() {

                @Override
                public void validate(StoreResult storeResult) {
                    assertThat(storeResult.storePhysicalAddress).isEqualTo(expectedURi);
                }
            });
            return this;
        }
    }
}
