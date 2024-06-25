// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class StronglyTypedHeadersBenchmark {
    private static final JacksonAdapter MAPPER = new JacksonAdapter();

    private static final HttpHeaders HEADERS
        = new HttpHeaders().set(HttpHeaderName.DATE, "Tue, 04 May 2021 23:22:58 GMT")
            .set(HttpHeaderName.CONTENT_LENGTH, "12345")
            .set(HttpHeaderName.CONTENT_TYPE, "application/json")
            .set(HttpHeaderName.LAST_MODIFIED, "Tue, 04 May 2021 23:22:58 GMT")
            .set(HttpHeaderName.ETAG, "0x8D90F538E5DF4FD");

    @Benchmark
    public void jacksonDatabindConvertSomeProperties(Blackhole blackhole) throws IOException {
        Databind databind = MAPPER.deserialize(HEADERS, Databind.class);

        blackhole.consume(databind.getLastModified());
        blackhole.consume(databind.getContentLength());
    }

    @Benchmark
    public void jacksonDatabindConvertAllProperties(Blackhole blackhole) throws IOException {
        Databind databind = MAPPER.deserialize(HEADERS, Databind.class);

        blackhole.consume(databind.getDate());
        blackhole.consume(databind.getContentLength());
        blackhole.consume(databind.getContentType());
        blackhole.consume(databind.getLastModified());
        blackhole.consume(databind.getETag());
    }

    @Benchmark
    public void deferredDeserializationSomeProperties(Blackhole blackhole) {
        Deferred deferred = new Deferred(HEADERS);

        blackhole.consume(deferred.getLastModified());
        blackhole.consume(deferred.getContentLength());
    }

    @Benchmark
    public void deferredDeserializationAllProperties(Blackhole blackhole) {
        Deferred deferred = new Deferred(HEADERS);

        blackhole.consume(deferred.getDate());
        blackhole.consume(deferred.getContentLength());
        blackhole.consume(deferred.getContentType());
        blackhole.consume(deferred.getLastModified());
        blackhole.consume(deferred.getETag());
    }

    @Benchmark
    public void eagerDeserializationSomeProperties(Blackhole blackhole) {
        Eager eager = new Eager(HEADERS);

        blackhole.consume(eager.getLastModified());
        blackhole.consume(eager.getContentLength());
    }

    @Benchmark
    public void eagerDeserializationAllProperties(Blackhole blackhole) {
        Eager eager = new Eager(HEADERS);

        blackhole.consume(eager.getDate());
        blackhole.consume(eager.getContentLength());
        blackhole.consume(eager.getContentType());
        blackhole.consume(eager.getLastModified());
        blackhole.consume(eager.getETag());
    }

    private static final class Databind {
        @JsonProperty("Date")
        private DateTimeRfc1123 date;

        @JsonProperty("Content-Length")
        private int contentLength;

        @JsonProperty("Content-Type")
        private String contentType;

        @JsonProperty("Last-Modified")
        private DateTimeRfc1123 lastModified;

        @JsonProperty("eTag")
        private String eTag;

        public OffsetDateTime getDate() {
            return (date == null) ? null : date.getDateTime();
        }

        public int getContentLength() {
            return contentLength;
        }

        public String getContentType() {
            return contentType;
        }

        public OffsetDateTime getLastModified() {
            return (lastModified == null) ? null : lastModified.getDateTime();
        }

        public String getETag() {
            return eTag;
        }
    }

    private static final class Deferred {
        private boolean hasDateBeenDeserialized;
        private DateTimeRfc1123 date;

        private boolean hasContentLengthBeenDeserialized;
        private int contentLength;

        private boolean hasContentTypeBeenDeserialized;
        private String contentType;

        private boolean hasLastModifiedBeenDeserialized;
        private DateTimeRfc1123 lastModified;

        private boolean hasETagBeenDeserialized;
        private String eTag;

        private final HttpHeaders rawHeaders;

        Deferred(HttpHeaders rawHeaders) {
            this.rawHeaders = rawHeaders;
        }

        public OffsetDateTime getDate() {
            if (!hasDateBeenDeserialized) {
                date = new DateTimeRfc1123(rawHeaders.getValue(HttpHeaderName.DATE));
                hasDateBeenDeserialized = true;
            }

            return (date == null) ? null : date.getDateTime();
        }

        public int getContentLength() {
            if (!hasContentLengthBeenDeserialized) {
                contentLength = Integer.parseInt(rawHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
                hasContentLengthBeenDeserialized = true;
            }

            return contentLength;
        }

        public String getContentType() {
            if (!hasContentTypeBeenDeserialized) {
                contentType = rawHeaders.getValue(HttpHeaderName.CONTENT_TYPE);
                hasContentTypeBeenDeserialized = true;
            }

            return contentType;
        }

        public OffsetDateTime getLastModified() {
            if (!hasLastModifiedBeenDeserialized) {
                lastModified = new DateTimeRfc1123(rawHeaders.getValue(HttpHeaderName.LAST_MODIFIED));
                hasLastModifiedBeenDeserialized = true;
            }

            return (lastModified == null) ? null : lastModified.getDateTime();
        }

        public String getETag() {
            if (!hasETagBeenDeserialized) {
                eTag = rawHeaders.getValue(HttpHeaderName.ETAG);
                hasETagBeenDeserialized = true;
            }

            return eTag;
        }
    }

    private static final class Eager {
        private final DateTimeRfc1123 date;

        private final int contentLength;

        private final String contentType;

        private final DateTimeRfc1123 lastModified;

        private final String eTag;

        Eager(HttpHeaders rawHeaders) {
            date = new DateTimeRfc1123(rawHeaders.getValue(HttpHeaderName.DATE));
            contentLength = Integer.parseInt(rawHeaders.getValue(HttpHeaderName.CONTENT_LENGTH));
            contentType = rawHeaders.getValue(HttpHeaderName.CONTENT_TYPE);
            lastModified = new DateTimeRfc1123(rawHeaders.getValue(HttpHeaderName.LAST_MODIFIED));
            eTag = rawHeaders.getValue(HttpHeaderName.ETAG);
        }

        public OffsetDateTime getDate() {
            return (date == null) ? null : date.getDateTime();
        }

        public int getContentLength() {
            return contentLength;
        }

        public String getContentType() {
            return contentType;
        }

        public OffsetDateTime getLastModified() {
            return (lastModified == null) ? null : lastModified.getDateTime();
        }

        public String getETag() {
            return eTag;
        }
    }
}
