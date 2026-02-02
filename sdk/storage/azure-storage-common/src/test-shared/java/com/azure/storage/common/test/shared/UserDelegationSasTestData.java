// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import org.junit.jupiter.params.provider.Arguments;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Helper class to build test arguments for User Delegation SAS string-to-sign tests.
 * Extends {@link SasTestData} to inherit common SAS fields.
 * All fields default to null, so you only need to set the ones you're testing.
 * <p>
 * Note: User delegation SAS does NOT use the 'identifier' field (that's for regular SAS).
 * Request headers and query parameters are only used in user delegation SAS.
 * <p>
 * For regular SAS tests, use {@link SasTestData} directly.
 */
public class UserDelegationSasTestData extends SasTestData {
    private String keyOid;
    private String keyTid;
    private OffsetDateTime keyStart;
    private OffsetDateTime keyExpiry;
    private String keyService;
    private String keyVersion;
    private String keyValue;
    private Map<String, String> requestHeaders;
    private Map<String, String> requestQueryParameters;
    private String saoid;
    private String suoid;
    private String cid;

    /**
     * Default constructor.
     * All fields default to null.
     */
    public UserDelegationSasTestData() {
        super();
    }

    // Override parent setters to return UserDelegationSasTestData for fluent API
    @Override
    public UserDelegationSasTestData setStartTime(OffsetDateTime startTime) {
        super.setStartTime(startTime);
        return this;
    }

    @Override
    public UserDelegationSasTestData setIpRange(SasIpRange ipRange) {
        super.setIpRange(ipRange);
        return this;
    }

    @Override
    public UserDelegationSasTestData setProtocol(SasProtocol protocol) {
        super.setProtocol(protocol);
        return this;
    }

    @Override
    public UserDelegationSasTestData setCacheControl(String cacheControl) {
        super.setCacheControl(cacheControl);
        return this;
    }

    @Override
    public UserDelegationSasTestData setDisposition(String disposition) {
        super.setDisposition(disposition);
        return this;
    }

    @Override
    public UserDelegationSasTestData setEncoding(String encoding) {
        super.setEncoding(encoding);
        return this;
    }

    @Override
    public UserDelegationSasTestData setLanguage(String language) {
        super.setLanguage(language);
        return this;
    }

    @Override
    public UserDelegationSasTestData setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public UserDelegationSasTestData setExpectedStringToSign(String expectedStringToSign) {
        super.setExpectedStringToSign(expectedStringToSign);
        return this;
    }

    public UserDelegationSasTestData setKeyOid(String keyOid) {
        this.keyOid = keyOid;
        return this;
    }

    public UserDelegationSasTestData setKeyTid(String keyTid) {
        this.keyTid = keyTid;
        return this;
    }

    public UserDelegationSasTestData setKeyStart(OffsetDateTime keyStart) {
        this.keyStart = keyStart;
        return this;
    }

    public UserDelegationSasTestData setKeyExpiry(OffsetDateTime keyExpiry) {
        this.keyExpiry = keyExpiry;
        return this;
    }

    public UserDelegationSasTestData setKeyService(String keyService) {
        this.keyService = keyService;
        return this;
    }

    public UserDelegationSasTestData setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
        return this;
    }

    public UserDelegationSasTestData setKeyValue(String keyValue) {
        this.keyValue = keyValue;
        return this;
    }

    public UserDelegationSasTestData setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    public UserDelegationSasTestData setRequestQueryParameters(Map<String, String> requestQueryParameters) {
        this.requestQueryParameters = requestQueryParameters;
        return this;
    }

    public UserDelegationSasTestData setSaoid(String saoid) {
        this.saoid = saoid;
        return this;
    }

    public UserDelegationSasTestData setSuoid(String suoid) {
        this.suoid = suoid;
        return this;
    }

    public UserDelegationSasTestData setCid(String cid) {
        this.cid = cid;
        return this;
    }

    /**
     * Converts to Arguments for user delegation SAS tests.
     * Returns arguments with or without request headers/query parameters based on the parameter.
     *
     * @param withHeadersAndParams Whether to include request headers and query parameters in the test data.
     * @return Arguments for parameterized tests matching the signature of user delegation SAS test methods
     */
    public Arguments toArguments(boolean withHeadersAndParams) {
        if (withHeadersAndParams) {
            return Arguments.of(
                getStartTime(), keyOid, keyTid, keyStart, keyExpiry, keyService, keyVersion, keyValue,
                getIpRange(), getProtocol(), getCacheControl(), getDisposition(), getEncoding(), getLanguage(), getType(),
                requestHeaders, requestQueryParameters, saoid, suoid, cid, getExpectedStringToSign()
            );
        } else {
            return Arguments.of(
                getStartTime(), keyOid, keyTid, keyStart, keyExpiry, keyService, keyVersion, keyValue,
                getIpRange(), getProtocol(), getCacheControl(), getDisposition(), getEncoding(), getLanguage(), getType(),
                saoid, suoid, cid, getExpectedStringToSign()
            );
        }
    }

    /**
     * Converts to Arguments for user delegation SAS tests with request headers and query parameters.
     * This is a convenience method that calls {@link #toArguments(boolean)} with true.
     *
     * @return Arguments for parameterized tests with headers and query parameters included
     */
    public Arguments toArguments() {
        return toArguments(true);
    }
}
