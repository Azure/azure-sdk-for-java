package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import io.netty.buffer.ByteBuf;

public interface HttpTransportSerializer {
    HttpRequest wrapInHttpRequest(RxDocumentServiceRequest request, String requestUri, int port) throws Exception;

    StoreResponse unwrapToStoreResponse(
        String endpoint,
        RxDocumentServiceRequest request,
        int statusCode,
        HttpHeaders headers,
        ByteBuf retainedContent);
}
