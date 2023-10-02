// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http;

import com.typespec.core.util.ExpandableStringEnum;

import java.util.Locale;

/**
 * Represents HTTP header names for multiple versions of HTTP.
 */
@SuppressWarnings("unused")
public final class HttpHeaderName extends ExpandableStringEnum<HttpHeaderName> {
    private String caseInsensitive;

    /**
     * Creates a new instance of {@link HttpHeaderName} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link HttpHeaderName} which doesn't have a String enum
     * value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public HttpHeaderName() {
    }

    /**
     * Gets the HTTP header name based on the name passed into {@link #fromString(String)}.
     *
     * @return The HTTP header name based on the construction of this {@link HttpHeaderName}.
     */
    public String getCaseSensitiveName() {
        return toString();
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

        HttpHeaderName headerName = fromString(name, HttpHeaderName.class);
        headerName.caseInsensitive = name.toLowerCase(Locale.ROOT);

        return headerName;
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
    public static final HttpHeaderName ACCEPT = fromString("Accept");

    /**
     * {@code Accept-Charset}/{@code accept-charset}
     */
    public static final HttpHeaderName ACCEPT_CHARSET = fromString("Accept-Charset");

    /**
     * {@code Access-Control-Allow-Credentials}/{@code access-control-allow-credentials}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_CREDENTIALS
        = fromString("Access-Control-Allow-Credentials");

    /**
     * {@code Access-Control-Allow-Headers}/{@code access-control-allow-headers}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_HEADERS = fromString("Access-Control-Allow-Headers");

    /**
     * {@code Access-Control-Allow-Methods}/{@code access-control-allow-methods}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_METHODS = fromString("Access-Control-Allow-Methods");

    /**
     * {@code Access-Control-Allow-Origin}/{@code access-control-allow-origin}
     */
    public static final HttpHeaderName ACCESS_CONTROL_ALLOW_ORIGIN = fromString("Access-Control-Allow-Origin");

    /**
     * {@code Access-Control-Expose-Headers}/{@code access-control-expose-headers}
     */
    public static final HttpHeaderName ACCESS_CONTROL_EXPOSE_HEADERS = fromString("Access-Control-Expose-Headers");

    /**
     * {@code Access-Control-Max-Age}/{@code access-control-max-age}
     */
    public static final HttpHeaderName ACCESS_CONTROL_MAX_AGE = fromString("Access-Control-Max-Age");

    /**
     * {@code Accept-Datetime}/{@code accept-datetime}
     */
    public static final HttpHeaderName ACCEPT_DATETIME = fromString("Accept-Datetime");

    /**
     * {@code Accept-Encoding}/{@code accept-encoding}
     */
    public static final HttpHeaderName ACCEPT_ENCODING = fromString("Accept-Encoding");

    /**
     * {@code Accept-Language}/{@code accept-language}
     */
    public static final HttpHeaderName ACCEPT_LANGUAGE = fromString("Accept-Language");

    /**
     * {@code Accept-Patch}/{@code accept-patch}
     */
    public static final HttpHeaderName ACCEPT_PATCH = fromString("Accept-Patch");

    /**
     * {@code Accept-Ranges}/{@code accept-ranges}
     */
    public static final HttpHeaderName ACCEPT_RANGES = fromString("Accept-Ranges");

    /**
     * {@code Age}/{@code age}
     */
    public static final HttpHeaderName AGE = fromString("Age");

    /**
     * {@code Allow}/{@code allow}
     */
    public static final HttpHeaderName ALLOW = fromString("Allow");

    /**
     * {@code Authorization}/{@code authorization}
     */
    public static final HttpHeaderName AUTHORIZATION = fromString("Authorization");

    /**
     * {@code Cache-Control}/{@code cache-control}
     */
    public static final HttpHeaderName CACHE_CONTROL = fromString("Cache-Control");

    /**
     * {@code Connection}/{@code connection}
     */
    public static final HttpHeaderName CONNECTION = fromString("Connection");

    /**
     * {@code Content-Disposition}/{@code content-disposition}
     */
    public static final HttpHeaderName CONTENT_DISPOSITION = fromString("Content-Disposition");

    /**
     * {@code Content-Encoding}/{@code content-encoding}
     */
    public static final HttpHeaderName CONTENT_ENCODING = fromString("Content-Encoding");

    /**
     * {@code Content-Language}/{@code content-language}
     */
    public static final HttpHeaderName CONTENT_LANGUAGE = fromString("Content-Language");

    /**
     * {@code Content-Length}/{@code content-length}
     */
    public static final HttpHeaderName CONTENT_LENGTH = fromString("Content-Length");

    /**
     * {@code Content-Location}/{@code content-location}
     */
    public static final HttpHeaderName CONTENT_LOCATION = fromString("Content-Location");

    /**
     * {@code Content-MD5}/{@code content-md5}
     */
    public static final HttpHeaderName CONTENT_MD5 = fromString("Content-MD5");

    /**
     * {@code Content-Range}/{@code content-range}
     */
    public static final HttpHeaderName CONTENT_RANGE = fromString("Content-Range");

    /**
     * {@code Content-Type}/{@code content-type}
     */
    public static final HttpHeaderName CONTENT_TYPE = fromString("Content-Type");

    /**
     * {@code Cookie}/{@code cookie}
     */
    public static final HttpHeaderName COOKIE = fromString("Cookie");

    /**
     * {@code Date}/{@code date}
     */
    public static final HttpHeaderName DATE = fromString("Date");

    /**
     * {@code ETag}/{@code etag}
     */
    public static final HttpHeaderName ETAG = fromString("ETag");

    /**
     * {@code Expect}/{@code expect}
     */
    public static final HttpHeaderName EXPECT = fromString("Expect");

    /**
     * {@code Expires}/{@code expires}
     */
    public static final HttpHeaderName EXPIRES = fromString("Expires");

    /**
     * {@code Forwarded}/{@code forwarded}
     */
    public static final HttpHeaderName FORWARDED = fromString("Forwarded");

    /**
     * {@code From}/{@code from}
     */
    public static final HttpHeaderName FROM = fromString("From");

    /**
     * {@code Host}/{@code host}
     */
    public static final HttpHeaderName HOST = fromString("Host");

    /**
     * {@code HTTP2-Settings}/{@code http2-settings}
     */
    public static final HttpHeaderName HTTP2_SETTINGS = fromString("HTTP2-Settings");

    /**
     * {@code If-Match}/{@code if-match}
     */
    public static final HttpHeaderName IF_MATCH = fromString("If-Match");

    /**
     * {@code If-Modified-Since}/{@code if-modified-since}
     */
    public static final HttpHeaderName IF_MODIFIED_SINCE = fromString("If-Modified-Since");

    /**
     * {@code If-None-Match}/{@code if-none-match}
     */
    public static final HttpHeaderName IF_NONE_MATCH = fromString("If-None-Match");

    /**
     * {@code If-Range}/{@code if-range}
     */
    public static final HttpHeaderName IF_RANGE = fromString("If-Range");

    /**
     * {@code If-Unmodified-Since}/{@code if-unmodified-since}
     */
    public static final HttpHeaderName IF_UNMODIFIED_SINCE = fromString("If-Unmodified-Since");

    /**
     * {@code Last-Modified}/{@code last-modified}
     */
    public static final HttpHeaderName LAST_MODIFIED = fromString("Last-Modified");

    /**
     * {@code Link}/{@code link}
     */
    public static final HttpHeaderName LINK = fromString("Link");

    /**
     * {@code Location}/{@code location}
     */
    public static final HttpHeaderName LOCATION = fromString("Location");

    /**
     * {@code Max-Forwards}/{@code max-forwards}
     */
    public static final HttpHeaderName MAX_FORWARDS = fromString("Max-Forwards");

    /**
     * {@code Origin}/{@code origin}
     */
    public static final HttpHeaderName ORIGIN = fromString("Origin");

    /**
     * {@code Pragma}/{@code pragma}
     */
    public static final HttpHeaderName PRAGMA = fromString("Pragma");

    /**
     * {@code Prefer}/{@code prefer}
     */
    public static final HttpHeaderName PREFER = fromString("Prefer");

    /**
     * {@code Preference-Applied}/{@code preference-applied}
     */
    public static final HttpHeaderName PREFERENCE_APPLIED = fromString("Preference-Applied");

    /**
     * {@code Proxy-Authenticate}/{@code proxy-authenticate}
     */
    public static final HttpHeaderName PROXY_AUTHENTICATE = fromString("Proxy-Authenticate");

    /**
     * {@code Proxy-Authorization}/{@code proxy-authorization}
     */
    public static final HttpHeaderName PROXY_AUTHORIZATION = fromString("Proxy-Authorization");

    /**
     * {@code Range}/{@code range}
     */
    public static final HttpHeaderName RANGE = fromString("Range");

    /**
     * {@code Referer}/{@code referer}
     */
    public static final HttpHeaderName REFERER = fromString("Referer");

    /**
     * {@code Retry-After}/{@code retry-after}
     */
    public static final HttpHeaderName RETRY_AFTER = fromString("Retry-After");

    /**
     * {@code Server}/{@code server}
     */
    public static final HttpHeaderName SERVER = fromString("Server");

    /**
     * {@code Set-Cookie}/{@code set-cookie}
     */
    public static final HttpHeaderName SET_COOKIE = fromString("Set-Cookie");

    /**
     * {@code Strict-Transport-Security}/{@code strict-transport-security}
     */
    public static final HttpHeaderName STRICT_TRANSPORT_SECURITY = fromString("Strict-Transport-Security");

    /**
     * {@code TE}/{@code te}
     */
    public static final HttpHeaderName TE = fromString("TE");

    /**
     * {@code Trailer}/{@code trailer}
     */
    public static final HttpHeaderName TRAILER = fromString("Trailer");

    /**
     * {@code Transfer-Encoding}/{@code transfer-encoding}
     */
    public static final HttpHeaderName TRANSFER_ENCODING = fromString("Transfer-Encoding");

    /**
     * {@code User-Agent}/{@code user-agent}
     */
    public static final HttpHeaderName USER_AGENT = fromString("User-Agent");

    /**
     * {@code Upgrade}/{@code upgrade}
     */
    public static final HttpHeaderName UPGRADE = fromString("Upgrade");

    /**
     * {@code Vary}/{@code vary}
     */
    public static final HttpHeaderName VARY = fromString("Vary");

    /**
     * {@code Via}/{@code via}
     */
    public static final HttpHeaderName VIA = fromString("Via");

    /**
     * {@code Warning}/{@code warning}
     */
    public static final HttpHeaderName WARNING = fromString("Warning");

    /**
     * {@code WWW-Authenticate}/{@code www-authenticate}
     */
    public static final HttpHeaderName WWW_AUTHENTICATE = fromString("WWW-Authenticate");

    /**
     * {@code x-ms-client-request-id}
     */
    public static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = fromString("x-ms-client-request-id");
}
