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
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

public class CosmosTrigger extends CosmosResource{

    private CosmosContainer container;

    CosmosTrigger(String id, CosmosContainer container) {
        super(id);
        this.container = container;
    }

    @Override
    protected String URIPathSegment() {
        return Paths.TRIGGERS_PATH_SEGMENT;
    }

    @Override
    protected String parentLink() {
        return container.getLink();
    }

    /**
     * Reads a cosmos trigger by the trigger link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the read trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @param options     the request options.
     * @return an {@link Mono} containing the single resource response for the read cosmos trigger or an error.
     */
    public Mono<CosmosTriggerResponse> read(RequestOptions options){
        return RxJava2Adapter.singleToMono(
                RxJavaInterop.toV2Single(container.getDatabase()
                                                 .getDocClientWrapper()
                                                 .readTrigger(getLink(), options)
                                                 .map(response -> new CosmosTriggerResponse(response, container))
                                                 .toSingle()));
    }


    /**
     * Replaces a cosmos trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @param triggerSettings the cosmos trigger settings.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the replaced cosmos trigger or an error.
     */
    public Mono<CosmosTriggerResponse> replace(CosmosTriggerSettings triggerSettings, RequestOptions options){
        return RxJava2Adapter.singleToMono(
                RxJavaInterop.toV2Single(container.getDatabase()
                                                 .getDocClientWrapper()
                                                 .replaceTrigger(new Trigger(triggerSettings.toJson()), options)
                                                 .map(response -> new CosmosTriggerResponse(response, container))
                                                 .toSingle()));
    }

    /**
     * Deletes a cosmos trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @param options     the request options.
     * @return an {@link Mono} containing the single resource response for the deleted cosmos trigger or an error.
     */
    public Mono<CosmosResponse> delete(CosmosRequestOptions options){
        return RxJava2Adapter.singleToMono(
                RxJavaInterop.toV2Single(container.getDatabase()
                                                 .getDocClientWrapper()
                                                 .deleteTrigger(getLink(), options.toRequestOptions())
                                                 .map(response -> new CosmosResponse(response.getResource()))
                                                 .toSingle()));
    }

}
