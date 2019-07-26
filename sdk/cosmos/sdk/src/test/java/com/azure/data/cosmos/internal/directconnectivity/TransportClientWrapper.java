// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.*;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class TransportClientWrapper {
    private static Logger logger = LoggerFactory.getLogger(TransportClientWrapper.class);
    public final TransportClient transportClient;
    private final AtomicBoolean valid;
    private final AtomicInteger cnt;
    private final List<Pair<URI, RxDocumentServiceRequest>> requests;

    TransportClientWrapper(TransportClient transportClient, AtomicInteger cnt, AtomicBoolean valid, List<Pair<URI, RxDocumentServiceRequest>> requests) {
        this.transportClient = transportClient;
        this.valid = valid;
        this.cnt = cnt;
        this.requests = requests;
    }

    public static class TransportClientWrapperVerificationBuilder {
        private List<Function<TransportClientWrapper, Void>> actions = new ArrayList<>();

        public static TransportClientWrapperVerificationBuilder create() {
            return new TransportClientWrapperVerificationBuilder();
        }

        public TransportClientWrapperVerificationBuilder verifyNumberOfInvocations(int count) {
            actions.add(transportClientWrapper -> {
                assertThat(transportClientWrapper.getNumberOfInvocations()).isEqualTo(count);
                return null;
            });
            return this;
        }

        public void execute(TransportClientWrapper transportClientWrapper) {
                for(Function<TransportClientWrapper, Void> action: actions) {
                    action.apply(transportClientWrapper);
                }
        }
    }

    public TransportClientWrapper verifyNumberOfInvocations(int count) {
        assertThat(cnt.get()).isEqualTo(count);
        return this;
    }

    public List<Pair<URI, RxDocumentServiceRequest>> getCapturedArgs() {
        return requests;
    }

    public int getNumberOfInvocations() {
        return cnt.get();
    }

    public TransportClientWrapper validate() {
        assertThat(valid).isTrue();
        return this;
    }

    public interface Builder {

         static void capture(List<Pair<URI, RxDocumentServiceRequest>> capturedRequests, InvocationOnMock invocation) {
             URI physicalUri = invocation.getArgumentAt(0, URI.class);
             RxDocumentServiceRequest request = invocation.getArgumentAt(1, RxDocumentServiceRequest.class);
             logger.debug("URI: {}, request {}", physicalUri, request);
             capturedRequests.add(Pair.of(physicalUri, request));
        }

        TransportClientWrapper build();

        public static ReplicaResponseBuilder replicaResponseBuilder() {
            return new ReplicaResponseBuilder();
        }

        class ReplicaResponseBuilder implements Builder {
            Map<URI, Function2WithCheckedException> responseFunctionDictionary = new HashMap<>();

            public ReplicaResponseBuilder addReplica(URI replicaURI,
                                                     Function2WithCheckedException<Integer, RxDocumentServiceRequest, StoreResponse> invocationNumberToStoreResponse) {

                responseFunctionDictionary.put(replicaURI, invocationNumberToStoreResponse);
                return this;
            }

            public TransportClientWrapper build() {

                Map<URI, AtomicInteger> replicaResponseCounterDict = new HashMap<>();

                AtomicInteger i = new AtomicInteger(0);
                AtomicBoolean valid = new AtomicBoolean(true);
                List<Pair<URI, RxDocumentServiceRequest>> capturedArgs = Collections.synchronizedList(new ArrayList<>());

                TransportClient transportClient = Mockito.mock(TransportClient.class);
                Mockito.doAnswer(invocation ->  {
                    i.incrementAndGet();
                    URI physicalUri = invocation.getArgumentAt(0, URI.class);
                    RxDocumentServiceRequest request = invocation.getArgumentAt(1, RxDocumentServiceRequest.class);
                    Function2WithCheckedException function = responseFunctionDictionary.get(physicalUri);
                    if (function == null) {
                        valid.set(false);
                        return Mono.error(new IllegalStateException("no registered function for replica " + physicalUri));
                    }
                    int current;
                    synchronized (transportClient) {
                        capture(capturedArgs, invocation);

                        AtomicInteger cnt = replicaResponseCounterDict.get(physicalUri);
                        if (cnt == null) {
                            cnt = new AtomicInteger(0);
                            replicaResponseCounterDict.put(physicalUri, cnt);
                        }

                        current = cnt.getAndIncrement();
                    }

                    try {
                        return Mono.just(function.apply(current, request));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }

                }).when(transportClient).invokeResourceOperationAsync(Mockito.any(URI.class), Mockito.any(RxDocumentServiceRequest.class));

                return new TransportClientWrapper(transportClient, i, valid, capturedArgs);
            }
        }


        static SequentialBuilder sequentialBuilder() {
            return new SequentialBuilder();
        }

        class SequentialBuilder implements Builder {
            private List<Object> list = new ArrayList<>();

            public SequentialBuilder then(StoreResponse response) {
                list.add(response);
                return this;
            }

            public SequentialBuilder then(Exception exception) {
                list.add(exception);
                return this;
            }

            public TransportClientWrapper build() {
                AtomicInteger i = new AtomicInteger(0);
                AtomicBoolean valid = new AtomicBoolean(true);
                List<Pair<URI, RxDocumentServiceRequest>> capturedArgs = Collections.synchronizedList(new ArrayList<>());

                TransportClient transportClient = Mockito.mock(TransportClient.class);
                Mockito.doAnswer(invocation ->  {
                    capture(capturedArgs, invocation);

                    int current = i.getAndIncrement();
                    if (current >= list.size()) {
                        valid.set(false);
                        return Mono.error(new IllegalStateException());
                    }
                    Object obj = list.get(current);
                    StoreResponse response = Utils.as(obj, StoreResponse.class);
                    if (response != null) {
                        return Mono.just(response);
                    } else {
                        return Mono.error((Exception) obj);
                    }

                }).when(transportClient).invokeResourceOperationAsync(Mockito.any(URI.class), Mockito.any(RxDocumentServiceRequest.class));

                return new TransportClientWrapper(transportClient, i, valid, capturedArgs);
            }
        }

        static UriToResultBuilder uriToResultBuilder() {
            return new UriToResultBuilder();
        }

        class UriToResultBuilder implements Builder {
            private static class Result {
                StoreResponse storeResponse;
                Exception exception;
                boolean stickyResult;

                public Result(StoreResponse storeResponse, Exception exception, boolean stickyResult) {
                    this.storeResponse = storeResponse;
                    this.exception = exception;
                    this.stickyResult = stickyResult;
                }
            }

            private static class Tuple {
                URI replicaURI;
                OperationType operationType;
                ResourceType resourceType;

                public Tuple(URI replicaURI, OperationType operationType, ResourceType resourceType) {
                    this.replicaURI = replicaURI;
                    this.operationType = operationType;
                    this.resourceType = resourceType;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    Tuple tuple = (Tuple) o;
                    return Objects.equals(replicaURI, tuple.replicaURI) &&
                            operationType == tuple.operationType &&
                            resourceType == tuple.resourceType;
                }

                @Override
                public int hashCode() {
                    return Objects.hash(replicaURI, operationType, resourceType);
                }

                @Override
                public String toString() {
                    return "Tuple{" +
                            "replicaURI=" + replicaURI +
                            ", operationType=" + operationType +
                            ", resourceType=" + resourceType +
                            '}';
                }
            }
            private Map<Tuple, List<Result>> uriToResult = new HashMap<>();


            private UriToResultBuilder resultOn(URI replicaURI, OperationType operationType, ResourceType resourceType, StoreResponse rsp, Exception ex, boolean stickyResult) {
                Tuple key = new Tuple(replicaURI, operationType, resourceType);
                List<Result> list = uriToResult.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    uriToResult.put(key, list);
                }
                list.add(new Result(rsp, ex, stickyResult));
                return this;
            }

            public UriToResultBuilder storeResponseOn(URI replicaURI, OperationType operationType, ResourceType resourceType, StoreResponse response, boolean stickyResult) {
                resultOn(replicaURI, operationType, resourceType, response, null, stickyResult);
                return this;
            }

            public UriToResultBuilder exceptionOn(URI replicaURI, OperationType operationType, ResourceType resourceType, Exception exception, boolean stickyResult) {
                resultOn(replicaURI, operationType, resourceType, null, exception, stickyResult);
                return this;
            }

            public TransportClientWrapper build() {
                AtomicBoolean valid = new AtomicBoolean(true);
                AtomicInteger cnt = new AtomicInteger(0);
                List<Pair<URI, RxDocumentServiceRequest>> capturedArgs = Collections.synchronizedList(new ArrayList<>());
                TransportClient transportClient = Mockito.mock(TransportClient.class);
                Mockito.doAnswer(invocation ->  {
                    cnt.getAndIncrement();
                    URI physicalUri = invocation.getArgumentAt(0, URI.class);
                    RxDocumentServiceRequest request = invocation.getArgumentAt(1, RxDocumentServiceRequest.class);
                    capture(capturedArgs, invocation);

                    Tuple tuple = new Tuple(physicalUri, request.getOperationType(), request.getResourceType());
                    List<Result> list = uriToResult.get(tuple);
                    if (list == null || list.isEmpty()) {
                        // unknown
                        valid.set(false);
                        return Mono.error(new IllegalStateException(tuple.toString()));
                    }

                    Result result = list.get(0);

                    if (!result.stickyResult) {
                        list.remove(0);
                    }
                    if (result.storeResponse != null) {
                        return Mono.just(result.storeResponse);
                    } else {
                        return Mono.error(result.exception);
                    }

                }).when(transportClient).invokeResourceOperationAsync(Mockito.any(URI.class), Mockito.any(RxDocumentServiceRequest.class));

                return new TransportClientWrapper(transportClient, cnt, valid, capturedArgs);
            }
        }
    }
}
