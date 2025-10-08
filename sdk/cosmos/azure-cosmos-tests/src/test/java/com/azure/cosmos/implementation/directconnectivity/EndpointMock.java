// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class EndpointMock {

    TransportClientWrapper transportClientWrapper;
    AddressSelectorWrapper addressSelectorWrapper;

    public EndpointMock(AddressSelectorWrapper addressSelectorWrapper, TransportClientWrapper transportClientWrapper) {
        this.addressSelectorWrapper = addressSelectorWrapper;
        this.transportClientWrapper = transportClientWrapper;
    }

    public static class EndpointMockVerificationBuilder {
        public static EndpointMockVerificationBuilder builder() {
            return new EndpointMockVerificationBuilder();
        }

        private AddressSelectorWrapper.InOrderVerificationBuilder addressSelectorVerificationBuilder;
        private TransportClientWrapper.TransportClientWrapperVerificationBuilder transportClientValidation;

        public EndpointMockVerificationBuilder withAddressSelectorValidation(AddressSelectorWrapper.InOrderVerificationBuilder addressSelectorBuilder) {
            addressSelectorVerificationBuilder = addressSelectorBuilder;
            return this;
        }

        public EndpointMockVerificationBuilder withTransportClientValidation(TransportClientWrapper.TransportClientWrapperVerificationBuilder transportClientValidation) {
            this.transportClientValidation = transportClientValidation;
            return this;
        }

        public void execute(EndpointMock endpointMock) {
            this.addressSelectorVerificationBuilder.execute(endpointMock.addressSelectorWrapper);
            this.transportClientValidation.execute(endpointMock.transportClientWrapper);
        }
    }


    public void validate(EndpointMockVerificationBuilder verificationBuilder) {
        this.addressSelectorWrapper.validate();
        this.transportClientWrapper.validate();
        if (verificationBuilder != null) {
            verificationBuilder.execute(this);
        }
    }

    public static Builder.NoSecondaryReplica noSecondaryReplicaBuilder() throws Exception {
        return new Builder.NoSecondaryReplica();
    }

    abstract static class Builder {

        class ReplicasWithSameSpeed extends Builder {

            Uri primary;
            List<Uri> secondaries = new ArrayList<>();
            StoreResponse headStoreResponse;
            StoreResponse readStoreResponse;

            ReplicasWithSameSpeed addPrimary(Uri replicaAddress) {
                primary = replicaAddress;
                return this;
            }

            ReplicasWithSameSpeed addSecondary(Uri replicaAddress) {
                secondaries.add(replicaAddress);
                return this;
            }

            ReplicasWithSameSpeed storeResponseOnRead(StoreResponse storeResponse) {
                this.readStoreResponse = storeResponse;
                return this;
            }

            ReplicasWithSameSpeed storeResponseOnHead(StoreResponse storeResponse) {
                this.headStoreResponse = storeResponse;
                return this;
            }

            public EndpointMock build() {
                TransportClientWrapper.Builder.ReplicaResponseBuilder transportClientWrapperBuilder = TransportClientWrapper.Builder.replicaResponseBuilder();

                ImmutableList<Uri> replicas = ImmutableList.<Uri>builder().add(primary).addAll(secondaries).build();

                for(Uri replica: replicas) {
                    transportClientWrapperBuilder.addReplica(replica, (i, request) -> {
                        if (request.getOperationType() == OperationType.Head || request.getOperationType() == OperationType.HeadFeed) {
                            return headStoreResponse;
                        } else {
                            return readStoreResponse;
                        }
                    });
                }

                AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create().withPrimary(primary)
                        .withSecondary(secondaries).build();

                return new EndpointMock(addressSelectorWrapper, transportClientWrapperBuilder.build()) {};
            }
        }

        class QuorumNotMetSecondaryReplicasDisappear {
            Uri primary;
            Map<Uri, Function2WithCheckedException<Integer, RxDocumentServiceRequest, Boolean>> disappearDictionary = new HashMap<>();
            public QuorumNotMetSecondaryReplicasDisappear primaryReplica(Uri primaryReplica) {
                this.primary = primaryReplica;
                return this;
            }

            public QuorumNotMetSecondaryReplicasDisappear secondaryReplicasDisappearWhen(Uri secondary,
                                                               Function2WithCheckedException<Integer, RxDocumentServiceRequest, Boolean> disappearPredicate) {
                disappearDictionary.put(secondary, disappearPredicate);
                return this;
            }

            public QuorumNotMetSecondaryReplicasDisappear secondaryReplicasDisappearAfter(Uri secondary, int attempt) {
                disappearDictionary.put(secondary, (i, r) -> i >= attempt);
                return this;
            }
        }

        static public class NoSecondaryReplica extends Builder {
            private long LOCAL_LSN = 19;
            private long LSN = 52;
            private Uri defaultPrimaryURI = Uri.create("primary");
            private Uri primary = defaultPrimaryURI;
            private StoreResponse defaultResponse = StoreResponseBuilder.create()
                    .withLSN(LSN)
                    .withLocalLSN(LOCAL_LSN)
                    .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, "1")
                    .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, Long.toString(LSN))
                    .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(LOCAL_LSN))
                    .withRequestCharge(0)
                    .build();

            private StoreResponse headStoreResponse = defaultResponse;
            private StoreResponse readStoreResponse = defaultResponse;
            private Function1WithCheckedException<RxDocumentServiceRequest, StoreResponse> storeResponseFunc;

            public NoSecondaryReplica() throws Exception {
            }

            public NoSecondaryReplica primaryReplica(Uri primaryReplica) {
                this.primary = primaryReplica;
                return this;
            }

            public NoSecondaryReplica response(StoreResponse storeResponse) {
                this.readStoreResponse = storeResponse;
                this.headStoreResponse = storeResponse;
                return this;
            }

            public NoSecondaryReplica response(Function1WithCheckedException<RxDocumentServiceRequest, StoreResponse> storeResponseFunc) {
                this.storeResponseFunc = storeResponseFunc;
                return this;
            }

            public EndpointMock build() {

                TransportClientWrapper.Builder.ReplicaResponseBuilder transportClientWrapperBuilder = TransportClientWrapper.Builder.replicaResponseBuilder();

                ImmutableList<Uri> replicas = ImmutableList.<Uri>builder().add(primary).build();

                for(Uri replica: replicas) {
                    transportClientWrapperBuilder.addReplica(replica, (i, request) -> {

                        if (storeResponseFunc != null) {
                            return storeResponseFunc.apply(request);
                        }

                        if (request.getOperationType() == OperationType.Head || request.getOperationType() == OperationType.HeadFeed) {
                            return headStoreResponse;
                        } else {
                            return readStoreResponse;
                        }
                    });
                }

                AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create().withPrimary(primary)
                        .withSecondary(ImmutableList.of()).build();

                return new EndpointMock(addressSelectorWrapper, transportClientWrapperBuilder.build()) {};
            }
        }

        static public class NoSecondaryReplica_TwoSecondaryReplicasGoLiveAfterFirstHitOnPrimary extends Builder {
            private long LOCAL_LSN = 19;
            private long LSN = 52;
            private Uri primary = Uri.create("primary");
            private ImmutableList<Uri> secondaryReplicas = ImmutableList.of(Uri.create("secondary1"), Uri.create("secondary2"));
            private StoreResponse primaryDefaultResponse = StoreResponseBuilder.create()
                    .withLSN(LSN)
                    .withLocalLSN(LOCAL_LSN)
                    .withHeader(WFConstants.BackendHeaders.CURRENT_REPLICA_SET_SIZE, "3")
                    .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, Long.toString(LSN))
                    .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(LOCAL_LSN))
                    .withRequestCharge(0)
                    .build();

            private StoreResponse secondaryDefaultResponse = StoreResponseBuilder.create()
                    .withLSN(LSN)
                    .withLocalLSN(LOCAL_LSN)
                    .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, Long.toString(LSN))
                    .withHeader(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(LOCAL_LSN))
                    .withRequestCharge(0)
                    .build();
            Map<Uri, Function1WithCheckedException<RxDocumentServiceRequest, StoreResponse>> secondaryResponseFunc =
                    new HashMap<>();

            public NoSecondaryReplica_TwoSecondaryReplicasGoLiveAfterFirstHitOnPrimary() throws Exception {
            }


            public NoSecondaryReplica_TwoSecondaryReplicasGoLiveAfterFirstHitOnPrimary primaryReplica(Uri primaryReplica) {
                this.primary = primaryReplica;
                return this;
            }

            public NoSecondaryReplica_TwoSecondaryReplicasGoLiveAfterFirstHitOnPrimary responseFromSecondary(
                    Uri replica,
                    Function1WithCheckedException<RxDocumentServiceRequest, StoreResponse> func) {
                secondaryResponseFunc.put(replica, func);
                return this;
            }

            public EndpointMock build() {

                TransportClientWrapper.Builder.ReplicaResponseBuilder transportClientWrapperBuilder = TransportClientWrapper.Builder.replicaResponseBuilder();

                transportClientWrapperBuilder.addReplica(primary, (i, request) -> {
                    return primaryDefaultResponse;
                });

                transportClientWrapperBuilder.addReplica(secondaryReplicas.get(0), (i, request) -> {
                    return secondaryDefaultResponse;
                });

                transportClientWrapperBuilder.addReplica(secondaryReplicas.get(1), (i, request) -> {
                    return secondaryDefaultResponse;
                });

                AddressSelectorWrapper addressSelectorWrapper = AddressSelectorWrapper.Builder.Simple.create().withPrimary(primary)
                        .withSecondary(ImmutableList.of()).build();

                return new EndpointMock(addressSelectorWrapper, transportClientWrapperBuilder.build()){};
            }
        }

        public abstract EndpointMock build() ;
    }
}
