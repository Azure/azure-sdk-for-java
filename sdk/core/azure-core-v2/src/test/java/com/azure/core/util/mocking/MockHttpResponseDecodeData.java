// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import com.azure.core.v2.implementation.http.UnexpectedExceptionInformation;
import com.azure.core.v2.implementation.serializer.HttpResponseDecodeData;

import java.lang.reflect.Type;

/**
 * An HTTP response decode data used for mocking in tests.
 */
public class MockHttpResponseDecodeData implements HttpResponseDecodeData {
    private final Integer expectedResponseStatusCode;
    private final UnexpectedExceptionInformation unexpectedExceptionInformation;
    private final Type returnType;
    private final Type returnValueWireType;
    private final boolean isReturnTypeDecodeable;

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

    public MockHttpResponseDecodeData(int expectedResponseStatusCode, Type returnType, boolean isReturnTypeDecodeable) {
        this(expectedResponseStatusCode, returnType, null, isReturnTypeDecodeable, null);
    }

    public MockHttpResponseDecodeData(int expectedResponseStatusCode, Type returnType, Type returnValueWireType,
        boolean isReturnTypeDecodeable) {
        this(expectedResponseStatusCode, returnType, returnValueWireType, isReturnTypeDecodeable, null);
    }

    private MockHttpResponseDecodeData(Integer expectedResponseStatusCode, Type returnType, Type returnValueWireType,
        boolean isReturnTypeDecodeable, UnexpectedExceptionInformation unexpectedExceptionInformation) {
        this.expectedResponseStatusCode = expectedResponseStatusCode;
        this.returnType = returnType;
        this.returnValueWireType = returnValueWireType;
        this.isReturnTypeDecodeable = isReturnTypeDecodeable;
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
    public boolean isReturnTypeDecodeable() {
        return isReturnTypeDecodeable;
    }

    @Override
    public boolean isResponseEagerlyRead() {
        return HttpResponseDecodeData.super.isResponseEagerlyRead();
    }

    @Override
    public boolean isResponseBodyIgnored() {
        return HttpResponseDecodeData.super.isResponseBodyIgnored();
    }

    @Override
    public boolean isHeadersEagerlyConverted() {
        return false;
    }
}
