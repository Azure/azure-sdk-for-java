// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.ModelBridgeInternal;

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
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(((CosmosException) t).getStatusCode()).isEqualTo(statusCode);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsnGreaterThan(long quorumAckedLSN) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(BridgeInternal.getLSN((CosmosException) t) > quorumAckedLSN).isTrue();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsnGreaterThanEqualsTo(long quorumAckedLSN) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(BridgeInternal.getLSN((CosmosException) t) >= quorumAckedLSN).isTrue();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder exceptionQuorumAckedLSNInNotNull() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException cosmosException = (CosmosException) t;
                    long exceptionQuorumAckedLSN = -1;
                    if (cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN) != null) {
                        exceptionQuorumAckedLSN = Long.parseLong(cosmosException.getResponseHeaders().get(WFConstants.BackendHeaders.QUORUM_ACKED_LSN));

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
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(((CosmosException) t).getActivityId()).isNotNull();
                }
            });
            return this;
        }

        public <T extends Throwable> Builder error(CosmosError cosmosError) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(ModelBridgeInternal.toJsonFromJsonSerializable(BridgeInternal.getCosmosError((CosmosException) t)))
                        .isEqualTo(ModelBridgeInternal.toJsonFromJsonSerializable(cosmosError));
                }
            });
            return this;
        }

        public <T extends Throwable> Builder subStatusCode(Integer substatusCode) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(((CosmosException) t).getSubStatusCode()).isEqualTo(substatusCode);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder unknownSubStatusCode() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(((CosmosException) t).getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder responseHeader(String key, String value) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    assertThat(((CosmosException) t).getResponseHeaders().get(key)).isEqualTo(value);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder lsn(long lsn) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
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
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
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
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
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
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
                    assertThat(ex.getStatusCode()).isEqualTo(404);

                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceTokenNotFound() {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(UnauthorizedException.class);
                    UnauthorizedException ex = (UnauthorizedException) t;
                    assertThat(ex.getMessage()).contains(RMResources.ResourceTokenNotFound);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder resourceAlreadyExists() {

            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
                    assertThat(ex.getStatusCode()).isEqualTo(409);

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
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
                    assertThat(BridgeInternal.getRequestHeaders(ex)).containsEntry(key, value);
                }
            });
            return this;
        }

        public <T extends Throwable> Builder documentClientExceptionToStringExcludesHeader(String header) {
            validators.add(new FailureValidator() {
                @Override
                public void validate(Throwable t) {
                    assertThat(t).isNotNull();
                    assertThat(t).isInstanceOf(CosmosException.class);
                    CosmosException ex = (CosmosException) t;
                    String exceptionToString = ex.toString();
                    assertThat(exceptionToString).doesNotContain(header);
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

        public <T extends Throwable> Builder withRuntimeExceptionClass(Class<T> k) {
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
