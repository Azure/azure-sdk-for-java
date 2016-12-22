/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * An instance of this interceptor placed in the request pipeline handles retriable errors.
 */
public final class LogLevel {
    /** The log level that logs only errors. */
    public static final LogLevel NONE = new LogLevel(HttpLoggingInterceptor.Level.NONE);

    /** The log level that logs only warnings and errors. */
    public static final LogLevel BASIC = new LogLevel(HttpLoggingInterceptor.Level.BASIC);

    /** The log level that logs basic HTTP traffic and other basic information. */
    public static final LogLevel HEADERS = new LogLevel(HttpLoggingInterceptor.Level.HEADERS);

    /** The log level that logs all HTTP traffic and debug level information. */
    public static final LogLevel BODY = new LogLevel(HttpLoggingInterceptor.Level.BODY);

    private HttpLoggingInterceptor.Level value;

    private LogLevel(HttpLoggingInterceptor.Level logLevel) {
        this.value = logLevel;
    }

    /**
     * @return raw OkHttp log level
     */
    public HttpLoggingInterceptor.Level raw() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LogLevel)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        LogLevel rhs = (LogLevel) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }

}
