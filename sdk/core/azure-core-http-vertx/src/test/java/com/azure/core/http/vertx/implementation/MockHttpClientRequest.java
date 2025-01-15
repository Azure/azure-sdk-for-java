// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.vertx.implementation;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.StreamPriority;
import io.vertx.core.net.HostAndPort;

import java.util.function.Function;

public class MockHttpClientRequest implements HttpClientRequest {
    @Override
    public HttpClientRequest exceptionHandler(Handler<Throwable> handler) {
        return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
        return null;
    }

    @Override
    public void write(Buffer data, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public HttpClientRequest setWriteQueueMaxSize(int maxSize) {
        return null;
    }

    @Override
    public boolean writeQueueFull() {
        return false;
    }

    @Override
    public HttpClientRequest drainHandler(Handler<Void> handler) {
        return this;
    }

    @Override
    public HttpClientRequest authority(HostAndPort authority) {
        return null;
    }

    @Deprecated
    @Override
    public HttpClientRequest setHost(String host) {
        return null;
    }

    @Deprecated
    @Override
    public String getHost() {
        return "";
    }

    @Deprecated
    @Override
    public HttpClientRequest setPort(int port) {
        return null;
    }

    @Deprecated
    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public HttpClientRequest setFollowRedirects(boolean followRedirects) {
        return null;
    }

    @Override
    public boolean isFollowRedirects() {
        return false;
    }

    @Override
    public HttpClientRequest setMaxRedirects(int maxRedirects) {
        return null;
    }

    @Override
    public int getMaxRedirects() {
        return 0;
    }

    @Override
    public int numberOfRedirections() {
        return 0;
    }

    @Override
    public HttpClientRequest setChunked(boolean chunked) {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public HttpMethod getMethod() {
        return null;
    }

    @Override
    public HttpClientRequest setMethod(HttpMethod method) {
        return null;
    }

    @Override
    public String absoluteURI() {
        return "";
    }

    @Override
    public String getURI() {
        return "";
    }

    @Override
    public HttpClientRequest setURI(String uri) {
        return null;
    }

    @Override
    public String path() {
        return "";
    }

    @Override
    public String query() {
        return "";
    }

    @Override
    public MultiMap headers() {
        return null;
    }

    @Override
    public HttpClientRequest putHeader(String name, String value) {
        return null;
    }

    @Override
    public HttpClientRequest putHeader(CharSequence name, CharSequence value) {
        return null;
    }

    @Override
    public HttpClientRequest putHeader(String name, Iterable<String> values) {
        return null;
    }

    @Override
    public HttpClientRequest putHeader(CharSequence name, Iterable<CharSequence> values) {
        return null;
    }

    @Override
    public HttpClientRequest traceOperation(String op) {
        return null;
    }

    @Override
    public String traceOperation() {
        return "";
    }

    @Override
    public HttpVersion version() {
        return null;
    }

    @Override
    public Future<Void> write(String chunk) {
        return null;
    }

    @Override
    public void write(String chunk, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public Future<Void> write(String chunk, String enc) {
        return null;
    }

    @Override
    public void write(String chunk, String enc, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public HttpClientRequest continueHandler(@Nullable Handler<Void> handler) {
        return null;
    }

    @Override
    public HttpClientRequest earlyHintsHandler(@Nullable Handler<MultiMap> handler) {
        return null;
    }

    @Override
    public HttpClientRequest
        redirectHandler(@Nullable Function<HttpClientResponse, Future<HttpClientRequest>> handler) {
        return null;
    }

    @Override
    public Future<Void> sendHead() {
        return null;
    }

    @Override
    public HttpClientRequest sendHead(Handler<AsyncResult<Void>> completionHandler) {
        return null;
    }

    @Override
    public void connect(Handler<AsyncResult<HttpClientResponse>> handler) {

    }

    @Override
    public Future<HttpClientResponse> connect() {
        return null;
    }

    @Override
    public HttpClientRequest response(Handler<AsyncResult<HttpClientResponse>> handler) {
        return null;
    }

    @Override
    public Future<HttpClientResponse> response() {
        return null;
    }

    @Override
    public Future<Void> end(String chunk) {
        return null;
    }

    @Override
    public void end(String chunk, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public Future<Void> end(String chunk, String enc) {
        return null;
    }

    @Override
    public void end(String chunk, String enc, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public Future<Void> end(Buffer chunk) {
        return null;
    }

    @Override
    public void end(Buffer chunk, Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public Future<Void> end() {
        return null;
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {

    }

    @Deprecated
    @Override
    public HttpClientRequest setTimeout(long timeout) {
        return null;
    }

    @Override
    public HttpClientRequest pushHandler(Handler<HttpClientRequest> handler) {
        return null;
    }

    @Override
    public boolean reset(long code) {
        return false;
    }

    @Override
    public boolean reset(long code, Throwable cause) {
        return false;
    }

    @Override
    public HttpConnection connection() {
        return null;
    }

    @Override
    public HttpClientRequest writeCustomFrame(int type, int flags, Buffer payload) {
        return null;
    }

    @Override
    public StreamPriority getStreamPriority() {
        return null;
    }
}
