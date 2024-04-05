// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http;

import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;

import java.lang.reflect.Type;

/**
 * An HTTP response decode data used for mocking in tests.
 */
public class MockHttpResponseDecodeData implements HttpResponseDecodeData {
    private final Integer expectedResponseStatusCode;
    private final UnexpectedExceptionInformation unexpectedExceptionInformation;
    private final Type returnType;
    private final Type returnValueWireType;
    private final boolean isReturnTypeDecodable;

    public MockHttpResponseDecodeData(UnexpectedExceptionInformation unexpectedExceptionInformation) {
        this(null, null, null, false, unexpectedExceptionInformation);
    }

    public MockHttpResponseDecodeData(int expectedResponseStatusCode) {
        this(expectedResponseStatusCode, null, null, false, null);
    }

    public MockHttpResponseDecodeData(int expectedResponseStatusCode,
                                      UnexpectedExceptionInformation unexpectedExceptionInformation) {
        this(expectedResponseStatusCode, null, null, false, unexpectedExceptionInformation);
    }

    public MockHttpResponseDecodeData(int expectedResponseStatusCode, Type returnType, boolean isReturnTypeDecodable) {
        this(expectedResponseStatusCode, returnType, null, isReturnTypeDecodable, null);
    }

    public MockHttpResponseDecodeData(int expectedResponseStatusCode, Type returnType, Type returnValueWireType,
                                      boolean isReturnTypeDecodable) {
        this(expectedResponseStatusCode, returnType, returnValueWireType, isReturnTypeDecodable, null);
    }

    private MockHttpResponseDecodeData(Integer expectedResponseStatusCode, Type returnType, Type returnValueWireType,
                                       boolean isReturnTypeDecodable,
                                       UnexpectedExceptionInformation unexpectedExceptionInformation) {
        this.expectedResponseStatusCode = expectedResponseStatusCode;
        this.returnType = returnType;
        this.returnValueWireType = returnValueWireType;
        this.isReturnTypeDecodable = isReturnTypeDecodable;
        this.unexpectedExceptionInformation = unexpectedExceptionInformation;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Type getHeadersType() {
        return HttpResponseDecodeData.super.getHeadersType();
    }

    @Override
    public boolean isExpectedResponseStatusCode(int statusCode) {
        return expectedResponseStatusCode != null && expectedResponseStatusCode == statusCode;
    }

    @Override
    public Type getReturnValueWireType() {
        return returnValueWireType;
    }

    @Override
    public UnexpectedExceptionInformation getUnexpectedException(int code) {
        return unexpectedExceptionInformation;
    }

    @Override
    public boolean isReturnTypeDecodable() {
        return isReturnTypeDecodable;
    }

    @Override
    public boolean isHeadersEagerlyConverted() {
        return false;
    }
}
