// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents common header names.
 */
@SuppressWarnings("unused")
public final class HeaderName implements ExpandableStringEnum<HeaderName> {
    private static final Map<String, HeaderName> VALUES = new ConcurrentHashMap<>();
    private final String caseSensitive;
    private final String caseInsensitive;

    private HeaderName(String name) {
        this.caseSensitive = name;
        this.caseInsensitive = name.toLowerCase();
    }

    /**
     * Gets the {@link HeaderName} based on the name passed into {@link #fromString(String)}.
     *
     * @return The header name based on the construction of this {@link HeaderName}.
     */
    public String getCaseSensitiveName() {
        return toString();
    }

    /**
     * Gets the {@link HeaderName} lower cased.
     *
     * @return The {@link HeaderName} lower cased.
     */
    public String getCaseInsensitiveName() {
        return caseInsensitive;
    }

    /**
     * Creates or finds a {@link HeaderName} for the passed {@code name}.
     *
     * <p>{@code null} will be returned if {@code name} is {@code null}.</p>
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link HeaderName} of the provided name, or {@code null} if {@code name} was
     * {@code null}.
     */
    public static HeaderName fromString(String name) {
        if (name == null) {
            return null;
        }

        HeaderName headerName = VALUES.get(name);

        if (headerName != null) {
            return headerName;
        }

        return VALUES.computeIfAbsent(name, HeaderName::new);
    }

    /**
     * Gets all known {@link HeaderName} values.
     *
     * @return The known {@link HeaderName} values.
     */
    public static Collection<HeaderName> values() {
        return VALUES.values();
    }

    @Override
    public String toString() {
        return caseSensitive;
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

        if (!(obj instanceof HeaderName)) {
            return false;
        }

        HeaderName other = (HeaderName) obj;

        return Objects.equals(caseInsensitive, other.caseInsensitive);
    }

    /**
     * {@code Accept}/{@code accept}
     */
    public static final HeaderName ACCEPT = fromString("Accept");

    /**
     * {@code Accept-Charset}/{@code accept-charset}
     */
    public static final HeaderName ACCEPT_CHARSET = fromString("Accept-Charset");

    /**
     * {@code Access-Control-Allow-Credentials}/{@code access-control-allow-credentials}
     */
    public static final HeaderName ACCESS_CONTROL_ALLOW_CREDENTIALS
        = fromString("Access-Control-Allow-Credentials");

    /**
     * {@code Access-Control-Allow-Headers}/{@code access-control-allow-headers}
     */
    public static final HeaderName ACCESS_CONTROL_ALLOW_HEADERS = fromString("Access-Control-Allow-Headers");

    /**
     * {@code Access-Control-Allow-Methods}/{@code access-control-allow-methods}
     */
    public static final HeaderName ACCESS_CONTROL_ALLOW_METHODS = fromString("Access-Control-Allow-Methods");

    /**
     * {@code Access-Control-Allow-Origin}/{@code access-control-allow-origin}
     */
    public static final HeaderName ACCESS_CONTROL_ALLOW_ORIGIN = fromString("Access-Control-Allow-Origin");

    /**
     * {@code Access-Control-Expose-Headers}/{@code access-control-expose-headers}
     */
    public static final HeaderName ACCESS_CONTROL_EXPOSE_HEADERS = fromString("Access-Control-Expose-Headers");

    /**
     * {@code Access-Control-Max-Age}/{@code access-control-max-age}
     */
    public static final HeaderName ACCESS_CONTROL_MAX_AGE = fromString("Access-Control-Max-Age");

    /**
     * {@code Accept-Datetime}/{@code accept-datetime}
     */
    public static final HeaderName ACCEPT_DATETIME = fromString("Accept-Datetime");

    /**
     * {@code Accept-Encoding}/{@code accept-encoding}
     */
    public static final HeaderName ACCEPT_ENCODING = fromString("Accept-Encoding");

    /**
     * {@code Accept-Language}/{@code accept-language}
     */
    public static final HeaderName ACCEPT_LANGUAGE = fromString("Accept-Language");

    /**
     * {@code Accept-Patch}/{@code accept-patch}
     */
    public static final HeaderName ACCEPT_PATCH = fromString("Accept-Patch");

    /**
     * {@code Accept-Ranges}/{@code accept-ranges}
     */
    public static final HeaderName ACCEPT_RANGES = fromString("Accept-Ranges");

    /**
     * {@code Age}/{@code age}
     */
    public static final HeaderName AGE = fromString("Age");

    /**
     * {@code Allow}/{@code allow}
     */
    public static final HeaderName ALLOW = fromString("Allow");

    /**
     * {@code Authorization}/{@code authorization}
     */
    public static final HeaderName AUTHORIZATION = fromString("Authorization");

    /**
     * {@code Cache-Control}/{@code cache-control}
     */
    public static final HeaderName CACHE_CONTROL = fromString("Cache-Control");

    /**
     * {@code client-request-id}
     */
    public static final HeaderName CLIENT_REQUEST_ID = fromString("client-request-id");

    /**
     * {@code Connection}/{@code connection}
     */
    public static final HeaderName CONNECTION = fromString("Connection");

    /**
     * {@code Content-Disposition}/{@code content-disposition}
     */
    public static final HeaderName CONTENT_DISPOSITION = fromString("Content-Disposition");

    /**
     * {@code Content-Encoding}/{@code content-encoding}
     */
    public static final HeaderName CONTENT_ENCODING = fromString("Content-Encoding");

    /**
     * {@code Content-Language}/{@code content-language}
     */
    public static final HeaderName CONTENT_LANGUAGE = fromString("Content-Language");

    /**
     * {@code Content-Length}/{@code content-length}
     */
    public static final HeaderName CONTENT_LENGTH = fromString("Content-Length");

