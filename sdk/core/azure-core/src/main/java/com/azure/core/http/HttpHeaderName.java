// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents HTTP header names for multiple versions of HTTP.
 */
public final class HttpHeaderName {
    private static final Map<String, HttpHeaderName> HEADER_NAMES = new ConcurrentHashMap<>(256);

    private final String caseSensitive;
    private final String caseInsensitive;

    private HttpHeaderName(String name) {
        this.caseSensitive = name;
        this.caseInsensitive = name.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the HTTP header name based on the name passed into {@link #fromString(String)}.
     *
     * @return The HTTP header name based on the construction of this {@link HttpHeaderName}.
     */
    public String getCaseSensitiveName() {
        return caseSensitive;
    }

    /**
     * Gets the HTTP header name lower cased.
     *
     * @return The HTTP header name lower cased.
     */
    public String getCaseInsensitiveName() {
        return caseInsensitive;
    }

    /**
     * Gets or creates the {@link HttpHeaderName} for the passed {@code name}.
     * <p>
     * null will be returned if {@code name} is null.
     *
     * @param name The name.
     * @return The HttpHeaderName of the passed name, or null if name was null.
     */
    public static HttpHeaderName fromString(String name) {
        if (name == null) {
            return null;
        }

        if (HEADER_NAMES.size() > 10000) {
            HEADER_NAMES.clear();
        }

        return HEADER_NAMES.computeIfAbsent(name, HttpHeaderName::new);
    }

    private static HttpHeaderName fromKnownHeader(String name) {
        HttpHeaderName knownHeader = new HttpHeaderName(name);

        HEADER_NAMES.put(knownHeader.caseSensitive, knownHeader);
        HEADER_NAMES.put(knownHeader.caseInsensitive, knownHeader);

        return knownHeader;
    }

    @Override
    public int hashCode() {
        return caseInsensitive.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof HttpHeaderName)) {
            return false;
        }

        HttpHeaderName other = (HttpHeaderName) obj;
        return caseInsensitive.equals(other.caseInsensitive);
    }

    /**
     * {@code Accept}/{@code accept}
     */
    public static final HttpHeaderName ACCEPT = fromKnownHeader("Accept");

    /**
     * {@code Accept-Charset}/{@code accept-charset}
     */
    public static final HttpHeaderName ACCEPT_CHARSET = fromKnownHeader("Accept-Charset");

    /**
     * {@code Access-Control-Allow-Credentials}/{@code access-control-allow-credentials}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_CREDENTIALS
        = fromKnownHeader("Access-Control-Allow-Credentials");

    /**
     * {@code Access-Control-Allow-Headers}/{@code access-control-allow-headers}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_HEADERS = fromKnownHeader("Access-Control-Allow-Headers");

    /**
     * {@code Access-Control-Allow-Methods}/{@code access-control-allow-methods}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_METHODS = fromKnownHeader("Access-Control-Allow-Methods");

    /**
     * {@code Access-Control-Allow-Origin}/{@code access-control-allow-origin}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_ORIGIN = fromKnownHeader("Access-Control-Allow-Origin");

    /**
     * {@code Access-Control-Expose-Headers}/{@code access-control-expose-headers}
     */
    public static final HttpHeaderName ACCESS_CONTROL_EXPOSE_HEADERS = fromKnownHeader("Access-Control-Expose-Headers");

    /**
     * {@code Access-Control-Max-Age}/{@code access-control-max-age}
     */
    public static final HttpHeaderName ACCESS_CONTROL_MAX_AGE = fromKnownHeader("Access-Control-Max-Age");

    /**
     * {@code Accept-Datetime}/{@code accept-datetime}
     */
    public static final HttpHeaderName ACCEPT_DATETIME = fromKnownHeader("Accept-Datetime");

    /**
     * {@code Accept-Encoding}/{@code accept-encoding}
     */
    public static final HttpHeaderName ACCEPT_ENCODING = fromKnownHeader("Accept-Encoding");

    /**
     * {@code Accept-Language}/{@code accept-language}
     */
    public static final HttpHeaderName ACCEPT_LANGUAGE = fromKnownHeader("Accept-Language");

    /**
     * {@code Accept-Patch}/{@code accept-patch}
     */
    public static final HttpHeaderName ACCEPT_PATCH = fromKnownHeader("Accept-Patch");

