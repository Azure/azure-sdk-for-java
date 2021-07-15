package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;

public class Utilities {

    static <T, R> ResponseBase<Void, T> generateResponseFromModelType(Response<R> response, T value) {
        return new ResponseBase<Void, T>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            value,
            null);
    }
}
