// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.rest.Response;
import com.azure.core.perf.models.UserDatabase;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Host("{$host}")
@ServiceInterface(name = "MyMockService")
public interface MyRestProxyService {
    @Get("RawData")
    Mono<Response<Flux<ByteBuffer>>> getRawDataAsync(@HostParam("$host") String endpoint);

    @Put("RawData")
    Mono<Void> setRawData(@HostParam("$host") String endpoint,
                          @BodyParam("application/octet-stream") Flux<ByteBuffer> body,
                          @HeaderParam("Content-Length") long length);

    @Get("BinaryData")
    Mono<Response<BinaryData>> getBinaryDataAsync(@HostParam("$host") String endpoint);

    @Put("BinaryData")
    Mono<Void> setBinaryData(@HostParam("$host") String endpoint,
                          @BodyParam("application/octet-stream") BinaryData body,
                          @HeaderParam("Content-Length") long length);

    @Get("UserDatabaseXml")
    Mono<Response<UserDatabase>> getUserDatabaseXmlAsync(@HostParam("$host") String endpoint);

    @Put("UserDatabaseXml")
    Mono<Void> setUserDatabaseXml(@HostParam("$host") String endpoint,
                                  @BodyParam("application/xml") UserDatabase userDatabase);

    @Get("UserDatabaseJson")
    Mono<Response<UserDatabase>> getUserDatabaseJsonAsync(@HostParam("$host") String endpoint);

    @Put("UserDatabaseJson")
    Mono<Void> setUserDatabaseJson(@HostParam("$host") String endpoint,
                                   @BodyParam("application/json") UserDatabase userDatabase);
}