    /**
     * {@code Accept-Ranges}/{@code accept-ranges}
     */
    public static final HttpHeaderName ACCEPT_RANGES = fromKnownHeader("Accept-Ranges");

    /**
     * {@code Age}/{@code age}
     */
    public static final HttpHeaderName AGE = fromKnownHeader("Age");

    /**
     * {@code Allow}/{@code allow}
     */
    public static final HttpHeaderName ALLOW = fromKnownHeader("Allow");

    /**
     * {@code Authorization}/{@code authorization}
     */
    public static final HttpHeaderName AUTHORIZATION = fromKnownHeader("Authorization");

    /**
     * {@code Cache-Control}/{@code cache-control}
     */
    public static final HttpHeaderName CACHE_CONTROL = fromKnownHeader("Cache-Control");

    /**
     * {@code Connection}/{@code connection}
     */
    public static final HttpHeaderName CONNECTION = fromKnownHeader("Connection");

    /**
     * {@code Content-Disposition}/{@code content-disposition}
     */
    public static final HttpHeaderName CONTENT_DISPOSITION = fromKnownHeader("Content-Disposition");

    /**
     * {@code Content-Encoding}/{@code content-encoding}
     */
    public static final HttpHeaderName CONTENT_ENCODING = fromKnownHeader("Content-Encoding");

    /**
     * {@code Content-Language}/{@code content-language}
     */
    public static final HttpHeaderName CONTENT_LANGUAGE = fromKnownHeader("Content-Language");

    /**
     * {@code Content-Length}/{@code content-length}
     */
    public static final HttpHeaderName CONTENT_LENGTH = fromKnownHeader("Content-Length");

    /**
     * {@code Content-Location}/{@code content-location}
     */
    public static final HttpHeaderName CONTENT_LOCATION = fromKnownHeader("Content-Location");

    /**
     * {@code Content-MD5}/{@code content-md5}
     */
    public static final HttpHeaderName CONTENT_MD5 = fromKnownHeader("Content-MD5");

    /**
     * {@code Content-Range}/{@code content-range}
     */
    public static final HttpHeaderName CONTENT_RANGE = fromKnownHeader("Content-Range");

    /**
     * {@code Content-Type}/{@code content-type}
     */
    public static final HttpHeaderName CONTENT_TYPE = fromKnownHeader("Content-Type");

    /**
     * {@code Cookie}/{@code cookie}
     */
    public static final HttpHeaderName COOKIE = fromKnownHeader("Cookie");

    /**
     * {@code Date}/{@code date}
     */
    public static final HttpHeaderName DATE = fromKnownHeader("Date");

    /**
     * {@code ETag}/{@code etag}
     */
    public static final HttpHeaderName ETAG = fromKnownHeader("ETag");

    /**
     * {@code Expect}/{@code expect}
     */
    public static final HttpHeaderName EXPECT = fromKnownHeader("Expect");

    /**
     * {@code Expires}/{@code expires}
     */
    public static final HttpHeaderName EXPIRES = fromKnownHeader("Expires");

    /**
     * {@code Forwarded}/{@code forwarded}
     */
    public static final HttpHeaderName FORWARDED = fromKnownHeader("Forwarded");

    /**
     * {@code From}/{@code from}
     */
    public static final HttpHeaderName FROM = fromKnownHeader("From");

    /**
     * {@code Host}/{@code host}
     */
    public static final HttpHeaderName HOST = fromKnownHeader("Host");

    /**
     * {@code HTTP2-Settings}/{@code http2-settings}
     */
    public static final HttpHeaderName HTTP2_SETTINGS = fromKnownHeader("HTTP2-Settings");

    /**
     * {@code If-Match}/{@code if-match}
     */
    public static final HttpHeaderName IF_MATCH = fromKnownHeader("If-Match");

    /**
     * {@code If-Modified-Since}/{@code if-modified-since}
     */
    public static final HttpHeaderName IF_MODIFIED_SINCE = fromKnownHeader("If-Modified-Since");

    /**
     * {@code If-None-Match}/{@code if-none-match}
     */
    public static final HttpHeaderName IF_NONE_MATCH = fromKnownHeader("If-None-Match");

    /**
     * {@code If-Range}/{@code if-range}
     */
    public static final HttpHeaderName IF_RANGE = fromKnownHeader("If-Range");

