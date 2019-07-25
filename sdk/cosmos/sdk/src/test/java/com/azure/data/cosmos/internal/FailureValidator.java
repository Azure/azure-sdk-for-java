// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosError;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public interface FailureValidator {

    static Builder builder() {
        return new Builder();
    }

    void validate(Throwable t);

    class Builder {
        private List<FailureValidator> validators = new ArrayList<>();

        public FailureValidator build() {
            return new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    for (FailureValidator validator : validators) {
                        validator.validate(t);
                    }
                }
            };
        }

        public <T extends Throwable> Builder statusCode(int statusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(((CosmosClientException) t).statusCode()).isEqualTo(statusCode);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsnGreaterThan(long quorumAckedLSN) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(BridgeInternal.getLSN((CosmosClientException) t) > quorumAckedLSN).isTrue();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsnGreaterThanEqualsTo(long quorumAckedLSN) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(BridgeInternal.getLSN((CosmosClientException) t) >= quorumAckedLSN).isTrue();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder exceptionQuorumAckedLSNInNotNull() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException cosmosClientException = (CosmosClientException) t;
                    long exceptionQuorumAckedLSN = -1;
                    if (cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN) != null) {
                        exceptionQuorumAckedLSN = Long.parseLong((String) cosmosClientException.responseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));

                    }
                    assertThat(exceptionQuorumAckedLSN).isNotEqualTo(-1);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder errorMessageContains(String errorMsg) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t.getMessage()).contains(errorMsg);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder notNullActivityId() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(((CosmosClientException) t).message()).isNotNull();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder error(CosmosError cosmosError) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(((CosmosClientException) t).error().toJson()).isEqualTo(cosmosError.toJson());
                }
            });
            return this;
        }

        public <T extends Throwable> Builder subStatusCode(Integer substatusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(((CosmosClientException) t).subStatusCode()).isEqualTo(substatusCode);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder unknownSubStatusCode() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(((CosmosClientException) t).subStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder responseHeader(String key, String value) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    assertThat(((CosmosClientException) t).responseHeaders().get(key)).isEqualTo(value);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsn(long lsn) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException ex = (CosmosClientException) t;
                    assertThat(BridgeInternal.getLSN(ex)).isEqualTo(lsn);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder partitionKeyRangeId(String pkrid) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException ex = (CosmosClientException) t;
                    assertThat(BridgeInternal.getPartitionKeyRangeId(ex)).isEqualTo(pkrid);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceAddress(String resourceAddress) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException ex = (CosmosClientException) t;
                    assertThat(BridgeInternal.getResourceAddress(ex)).isEqualTo(resourceAddress);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder instanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(cls);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder sameAs(Exception exception) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isSameAs(exception);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceNotFound() {

            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException ex = (CosmosClientException) t;
                    assertThat(ex.statusCode()).isEqualTo(404);

                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceTokenNotFound() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(IllegalArgumentException.class);
                    IllegalArgumentException ex = (IllegalArgumentException) t;
                    assertThat(ex.getMessage()).isEqualTo(RMResources.ResourceTokenNotFound);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceAlreadyExists() {

            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException ex = (CosmosClientException) t;
                    assertThat(ex.statusCode()).isEqualTo(409);

                }
            });
            return this;
        }

        public <T extends Throwable> Builder causeInstanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t.getCause()).isNotNull();
                    assertThat(t.getCause()).isInstanceOf(cls);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder causeOfCauseInstanceOf(Class<T> cls) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t.getCause()).isNotNull();
                    assertThat(t.getCause().getCause()).isNotNull();
                    assertThat(t.getCause().getCause()).isInstanceOf(cls);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder documentClientExceptionHeaderRequestContainsEntry(String key, String value) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosClientException.class);
                    CosmosClientException ex = (CosmosClientException) t;
                    assertThat(BridgeInternal.getRequestHeaders(ex)).containsEntry(key, value);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder withRuntimeExceptionMessage(String message) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(RuntimeException.class);
                    assertThat(t.getMessage()).isEqualTo(message);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder withRuntimeExceptionClass(Class k) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(RuntimeException.class);
                    assertThat(t).isInstanceOf(k);
                }
            });
            return this;
        }
    }
}
