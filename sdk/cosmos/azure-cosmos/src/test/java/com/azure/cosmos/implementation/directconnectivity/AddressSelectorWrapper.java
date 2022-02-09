// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.guava25.base.Predicates;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Condition;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressSelectorWrapper {

    private static String resolveAllUriAsync = "resolveAllUriAsync";
    private static String resolvePrimaryUriAsync = "resolvePrimaryUriAsync";
    private static String resolveAddressesAsync = "resolveAddressesAsync";
    private final List<InvocationOnMock> invocationOnMockList;

    public final AddressSelector addressSelector;

    public static class InOrderVerificationBuilder {
        private List<Function<InOrderVerification, Void>> actions = new ArrayList<>();

        public static InOrderVerificationBuilder create() {
            return new InOrderVerificationBuilder();
        }

        public InOrderVerificationBuilder verify(InOrderVerification.Verifier v, int index) {
            actions.add(verification -> {
                verification.verify(v, index);
                return null;
            });
            return this;
        }

        public InOrderVerificationBuilder verifyOnAll(InOrderVerification.Verifier v) {
            actions.add(verification -> {
                verification.verifyOnAll(v);
                return null;
            });
            return this;
        }

        public InOrderVerificationBuilder verifyNext(InOrderVerification.Verifier v) {
            actions.add(verification -> {
                verification.verifyNext(v);
                return null;
            });
            return this;
        }

        public InOrderVerificationBuilder verifyNumberOfInvocations(int expected) {
            actions.add(verification -> {
                verification.verifyNumberOfInvocations(expected);
                return null;
            });
            return this;
        }

        public void execute(AddressSelectorWrapper addressSelectorWrapper) {
            InOrderVerification v = new InOrderVerification(addressSelectorWrapper.invocationOnMockList);
            for(Function<InOrderVerification, Void> action: actions) {
                action.apply(v);
            }
        }
    }

    public InOrderVerification getInOrderVerification() {
        return new InOrderVerification(invocationOnMockList);
    }

    public static class InOrderVerification  {
        private final List<InvocationOnMock> invocations;
        private int internalIndex = 0;

        InOrderVerification(List<InvocationOnMock> invocationOnMockList) {
            invocations = invocationOnMockList;
        }

        public InOrderVerification verify(Verifier v, int index) {
            v.verify(invocations.get(index));
            return this;
        }

        public InOrderVerification verifyOnAll(Verifier v) {
            for(InvocationOnMock i: invocations) {
                v.verify(i);
            }
            return this;
        }

        public InOrderVerification verifyNext(Verifier v) {
            v.verify(invocations.get(internalIndex++));
            return this;
        }

        public InOrderVerification verifyNumberOfInvocations(int expected) {
            assertThat(invocations).hasSize(expected);
            return this;
        }

        interface Verifier {

            void verify(InvocationOnMock invocation);

            public static VerifierBuilder builder() {
                return new VerifierBuilder();
            }

            public static class VerifierBuilder {

                public Verifier build() {
                    return new Verifier() {
                        @Override
                        public void verify(InvocationOnMock invocation) {
                            for(Verifier v: verifiers) {
                                v.verify(invocation);
                            }
                        }
                    };
                }

                List<Verifier> verifiers = new ArrayList<>();

                VerifierBuilder add(Verifier verifier) {
                    verifiers.add(verifier);
                    return this;
                }

                VerifierBuilder methodName(String methodName) {
                    add(new Verifier() {
                        @Override
                        public void verify(InvocationOnMock invocation) {
                            assertThat(invocation.getMethod().getName()).isEqualTo(methodName);
                        }
                    });
                    return this;
                }

                VerifierBuilder resolveAllUriAsync() {
                    methodName(resolveAllUriAsync);
                    return this;
                }

                VerifierBuilder resolvePrimaryUriAsync() {
                    methodName(resolvePrimaryUriAsync);
                    return this;
                }

                VerifierBuilder resolveAddressesAsync() {
                    methodName(resolveAddressesAsync);
                    return this;
                }

                VerifierBuilder resolveAllUriAsync(Condition<RxDocumentServiceRequest> requestMatcher, Condition<Boolean> includePrimaryMatcher, Condition<Boolean> forceRefreshMatcher) {
                    methodName(resolveAllUriAsync);
                    add(new Verifier() {
                        @Override
                        public void verify(InvocationOnMock invocation) {
                            RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);
                            boolean includePrimary = invocation.getArgument(1, Boolean.class);
                            boolean forceRefresh = invocation.getArgument(2, Boolean.class);

                            assertThat(request).is(requestMatcher);

                            assertThat(includePrimary).is(includePrimaryMatcher);
                            assertThat(forceRefresh).is(forceRefreshMatcher);
                        }
                    });
                    return this;
                }

                VerifierBuilder resolveAllUriAsync_IncludePrimary(boolean primaryIncluded) {
                    methodName(resolveAllUriAsync);

                    Condition<Boolean> alwaysTrue = new Condition<>(Predicates.alwaysTrue(), "no getCondition");
                    Condition<Boolean> primaryIncludedCond = new Condition<>(Predicates.equalTo(primaryIncluded), String.format("%b (primaryIncluded)", primaryIncluded));

                    resolveAllUriAsync(
                        new Condition<>(Predicates.alwaysTrue(), "no condition"),
                        primaryIncludedCond,
                        alwaysTrue);
                    return this;
                }

                VerifierBuilder resolveAllUriAsync_ForceRefresh(boolean forceRefresh) {
                    methodName(resolveAllUriAsync);

                    Condition<Boolean> alwaysTrue = new Condition<>(Predicates.alwaysTrue(), "no condition");
                    Condition<Boolean> forceRefreshCond = new Condition<>(Predicates.equalTo(forceRefresh), String.format("%b (forceRefresh)", forceRefresh));

                    resolveAllUriAsync(
                        new Condition<>(Predicates.alwaysTrue(), "no condition"),
                        alwaysTrue,
                        forceRefreshCond);
                    return this;
                }
            }
        }
    }

    public AddressSelectorWrapper(AddressSelector addressSelector, List<InvocationOnMock> invocationOnMockList) {
        this.addressSelector = addressSelector;
        this.invocationOnMockList = invocationOnMockList;
    }

    public AddressSelectorWrapper verifyNumberOfForceCachRefresh(int expectedNumber) {
        int count = 0;
        for (InvocationOnMock invocationOnMock : invocationOnMockList) {
            boolean forceRefresh;
            if (invocationOnMock.getMethod().getName().endsWith("resolveAllUriAsync")) {
                forceRefresh = invocationOnMock.getArgument(2, Boolean.class);
            } else {
                forceRefresh = invocationOnMock.getArgument(1, Boolean.class);
            }
            if (forceRefresh) {
                count++;
            }
        }
        assertThat(count).isEqualTo(expectedNumber);
        return this;
    }

    public AddressSelectorWrapper verifyNumberOfForceCacheRefreshGreaterThanOrEqualTo(int minimum) {
        int count = 0;
        for (InvocationOnMock invocationOnMock : invocationOnMockList) {
            boolean forceRefresh;
            if (invocationOnMock.getMethod().getName().endsWith("resolveAllUriAsync")) {
                forceRefresh = invocationOnMock.getArgument(2, Boolean.class);
            } else {
                forceRefresh = invocationOnMock.getArgument(1, Boolean.class);
            }
            if (forceRefresh) {
                count++;
            }
        }
        assertThat(count).isGreaterThanOrEqualTo(minimum);
        return this;
    }

    public AddressSelectorWrapper validate() {
        // for now do nothing;
        return this;
    }

    public AddressSelectorWrapper verifyVesolvePrimaryUriAsyncCount(int count) {
        Mockito.verify(addressSelector, Mockito.times(count)).resolvePrimaryUriAsync(Mockito.any(), Mockito.anyBoolean());
        return this;
    }

    public AddressSelectorWrapper verifyResolveAddressesAsync(int count) {
        Mockito.verify(addressSelector, Mockito.times(count)).resolveAddressesAsync(Mockito.any(), Mockito.anyBoolean());
        return this;
    }

    public AddressSelectorWrapper verifyResolveAllUriAsync(int count) {
        Mockito.verify(addressSelector, Mockito.times(count)).resolveAllUriAsync(Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
        return this;
    }

    public AddressSelectorWrapper verifyTotalInvocations(int count) {
        assertThat(invocationOnMockList).hasSize(count);
        return this;
    }

    public static class Builder {
        final Protocol protocol;
        AddressSelector addressSelector;
        List<InvocationOnMock> invocationOnMockList = Collections.synchronizedList(new ArrayList<>());


        public Builder(Protocol protocol) {
            this.protocol = protocol;
        }

        private static AddressInformation toAddressInformation(Uri uri, boolean isPrimary, Protocol protocol) {
            return new AddressInformation(true, isPrimary, uri.getURIAsString(), protocol);
        }

        public static class PrimaryReplicaMoveBuilder extends Builder {
            static PrimaryReplicaMoveBuilder  create(Protocol protocol) {
                return new PrimaryReplicaMoveBuilder(protocol);
            }

            public PrimaryReplicaMoveBuilder(Protocol protocol) {
                super(protocol);
                addressSelector = Mockito.mock(AddressSelector.class);
            }

            public PrimaryReplicaMoveBuilder withPrimaryReplicaMove(Uri primaryURIBeforeForceRefresh, Uri primaryURIAfterForceRefresh) {
                AtomicBoolean refreshed = new AtomicBoolean(false);
                Mockito.doAnswer((invocation) -> {
                    capture(invocation);
                    RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);
                    boolean forceRefresh = invocation.getArgument(1, Boolean.class);

                    if (forceRefresh || refreshed.get()) {
                        refreshed.set(true);
                        return Mono.just(primaryURIAfterForceRefresh);
                    }

                    return Mono.just(primaryURIBeforeForceRefresh);
                }).when(addressSelector).resolvePrimaryUriAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.anyBoolean());

                Mockito.doAnswer((invocation -> {
                    capture(invocation);
                    return null;
                })).when(addressSelector).resolveAllUriAsync(Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());

                Mockito.doAnswer((invocation -> {
                    capture(invocation);
                    return null;
                })).when(addressSelector).resolveAddressesAsync(Mockito.any(), Mockito.anyBoolean());

                return this;
            }

            public AddressSelectorWrapper build() {
                return new AddressSelectorWrapper(this.addressSelector, this.invocationOnMockList);
            }
        }

        public static class ReplicaMoveBuilder extends Builder {

            List<Pair<Uri, Uri>> secondary = new ArrayList<>();
            Pair<Uri, Uri> primary;
            private Function<RxDocumentServiceRequest, PartitionKeyRange> partitionKeyRangeFunction;

            static ReplicaMoveBuilder  create(Protocol protocol) {
                return new ReplicaMoveBuilder(protocol);
            }

            public ReplicaMoveBuilder(Protocol protocol) {
                super(protocol);
                addressSelector = Mockito.mock(AddressSelector.class);
            }

            public ReplicaMoveBuilder withPrimaryMove(Uri uriBeforeForceRefresh, Uri uriAfterForceRefresh) {
                withReplicaMove(uriBeforeForceRefresh, uriAfterForceRefresh, true);
                return this;
            }

            public ReplicaMoveBuilder withSecondaryMove(Uri uriBeforeForceRefresh, Uri uriAfterForceRefresh) {
                withReplicaMove(uriBeforeForceRefresh, uriAfterForceRefresh, false);
                return this;
            }

            public ReplicaMoveBuilder newPartitionKeyRangeIdOnRefresh(Function<RxDocumentServiceRequest, PartitionKeyRange> partitionKeyRangeFunction) {
                this.partitionKeyRangeFunction = partitionKeyRangeFunction;
                return this;
            }

            public ReplicaMoveBuilder withReplicaMove(Uri uriBeforeForceRefresh, Uri uriAfterForceRefresh, boolean isPrimary) {
                if (isPrimary) {
                    primary = ImmutablePair.of(uriBeforeForceRefresh, uriAfterForceRefresh);
                } else {
                    secondary.add(ImmutablePair.of(uriBeforeForceRefresh, uriAfterForceRefresh));
                }
                return this;
            }


            public AddressSelectorWrapper build() {
                AtomicBoolean refreshed = new AtomicBoolean(false);
                Mockito.doAnswer((invocation) -> {
                    capture(invocation);
                    RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);
                    boolean forceRefresh = invocation.getArgument(1, Boolean.class);
                    if (partitionKeyRangeFunction != null) {
                        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeFunction.apply(request);
                    }
                    if (forceRefresh || refreshed.get()) {
                        refreshed.set(true);
                        return Mono.just(primary.getRight());
                    } else {
                        return Mono.just(primary.getLeft());
                    }

                }).when(addressSelector).resolvePrimaryUriAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.anyBoolean());

                Mockito.doAnswer((invocation -> {
                    capture(invocation);
                    RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);
                    boolean includePrimary = invocation.getArgument(1, Boolean.class);
                    boolean forceRefresh = invocation.getArgument(2, Boolean.class);

                    ImmutableList.Builder<Uri> b = ImmutableList.builder();

                    if (forceRefresh || refreshed.get()) {
                        if (partitionKeyRangeFunction != null) {
                            request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeFunction.apply(request);
                        }
                        refreshed.set(true);
                        if (includePrimary) {
                            b.add(primary.getRight());
                        }
                        b.addAll(secondary.stream().map(s -> s.getRight()).collect(Collectors.toList()));
                        return Mono.just(b.build());
                    } else {
                        // old
                        if (includePrimary) {
                            b.add(primary.getLeft());
                        }
                        b.addAll(secondary.stream().map(s -> s.getLeft()).collect(Collectors.toList()));
                        return Mono.just(b.build());
                    }

                })).when(addressSelector).resolveAllUriAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.anyBoolean(), Mockito.anyBoolean());

                Mockito.doAnswer((invocation -> {
                    capture(invocation);
                    RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);
                    boolean forceRefresh = invocation.getArgument(1, Boolean.class);

                    ImmutableList.Builder<AddressInformation> b = ImmutableList.builder();

                    if (forceRefresh || refreshed.get()) {
                        if (partitionKeyRangeFunction != null) {
                            request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeFunction.apply(request);
                        }

                        refreshed.set(true);
                        b.add(toAddressInformation(primary.getRight(), true, protocol));
                        b.addAll(
                            secondary.stream()
                                     .map(
                                        s -> toAddressInformation(s.getRight(), false, protocol))
                                     .collect(Collectors.toList()));
                        return Mono.just(b.build());
                    } else {
                        // old
                        b.add(toAddressInformation(primary.getLeft(), true, protocol));
                        b.addAll(secondary.stream()
                                          .map(
                                              s -> toAddressInformation(s.getLeft(), false, protocol))
                                          .collect(Collectors.toList()));
                        return Mono.just(b.build());
                    }
                })).when(addressSelector).resolveAddressesAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.anyBoolean());

                return new AddressSelectorWrapper(addressSelector, invocationOnMockList);
            }
        }

        public static class Simple extends Builder {
            private Uri primaryAddress;
            private List<Uri> secondaryAddresses;
            static Simple  create() {
                return new Simple(Protocol.HTTPS);
            }

            public Simple(Protocol protocol) {
                super(protocol);
                addressSelector = Mockito.mock(AddressSelector.class);
            }

            public Simple withPrimary(Uri primaryAddress) {
                this.primaryAddress = primaryAddress;
                return this;
            }

            public Simple withSecondary(List<Uri> secondaryAddresses) {
                this.secondaryAddresses = secondaryAddresses;
                return this;
            }

            public AddressSelectorWrapper build() {
                Mockito.doAnswer((invocation) -> {
                    capture(invocation);
                    return Mono.just(primaryAddress);
                }).when(addressSelector).resolvePrimaryUriAsync(Mockito.any(RxDocumentServiceRequest.class), Mockito.anyBoolean());

                Mockito.doAnswer((invocation -> {
                    capture(invocation);
                    RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);
                    boolean includePrimary = invocation.getArgument(1, Boolean.class);
                    boolean forceRefresh = invocation.getArgument(2, Boolean.class);

                    if (includePrimary) {
                        return Mono.just(ImmutableList.builder().addAll(secondaryAddresses).add(primaryAddress).build());
                    } else {
                        return Mono.just(secondaryAddresses);
                    }
                })).when(addressSelector).resolveAllUriAsync(Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());

                Mockito.doAnswer((invocation -> {
                    capture(invocation);
                    return Mono.just(ImmutableList.builder()
                                               .addAll(secondaryAddresses.stream()
                                                               .map(uri -> toAddressInformation(uri, false, protocol))
                                                               .collect(Collectors.toList()))
                                               .add(toAddressInformation(primaryAddress, true, protocol))
                                               .build());
                })).when(addressSelector).resolveAddressesAsync(Mockito.any(), Mockito.anyBoolean());


                return new AddressSelectorWrapper(this.addressSelector, this.invocationOnMockList);
            }
        }

        protected void capture(InvocationOnMock invocationOnMock) {
            invocationOnMockList.add(invocationOnMock);
        }
    }
}
