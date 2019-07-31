// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.StorageErrorException;
import com.microsoft.azure.storage.blob.models.UserDelegationKey;
import io.reactivex.Single;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

final class Utility {

    static final DateTimeFormatter RFC_1123_GMT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ROOT).withZone(ZoneId.of("GMT"));

    static final DateTimeFormatter ISO_8601_UTC_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).withZone(ZoneId.of("UTC"));
    /**
     * Stores a reference to the UTC time zone.
     */
    static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    /**
     * Stores a reference to the date/time pattern with the greatest precision Java.util.Date is capable of expressing.
     */
    private static final String MAX_PRECISION_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    /**
     * Stores a reference to the ISO8601 date/time pattern.
     */
    private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    /**
     * Stores a reference to the ISO8601 date/time pattern.
     */
    private static final String ISO8601_PATTERN_NO_SECONDS = "yyyy-MM-dd'T'HH:mm'Z'";
    /**
     * The length of a datestring that matches the MAX_PRECISION_PATTERN.
     */
    private static final int MAX_PRECISION_DATESTRING_LENGTH = MAX_PRECISION_PATTERN.replaceAll("'", "").length();

    /**
     * Asserts that a value is not <code>null</code>.
     *
     * @param param
     *         A {@code String} that represents the name of the parameter, which becomes the exception message
     *         text if the <code>value</code> parameter is <code>null</code>.
     * @param value
     *         An <code>Object</code> object that represents the value of the specified parameter. This is the value
     *         being asserted as not <code>null</code>.
     */
    static void assertNotNull(final String param, final Object value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, SR.ARGUMENT_NULL_OR_EMPTY, param));
        }
    }

    /**
     * Returns a value that indicates whether the specified string is <code>null</code> or empty.
     *
     * @param value
     *         A {@code String} being examined for <code>null</code> or empty.
     *
     * @return <code>true</code> if the specified value is <code>null</code> or empty; otherwise, <code>false</code>
     */
    static boolean isNullOrEmpty(final String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Performs safe decoding of the specified string, taking care to preserve each + character, rather
     * than replacing it with a space character.
     *
     * @param stringToDecode
     *         A {@code String} that represents the string to decode.
     *
     * @return A {@code String} that represents the decoded string.
     */
    static String safeURLDecode(final String stringToDecode) {
        if (stringToDecode.length() == 0) {
            return Constants.EMPTY_STRING;
        }

        // '+' are decoded as ' ' so preserve before decoding
        if (stringToDecode.contains("+")) {
            final StringBuilder outBuilder = new StringBuilder();

            int startDex = 0;
            for (int m = 0; m < stringToDecode.length(); m++) {
                if (stringToDecode.charAt(m) == '+') {
                    if (m > startDex) {
                        try {
                            outBuilder.append(URLDecoder.decode(stringToDecode.substring(startDex, m),
                                    Constants.UTF8_CHARSET));
                        } catch (UnsupportedEncodingException e) {
                            throw new Error(e);
                        }
                    }

                    outBuilder.append("+");
                    startDex = m + 1;
                }
            }

            if (startDex != stringToDecode.length()) {
                try {
                    outBuilder.append(URLDecoder.decode(stringToDecode.substring(startDex, stringToDecode.length()),
                            Constants.UTF8_CHARSET));
                } catch (UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            }

            return outBuilder.toString();
        } else {
            try {
                return URLDecoder.decode(stringToDecode, Constants.UTF8_CHARSET);
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
        }
    }

    /**
     * Given a String representing a date in a form of the ISO8601 pattern, generates a Date representing it
     * with up to millisecond precision.
     *
     * @param dateString
     *         the {@code String} to be interpreted as a <code>Date</code>
     *
     * @return the corresponding <code>Date</code> object
     */
    public static OffsetDateTime parseDate(String dateString) {
        String pattern = MAX_PRECISION_PATTERN;
        switch (dateString.length()) {
            case 28: // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"-> [2012-01-04T23:21:59.1234567Z] length = 28
            case 27: // "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"-> [2012-01-04T23:21:59.123456Z] length = 27
            case 26: // "yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'"-> [2012-01-04T23:21:59.12345Z] length = 26
            case 25: // "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'"-> [2012-01-04T23:21:59.1234Z] length = 25
            case 24: // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"-> [2012-01-04T23:21:59.123Z] length = 24
                dateString = dateString.substring(0, MAX_PRECISION_DATESTRING_LENGTH);
                break;
            case 23: // "yyyy-MM-dd'T'HH:mm:ss.SS'Z'"-> [2012-01-04T23:21:59.12Z] length = 23
                // SS is assumed to be milliseconds, so a trailing 0 is necessary
                dateString = dateString.replace("Z", "0");
                break;
            case 22: // "yyyy-MM-dd'T'HH:mm:ss.S'Z'"-> [2012-01-04T23:21:59.1Z] length = 22
                // S is assumed to be milliseconds, so trailing 0's are necessary
                dateString = dateString.replace("Z", "00");
                break;
            case 20: // "yyyy-MM-dd'T'HH:mm:ss'Z'"-> [2012-01-04T23:21:59Z] length = 20
                pattern = Utility.ISO8601_PATTERN;
                break;
            case 17: // "yyyy-MM-dd'T'HH:mm'Z'"-> [2012-01-04T23:21Z] length = 17
                pattern = Utility.ISO8601_PATTERN_NO_SECONDS;
                break;
            default:
                throw new IllegalArgumentException(String.format(Locale.ROOT, SR.INVALID_DATE_STRING, dateString));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
        return LocalDateTime.parse(dateString, formatter).atZone(UTC_ZONE).toOffsetDateTime();
    }

    /**
     * Asserts that the specified integer is in the valid range.
     *
     * @param param
     *         A <code>String</code> that represents the name of the parameter, which becomes the exception message
     *         text if the <code>value</code> parameter is out of bounds.
     * @param value
     *         The value of the specified parameter.
     * @param min
     *         The minimum value for the specified parameter.
     * @param max
     *         The maximum value for the specified parameter.
     */
    public static void assertInBounds(final String param, final long value, final long min, final long max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, SR.PARAMETER_NOT_IN_RANGE, param, min, max));
        }
    }

    /**
     * Performs safe encoding of the specified string, taking care to insert %20 for each space character,
     * instead of inserting the + character.
     */
    static String safeURLEncode(final String stringToEncode) {
        if (stringToEncode == null) {
            return null;
        }
        if (stringToEncode.length() == 0) {
            return Constants.EMPTY_STRING;
        }

        try {
            final String tString = URLEncoder.encode(stringToEncode, Constants.UTF8_CHARSET);

            if (stringToEncode.contains(" ")) {
                final StringBuilder outBuilder = new StringBuilder();

                int startDex = 0;
                for (int m = 0; m < stringToEncode.length(); m++) {
                    if (stringToEncode.charAt(m) == ' ') {
                        if (m > startDex) {
                            outBuilder.append(URLEncoder.encode(stringToEncode.substring(startDex, m),
                                    Constants.UTF8_CHARSET));
                        }

                        outBuilder.append("%20");
                        startDex = m + 1;
                    }
                }

                if (startDex != stringToEncode.length()) {
                    outBuilder.append(URLEncoder.encode(stringToEncode.substring(startDex, stringToEncode.length()),
                            Constants.UTF8_CHARSET));
                }

                return outBuilder.toString();
            } else {
                return tString;
            }

        } catch (final UnsupportedEncodingException e) {
            throw new Error(e); // If we can't encode UTF-8, we fail.
        }
    }

    static <T> Single<T> postProcessResponse(Single<T> s) {
        s = addErrorWrappingToSingle(s);
        s = scrubEtagHeaderInResponse(s);
        return s;
    }

    /*
    We need to convert the generated StorageErrorException to StorageException, which has a cleaner interface and
    methods to conveniently access important values.
     */
    private static <T> Single<T> addErrorWrappingToSingle(Single<T> s) {
        return s.onErrorResumeNext(e -> {
            if (e instanceof StorageErrorException) {
                return Single.error(new StorageException((StorageErrorException) e));
            }
            return Single.error(e);
        });
    }

    /*
    The service is inconsistent in whether or not the etag header value has quotes. This method will check if the
    response returns an etag value, and if it does, remove any quotes that may be present to give the user a more
    predictable format to work with.
     */
    private static <T> Single<T> scrubEtagHeaderInResponse(Single<T> s) {
        return s.map(response -> {
            try {
                Object headers = response.getClass().getMethod("headers").invoke(response);
                Method etagGetterMethod = headers.getClass().getMethod("eTag");
                String etag = (String) etagGetterMethod.invoke(headers);
                // CommitBlockListHeaders has an etag property, but it's only set if the blob has committed blocks.
                if (etag == null) {
                    return response;
                }
                etag = etag.replace("\"", ""); // Etag headers without the quotes will be unaffected.
                headers.getClass().getMethod("withETag", String.class).invoke(headers, etag);
            } catch (NoSuchMethodException e) {
                // Response did not return an eTag value. No change necessary.
            }
            return response;
        });
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     *
     * @param delegate
     *         Key used to sign
     * @param stringToSign
     *         The UTF-8-encoded string to sign.
     *
     * @return A {@code String} that contains the HMAC-SHA256-encoded signature.
     *
     * @throws InvalidKeyException
     *         If the accountKey is not a valid Base64-encoded string.
     */
    static String delegateComputeHmac256(final UserDelegationKey delegate, String stringToSign) throws InvalidKeyException {
        try {
            byte[] key = Base64.getDecoder().decode(delegate.value());
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            hmacSha256.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] utf8Bytes = stringToSign.getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(hmacSha256.doFinal(utf8Bytes));
        } catch (final NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }
}
