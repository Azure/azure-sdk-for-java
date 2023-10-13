// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

/**
 * Represents HTTP header names for multiple versions of HTTP.
 */
public enum HttpHeaderName {

    /**
     * {@code Accept}/{@code accept}
     */
    ACCEPT("Accept"),

    /**
     * {@code Accept-Charset}/{@code accept-charset}
     */
    ACCEPT_CHARSET("Accept-Charset"),

    /**
     * {@code Access-Control-Allow-Credentials}/{@code access-control-allow-credentials}
     */
    ACCESS_CONTROL_ALLOW_CREDENTIALS
       ("Access-Control-Allow-Credentials"),

    /**
     * {@code Access-Control-Allow-Headers}/{@code access-control-allow-headers}
     */
    ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),

    /**
     * {@code Access-Control-Allow-Methods}/{@code access-control-allow-methods}
     */
    ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),

    /**
     * {@code Access-Control-Allow-Origin}/{@code access-control-allow-origin}
     */
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),

    /**
     * {@code Access-Control-Expose-Headers}/{@code access-control-expose-headers}
     */
    ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),

    /**
     * {@code Access-Control-Max-Age}/{@code access-control-max-age}
     */
    ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),

    /**
     * {@code Accept-Datetime}/{@code accept-datetime}
     */
    ACCEPT_DATETIME("Accept-Datetime"),

    /**
     * {@code Accept-Encoding}/{@code accept-encoding}
     */
    ACCEPT_ENCODING("Accept-Encoding"),

    /**
     * {@code Accept-Language}/{@code accept-language}
     */
    ACCEPT_LANGUAGE("Accept-Language"),

    /**
     * {@code Accept-Patch}/{@code accept-patch}
     */
    ACCEPT_PATCH("Accept-Patch"),

    /**
     * {@code Accept-Ranges}/{@code accept-ranges}
     */
    ACCEPT_RANGES("Accept-Ranges"),

    /**
     * {@code Age}/{@code age}
     */
    AGE("Age"),

    /**
     * {@code Allow}/{@code allow}
     */
    ALLOW("Allow"),

    /**
     * {@code Authorization}/{@code authorization}
     */
    AUTHORIZATION("Authorization"),

    /**
     * {@code Cache-Control}/{@code cache-control}
     */
    CACHE_CONTROL("Cache-Control"),

    /**
     * {@code Connection}/{@code connection}
     */
    CONNECTION("Connection"),

    /**
     * {@code Content-Disposition}/{@code content-disposition}
     */
    CONTENT_DISPOSITION("Content-Disposition"),

    /**
     * {@code Content-Encoding}/{@code content-encoding}
     */
    CONTENT_ENCODING("Content-Encoding"),

    /**
     * {@code Content-Language}/{@code content-language}
     */
    CONTENT_LANGUAGE("Content-Language"),

    /**
     * {@code Content-Length}/{@code content-length}
     */
    CONTENT_LENGTH("Content-Length"),

    /**
     * {@code Content-Location}/{@code content-location}
     */
    CONTENT_LOCATION("Content-Location"),

    /**
     * {@code Content-MD5}/{@code content-md5}
     */
    CONTENT_MD5("Content-MD5"),

    /**
     * {@code Content-Range}/{@code content-range}
     */
    CONTENT_RANGE("Content-Range"),

    /**
     * {@code Content-Type}/{@code content-type}
     */
    CONTENT_TYPE("Content-Type"),

    /**
     * {@code Cookie}/{@code cookie}
     */
    COOKIE("Cookie"),

    /**
     * {@code Date}/{@code date}
     */
    DATE("Date"),

    /**
     * {@code ETag}/{@code etag}
     */
    ETAG("ETag"),

    /**
     * {@code Expect}/{@code expect}
     */
    EXPECT("Expect"),

    /**
     * {@code Expires}/{@code expires}
     */
    EXPIRES("Expires"),

    /**
     * {@code Forwarded}/{@code forwarded}
     */
    FORWARDED("Forwarded"),

    /**
     * {@code From}/{@code from}
     */
    FROM("From"),

    /**
     * {@code Host}/{@code host}
     */
    HOST("Host"),

    /**
     * {@code HTTP2-Settings}/{@code http2-settings}
     */
    HTTP2_SETTINGS("HTTP2-Settings"),

    /**
     * {@code If-Match}/{@code if-match}
     */
    IF_MATCH("If-Match"),

    /**
     * {@code If-Modified-Since}/{@code if-modified-since}
     */
    IF_MODIFIED_SINCE("If-Modified-Since"),

    /**
     * {@code If-None-Match}/{@code if-none-match}
     */
    IF_NONE_MATCH("If-None-Match"),

    /**
     * {@code If-Range}/{@code if-range}
     */
    IF_RANGE("If-Range"),

    /**
     * {@code If-Unmodified-Since}/{@code if-unmodified-since}
     */
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),

    /**
     * {@code Last-Modified}/{@code last-modified}
     */
    LAST_MODIFIED("Last-Modified"),

    /**
     * {@code Link}/{@code link}
     */
    LINK("Link"),

    /**
     * {@code Location}/{@code location}
     */
    LOCATION("Location"),

    /**
     * {@code Max-Forwards}/{@code max-forwards}
     */
    MAX_FORWARDS("Max-Forwards"),

    /**
     * {@code Origin}/{@code origin}
     */
    ORIGIN("Origin"),

    /**
     * {@code Pragma}/{@code pragma}
     */
    PRAGMA("Pragma"),

    /**
     * {@code Prefer}/{@code prefer}
     */
    PREFER("Prefer"),

    /**
     * {@code Preference-Applied}/{@code preference-applied}
     */
    PREFERENCE_APPLIED("Preference-Applied"),

    /**
     * {@code Proxy-Authenticate}/{@code proxy-authenticate}
     */
    PROXY_AUTHENTICATE("Proxy-Authenticate"),

    /**
     * {@code Proxy-Authorization}/{@code proxy-authorization}
     */
    PROXY_AUTHORIZATION("Proxy-Authorization"),

    /**
     * {@code Range}/{@code range}
     */
    RANGE("Range"),

    /**
     * {@code Referer}/{@code referer}
     */
    REFERER("Referer"),

    /**
     * {@code Retry-After}/{@code retry-after}
     */
    RETRY_AFTER("Retry-After"),

    /**
     * {@code Server}/{@code server}
     */
    SERVER("Server"),

    /**
     * {@code Set-Cookie}/{@code set-cookie}
     */
    SET_COOKIE("Set-Cookie"),

    /**
     * {@code Strict-Transport-Security}/{@code strict-transport-security}
     */
    STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),

    /**
     * {@code TE}/{@code te}
     */
    TE("TE"),

    /**
     * {@code Trailer}/{@code trailer}
     */
    TRAILER("Trailer"),

    /**
     * {@code Transfer-Encoding}/{@code transfer-encoding}
     */
    TRANSFER_ENCODING("Transfer-Encoding"),

    /**
     * {@code User-Agent}/{@code user-agent}
     */
    USER_AGENT("User-Agent"),

    /**
     * {@code Upgrade}/{@code upgrade}
     */
    UPGRADE("Upgrade"),

    /**
     * {@code Vary}/{@code vary}
     */
    VARY("Vary"),

    /**
     * {@code Via}/{@code via}
     */
    VIA("Via"),

    /**
     * {@code Warning}/{@code warning}
     */
    WARNING("Warning"),

    /**
     * {@code WWW-Authenticate}/{@code www-authenticate}
     */
    WWW_AUTHENTICATE("WWW-Authenticate"),

    /**
     * {@code x-ms-client-request-id}
     */
    X_MS_CLIENT_REQUEST_ID("x-ms-client-request-id"),

    RETRY_AFTER_MS_HEADER("retry-after-ms"),
    X_MS_RETRY_AFTER_MS_HEADER("x-ms-retry-after-ms");

    private String name;

    HttpHeaderName(String name) {
        this.name = name;
    }

}