    /**
     * {@code Content-Location}/{@code content-location}
     */
    public static final HeaderName CONTENT_LOCATION = fromString("Content-Location");

    /**
     * {@code Content-MD5}/{@code content-md5}
     */
    public static final HeaderName CONTENT_MD5 = fromString("Content-MD5");

    /**
     * {@code Content-Range}/{@code content-range}
     */
    public static final HeaderName CONTENT_RANGE = fromString("Content-Range");

    /**
     * {@code Content-Type}/{@code content-type}
     */
    public static final HeaderName CONTENT_TYPE = fromString("Content-Type");

    /**
     * {@code Cookie}/{@code cookie}
     */
    public static final HeaderName COOKIE = fromString("Cookie");

    /**
     * {@code Date}/{@code date}
     */
    public static final HeaderName DATE = fromString("Date");

    /**
     * {@code ETag}/{@code etag}
     */
    public static final HeaderName ETAG = fromString("ETag");

    /**
     * {@code Expect}/{@code expect}
     */
    public static final HeaderName EXPECT = fromString("Expect");

    /**
     * {@code Expires}/{@code expires}
     */
    public static final HeaderName EXPIRES = fromString("Expires");

    /**
     * {@code Forwarded}/{@code forwarded}
     */
    public static final HeaderName FORWARDED = fromString("Forwarded");

    /**
     * {@code From}/{@code from}
     */
    public static final HeaderName FROM = fromString("From");

    /**
     * {@code Host}/{@code host}
     */
    public static final HeaderName HOST = fromString("Host");

    /**
     * {@code HTTP2-Settings}/{@code http2-settings}
     */
    public static final HeaderName HTTP2_SETTINGS = fromString("HTTP2-Settings");

    /**
     * {@code If-Match}/{@code if-match}
     */
    public static final HeaderName IF_MATCH = fromString("If-Match");

    /**
     * {@code If-Modified-Since}/{@code if-modified-since}
     */
    public static final HeaderName IF_MODIFIED_SINCE = fromString("If-Modified-Since");

    /**
     * {@code If-None-Match}/{@code if-none-match}
     */
    public static final HeaderName IF_NONE_MATCH = fromString("If-None-Match");

    /**
     * {@code If-Range}/{@code if-range}
     */
    public static final HeaderName IF_RANGE = fromString("If-Range");

    /**
     * {@code If-Unmodified-Since}/{@code if-unmodified-since}
     */
    public static final HeaderName IF_UNMODIFIED_SINCE = fromString("If-Unmodified-Since");

    /**
     * {@code Last-Modified}/{@code last-modified}
     */
    public static final HeaderName LAST_MODIFIED = fromString("Last-Modified");

    /**
     * {@code Link}/{@code link}
     */
    public static final HeaderName LINK = fromString("Link");

    /**
     * {@code Location}/{@code location}
     */
    public static final HeaderName LOCATION = fromString("Location");

    /**
     * {@code Max-Forwards}/{@code max-forwards}
     */
    public static final HeaderName MAX_FORWARDS = fromString("Max-Forwards");

    /**
     * {@code Origin}/{@code origin}
     */
    public static final HeaderName ORIGIN = fromString("Origin");

    /**
     * {@code Pragma}/{@code pragma}
     */
    public static final HeaderName PRAGMA = fromString("Pragma");

    /**
     * {@code Prefer}/{@code prefer}
     */
    public static final HeaderName PREFER = fromString("Prefer");

    /**
     * {@code Preference-Applied}/{@code preference-applied}
     */
    public static final HeaderName PREFERENCE_APPLIED = fromString("Preference-Applied");

    /**
     * {@code Range}/{@code range}
     */
    public static final HeaderName RANGE = fromString("Range");

    /**
     * {@code Referer}/{@code referer}
     */
    public static final HeaderName REFERER = fromString("Referer");

    /**
     * {@code Retry-After}/{@code retry-after}
     */
    public static final HeaderName RETRY_AFTER = fromString("Retry-After");

    /**
     * {@code Server}/{@code server}
     */
    public static final HeaderName SERVER = fromString("Server");

    /**
     * {@code Set-Cookie}/{@code set-cookie}
     */
    public static final HeaderName SET_COOKIE = fromString("Set-Cookie");

    /**
     * {@code Strict-Transport-Security}/{@code strict-transport-security}
     */
    public static final HeaderName STRICT_TRANSPORT_SECURITY = fromString("Strict-Transport-Security");

    /**
     * {@code TE}/{@code te}
     */
    public static final HeaderName TE = fromString("TE");

    /**
     * {@code Trailer}/{@code trailer}
     */
    public static final HeaderName TRAILER = fromString("Trailer");

    /**
     * {@code Transfer-Encoding}/{@code transfer-encoding}
     */
    public static final HeaderName TRANSFER_ENCODING = fromString("Transfer-Encoding");

    /**
     * {@code User-Agent}/{@code user-agent}
     */
    public static final HeaderName USER_AGENT = fromString("User-Agent");

    /**
     * {@code Upgrade}/{@code upgrade}
     */
    public static final HeaderName UPGRADE = fromString("Upgrade");

    /**
     * {@code Vary}/{@code vary}
     */
    public static final HeaderName VARY = fromString("Vary");

    /**
     * {@code Via}/{@code via}
     */
    public static final HeaderName VIA = fromString("Via");

    /**
     * {@code Warning}/{@code warning}
     */
    public static final HeaderName WARNING = fromString("Warning");

    /**
     * {@code WWW-Authenticate}/{@code www-authenticate}
     */
    public static final HeaderName WWW_AUTHENTICATE = fromString("WWW-Authenticate");
}
