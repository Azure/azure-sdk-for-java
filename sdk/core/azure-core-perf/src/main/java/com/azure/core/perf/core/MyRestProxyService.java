// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
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
    @Get("RawData/{id}")
    Mono<Response<Flux<ByteBuffer>>> getRawDataAsync(@HostParam("$host") String endpoint, @PathParam("id") String id);

    @Put("RawData/{id}")
    Mono<Void> setRawData(@HostParam("$host") String endpoint, @PathParam("id") String id,
        @BodyParam("application/octet-stream") Flux<ByteBuffer> body, @HeaderParam("Content-Length") long length);

    @Get("BinaryData/{id}")
    Mono<Response<BinaryData>> getBinaryDataAsync(@HostParam("$host") String endpoint, @PathParam("id") String id);

    @Put("BinaryData/{id}")
    Mono<Void> setBinaryData(@HostParam("$host") String endpoint, @PathParam("id") String id,
        @BodyParam("application/octet-stream") BinaryData body, @HeaderParam("Content-Length") long length);

    @Get("UserDatabaseXml/{id}")
    Mono<Response<UserDatabase>> getUserDatabaseXmlAsync(@HostParam("$host") String endpoint,
        @PathParam("id") String id);

    @Put("UserDatabaseXml/{id}")
    Mono<Void> setUserDatabaseXml(@HostParam("$host") String endpoint, @PathParam("id") String id,
        @BodyParam("application/xml") UserDatabase userDatabase);

    @Get("UserDatabaseJson/{id}")
    Mono<Response<UserDatabase>> getUserDatabaseJsonAsync(@HostParam("$host") String endpoint,
        @PathParam("id") String id);

    @Put("UserDatabaseJson/{id}")
    Mono<Void> setUserDatabaseJson(@HostParam("$host") String endpoint, @PathParam("id") String id,
        @BodyParam("application/json") UserDatabase userDatabase);
}
