// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.APISpec;
import com.azure.storage.blob.HttpGetterInfo;
import com.azure.storage.blob.implementation.models.BlobsDownloadResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DownloadResponseMockFlux extends Flux<ByteBuffer> {
    static final int DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK = 0;
    static final int DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK = 1;
    static final int DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES = 2;
    static final int DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED = 3;
    static final int DR_TEST_SCENARIO_NON_RETRYABLE_ERROR = 4;
    static final int DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE = 6;
    static final int DR_TEST_SCENARIO_INFO_TEST = 8;

    private int scenario;
    private int tryNumber;
    private HttpGetterInfo info;
    private ByteBuffer scenarioData;
    private DownloadRetryOptions options;

    DownloadResponseMockFlux(int scenario, APISpec apiSpec) {
        this.scenario = scenario;

        switch (this.scenario) {
            case DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                this.scenarioData = apiSpec.getRandomData(512 * 1024);
                break;
            case DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
            case DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES:
                this.scenarioData = apiSpec.getRandomData(1024);
                break;
            case DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED:
            case DR_TEST_SCENARIO_NON_RETRYABLE_ERROR:
            case DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE:
            case DR_TEST_SCENARIO_INFO_TEST:
                break;
            default:
                throw new IllegalArgumentException("Invalid download resource test scenario.");
        }
    }

    ByteBuffer getScenarioData() {
        return this.scenarioData;
    }

    int getTryNumber() {
        return this.tryNumber;
    }

    DownloadResponseMockFlux setOptions(DownloadRetryOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuffer> subscriber) {
        switch (this.scenario) {
            case DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                subscriber.onNext(this.scenarioData.duplicate());
                Operators.complete(subscriber);
                break;

            case DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
                for (int i = 0; i < 4; i++) {
                    ByteBuffer toSend = this.scenarioData.duplicate();
                    toSend.position(i * 256);
                    toSend.limit((i + 1) * 256);
                    subscriber.onNext(toSend);
                }
                Operators.complete(subscriber);
                break;

            case DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES:
                if (this.tryNumber <= 3) {
                    // tryNumber is 1 indexed, so we have to sub 1.
                    if (this.info.getOffset() != (this.tryNumber - 1) * 256
                        || this.info.getCount() != this.scenarioData.remaining() - (this.tryNumber - 1) * 256) {
                        Operators.error(subscriber, new IllegalArgumentException("Info values are incorrect."));
                        return;
                    }
                    ByteBuffer toSend = this.scenarioData.duplicate();
                    toSend.position((this.tryNumber - 1) * 256);
                    toSend.limit(this.tryNumber * 256);
                    subscriber.onNext(toSend);
                    Operators.error(subscriber, new IOException());
                    break;
                }
                if (this.info.getOffset() != (this.tryNumber - 1) * 256
                    || this.info.getCount() != this.scenarioData.remaining() - (this.tryNumber - 1) * 256) {
                    Operators.error(subscriber, new IllegalArgumentException("Info values are incorrect."));
                    return;
                }
                ByteBuffer toSend = this.scenarioData.duplicate();
                toSend.position((this.tryNumber - 1) * 256);
                toSend.limit(this.tryNumber * 256);
                subscriber.onNext(toSend);
                Operators.complete(subscriber);
                break;

            case DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED:
                Operators.error(subscriber, new IOException());
                break;

            case DR_TEST_SCENARIO_NON_RETRYABLE_ERROR:
                Operators.error(subscriber, new Exception());
                break;

            case DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE:
                if (this.tryNumber == 1) {
                    /*
                     We return a retryable error here so we have to invoke the getter, which will throw an error in
                     this case.
                     */
                    Operators.error(subscriber, new IOException());
                } else {
                    Operators.error(subscriber, new IllegalArgumentException("Retried after getter error."));
                }
                break;

            case DR_TEST_SCENARIO_INFO_TEST:
                switch (this.tryNumber) {
                    case 1:  // Test the value of info when getting the initial response.
                    case 2:  // Test the value of info when getting an intermediate response.
                        Operators.error(subscriber, new IOException());
                        break;
                    case 3:
                        // All calls to getter checked. Exit. This test does not check for data.
                        Operators.complete(subscriber);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid try number.");
                }
                break;

            default:
                Operators.error(subscriber, new IllegalArgumentException("Invalid test case"));
        }
    }

    Mono<ReliableDownload> getter(HttpGetterInfo info) {
        this.tryNumber++;
        this.info = info;
        BlobsDownloadResponse rawResponse = new BlobsDownloadResponse(null, 200, new HttpHeaders(), this, new BlobDownloadHeaders());
        ReliableDownload response = new ReliableDownload(rawResponse, options, info, this::getter);

        switch (this.scenario) {
            case DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE:
                switch (this.tryNumber) {
                    case 1:
                        return Mono.just(response);
                    case 2:
                        /*
                         This validates that we don't retry in the getter even if it's a retryable error from the
                         service.
                         */
                        throw new BlobStorageException("Message", new HttpResponse(null) {
                            @Override
                            public int getStatusCode() {
                                return 500;
                            }

                            @Override
                            public String getHeaderValue(String s) {
                                return null;
                            }

                            @Override
                            public HttpHeaders getHeaders() {
                                return null;
                            }

                            @Override
                            public Flux<ByteBuffer> getBody() {
                                return null;
                            }

                            @Override
                            public Mono<byte[]> getBodyAsByteArray() {
                                return null;
                            }

                            @Override
                            public Mono<String> getBodyAsString() {
                                return null;
                            }

                            @Override
                            public Mono<String> getBodyAsString(Charset charset) {
                                return null;
                            }
                        }, null);
                    default:
                        throw new IllegalArgumentException("Retried after error in getter");
                }
            case DR_TEST_SCENARIO_INFO_TEST:
                // We also test that the info is updated in DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES.
                if (info.getCount() != 10 || info.getOffset() != 20 || !info.getETag().equals("etag")) {
                    throw new IllegalArgumentException("Info values incorrect");
                }
                return Mono.just(response);
            default:
                return Mono.just(response);
        }
    }
}
