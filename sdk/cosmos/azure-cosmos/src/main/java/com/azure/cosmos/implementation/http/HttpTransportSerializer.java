package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import io.netty.buffer.ByteBuf;

import java.net.URI;

public interface HttpTransportSerializer {
    HttpRequest wrapInHttpRequest(RxDocumentServiceRequest request, URI requestUri) throws Exception;

    StoreResponse unwrapToStoreResponse(
        String endpoint,
        RxDocumentServiceRequest request,
        int statusCode,
        HttpHeaders headers,
        ByteBuf retainedContent) throws Exception;
}
