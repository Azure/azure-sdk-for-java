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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.rx.FailureValidator;

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