    /**
     * {@code If-Unmodified-Since}/{@code if-unmodified-since}
     */
    public static final HttpHeaderName IF_UNMODIFIED_SINCE = fromKnownHeader("If-Unmodified-Since");

    /**
     * {@code Last-Modified}/{@code last-modified}
     */
    public static final HttpHeaderName LAST_MODIFIED = fromKnownHeader("Last-Modified");

    /**
     * {@code Link}/{@code link}
     */
    public static final HttpHeaderName LINK = fromKnownHeader("Link");

    /**
     * {@code Location}/{@code location}
     */
    public static final HttpHeaderName LOCATION = fromKnownHeader("Location");

    /**
     * {@code Max-Forwards}/{@code max-forwards}
     */
    public static final HttpHeaderName MAX_FORWARDS = fromKnownHeader("Max-Forwards");

    /**
     * {@code Origin}/{@code origin}
     */
    public static final HttpHeaderName ORIGIN = fromKnownHeader("Origin");

    /**
     * {@code Pragma}/{@code pragma}
     */
    public static final HttpHeaderName PRAGMA = fromKnownHeader("Pragma");

    /**
     * {@code Prefer}/{@code prefer}
     */
    public static final HttpHeaderName PREFER = fromKnownHeader("Prefer");

    /**
     * {@code Preference-Applied}/{@code preference-applied}
     */
    public static final HttpHeaderName PREFERENCE_APPLIED = fromKnownHeader("Preference-Applied");

    /**
     * {@code Proxy-Authenticate}/{@code proxy-authenticate}
     */
    public static final HttpHeaderName PROXY_AUTHENTICATE = fromKnownHeader("Proxy-Authenticate");

    /**
     * {@code Proxy-Authorization}/{@code proxy-authorization}
     */
    public static final HttpHeaderName PROXY_AUTHORIZATION = fromKnownHeader("Proxy-Authorization");

    /**
     * {@code Range}/{@code range}
     */
    public static final HttpHeaderName RANGE = fromKnownHeader("Range");

    /**
     * {@code Referer}/{@code referer}
     */
    public static final HttpHeaderName REFERER = fromKnownHeader("Referer");

    /**
     * {@code Retry-After}/{@code retry-after}
     */
    public static final HttpHeaderName RETRY_AFTER = fromKnownHeader("Retry-After");

    /**
     * {@code Server}/{@code server}
     */
    public static final HttpHeaderName SERVER = fromKnownHeader("Server");

    /**
     * {@code Set-Cookie}/{@code set-cookie}
     */
    public static final HttpHeaderName SET_COOKIE = fromKnownHeader("Set-Cookie");

    /**
     * {@code Strict-Transport-Security}/{@code strict-transport-security}
     */
    public static final HttpHeaderName STRICT_TRANSPORT_SECURITY = fromKnownHeader("Strict-Transport-Security");

    /**
     * {@code TE}/{@code te}
     */
    public static final HttpHeaderName TE = fromKnownHeader("TE");

    /**
     * {@code Trailer}/{@code trailer}
     */
    public static final HttpHeaderName TRAILER = fromKnownHeader("Trailer");

    /**
     * {@code Transfer-Encoding}/{@code transfer-encoding}
     */
    public static final HttpHeaderName TRANSFER_ENCODING = fromKnownHeader("Transfer-Encoding");

    /**
     * {@code User-Agent}/{@code user-agent}
     */
    public static final HttpHeaderName USER_AGENT = fromKnownHeader("User-Agent");

    /**
     * {@code Upgrade}/{@code upgrade}
     */
    public static final HttpHeaderName UPGRADE = fromKnownHeader("Upgrade");

    /**
     * {@code Vary}/{@code vary}
     */
    public static final HttpHeaderName VARY = fromKnownHeader("Vary");

    /**
     * {@code Via}/{@code via}
     */
    public static final HttpHeaderName VIA = fromKnownHeader("Via");

    /**
     * {@code Warning}/{@code warning}
     */
    public static final HttpHeaderName WARNING = fromKnownHeader("Warning");

    /**
     * {@code WWW-Authenticate}/{@code www-authenticate}
     */
    public static final HttpHeaderName WWW_AUTHENTICATE = fromKnownHeader("WWW-Authenticate");

    /**
     * {@code x-ms-client-request-id}
     */
    public static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = fromKnownHeader("x-ms-client-request-id");
}
