// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobsDownloadResponse;
import com.azure.storage.blob.models.StorageErrorException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DownloadResponseMockFlux extends Flux<ByteBuf> {
    static final int DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK = 0;
    static final int DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK = 1;
    static final int DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES = 2;
    static final int DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED = 3;
    static final int DR_TEST_SCENARIO_NON_RETRYABLE_ERROR = 4;
    static final int DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE = 6;
    static final int DR_TEST_SCENARIO_INFO_TEST = 8;

    private int scenario;
    private int tryNumber;
    private HTTPGetterInfo info;
    private ByteBuffer scenarioData;

    public DownloadResponseMockFlux(int scenario) {
        this.scenario = scenario;
        switch (this.scenario) {
            case DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                this.scenarioData = APISpec.getRandomData(512 * 1024);
                break;
            case DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
                // Fall through
            case DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES:
                this.scenarioData = APISpec.getRandomData(1024);
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

    public ByteBuffer getScenarioData() {
        return this.scenarioData;
    }

    public int getTryNumber() {
        return this.tryNumber;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuf> subscriber) {
        switch (this.scenario) {
            case DR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                subscriber.onNext(Unpooled.wrappedBuffer(this.scenarioData.duplicate()));
                subscriber.onComplete();
                break;

            case DR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
                for (int i = 0; i < 4; i++) {
                    ByteBuffer toSend = this.scenarioData.duplicate();
                    toSend.position(i * 256);
                    toSend.limit((i + 1) * 256);
                    subscriber.onNext(Unpooled.wrappedBuffer(toSend));
                }
                subscriber.onComplete();
                break;

            case DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES:
                if (this.tryNumber <= 3) {
                    // tryNumber is 1 indexed, so we have to sub 1.
                    if (this.info.offset() != (this.tryNumber - 1) * 256
                        || this.info.count() != this.scenarioData.remaining() - (this.tryNumber - 1) * 256) {
                        subscriber.onError(new IllegalArgumentException("Info values are incorrect."));
                        return;
                    }
                    ByteBuffer toSend = this.scenarioData.duplicate();
                    toSend.position((this.tryNumber - 1) * 256);
                    toSend.limit(this.tryNumber * 256);
                    subscriber.onNext(Unpooled.wrappedBuffer(toSend));
                    subscriber.onError(new IOException());
                    break;
                }
                if (this.info.offset() != (this.tryNumber - 1) * 256
                    || this.info.count() != this.scenarioData.remaining() - (this.tryNumber - 1) * 256) {
                    subscriber.onError(new IllegalArgumentException("Info values are incorrect."));
                    return;
                }
                ByteBuffer toSend = this.scenarioData.duplicate();
                toSend.position((this.tryNumber - 1) * 256);
                toSend.limit(this.tryNumber * 256);
                subscriber.onNext(Unpooled.wrappedBuffer(toSend));
                subscriber.onComplete();
                break;

            case DR_TEST_SCENARIO_MAX_RETRIES_EXCEEDED:
                subscriber.onError(new IOException());
                break;

            case DR_TEST_SCENARIO_NON_RETRYABLE_ERROR:
                subscriber.onError(new Exception());
                break;

            case DR_TEST_SCENARIO_ERROR_GETTER_MIDDLE:
                if (this.tryNumber == 1) {
                    /*
                     We return a retryable error here so we have to invoke the getter, which will throw an error in
                     this case.
                     */
                    subscriber.onError(new IOException());
                } else {
                    subscriber.onError(new IllegalArgumentException("Retried after getter error."));
                }
                break;

            case DR_TEST_SCENARIO_INFO_TEST:
                switch (this.tryNumber) {
                    case 1:  // Test the value of info when getting the initial response.
                    case 2:  // Test the value of info when getting an intermediate response.
                        subscriber.onError(new IOException());
                        break;
                    case 3:
                        // All calls to getter checked. Exit. This test does not check for data.
                        subscriber.onComplete();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid try number.");
                }
                break;

            default:
                subscriber.onError(new IllegalArgumentException("Invalid test case"));
        }
    }

    public Mono<DownloadResponse> getter(HTTPGetterInfo info) {
        this.tryNumber++;
        this.info = info;
        BlobsDownloadResponse rawResponse = new BlobsDownloadResponse(null, 200, new HttpHeaders(), this, new BlobDownloadHeaders());
        DownloadResponse response = new DownloadResponse(rawResponse, info, this::getter);

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
                        throw new StorageErrorException("Message", new HttpResponse() {
                            @Override
                            public int statusCode() {
                                return 500;
                            }

                            @Override
                            public String headerValue(String s) {
                                return null;
                            }

                            @Override
                            public HttpHeaders headers() {
                                return null;
                            }

                            @Override
                            public Flux<ByteBuf> body() {
                                return null;
                            }

                            @Override
                            public Mono<byte[]> bodyAsByteArray() {
                                return null;
                            }

                            @Override
                            public Mono<String> bodyAsString() {
                                return null;
                            }

                            @Override
                            public Mono<String> bodyAsString(Charset charset) {
                                return null;
                            }
                        });
                    default:
                        throw new IllegalArgumentException("Retried after error in getter");
                }
            case DR_TEST_SCENARIO_INFO_TEST:
                // We also test that the info is updated in DR_TEST_SCENARIO_SUCCESSFUL_STREAM_FAILURES.
                if (info.count() != 10 || info.offset() != 20 || !info.eTag().equals("etag")) {
                    throw new IllegalArgumentException("Info values incorrect");
                }
                return Mono.just(response);
            default:
                return Mono.just(response);
        }
    }
}
