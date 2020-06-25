// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.rest.Response;
import com.azure.core.perf.models.UserDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Host("http://unused")
@ServiceInterface(name = "MyMockService")
public interface MyRestProxyService {
    @Get("GetRawDataAsync")
    Mono<Response<Flux<ByteBuffer>>> getRawDataAsync();

    @Get("GetUserDatabaseAsync")
    Mono<Response<UserDatabase>> getUserDatabaseAsync();

    @Put("SetRawData")
    Mono<Void> setRawData(@BodyParam("application/octet-stream") Flux<ByteBuffer> body,
                          @HeaderParam("Content-Length") long length);

    @Put("SetUserDatabase")
    Mono<Void> setUserDatabaseXml(@BodyParam("application/xml") UserDatabase userDatabase);

    @Put("SetUserDatabase")
    Mono<Void> setUserDatabaseJson(@BodyParam("application/json") UserDatabase userDatabase);
}
