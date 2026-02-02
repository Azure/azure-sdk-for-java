// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.test.shared;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import org.junit.jupiter.params.provider.Arguments;
import java.time.OffsetDateTime;
/**
 * Helper class to build test arguments for regular SAS string-to-sign tests.
 * This is the base class that contains common fields shared by both regular SAS and user delegation SAS.
 * All fields default to null, so you only need to set the ones you're testing.
 * <p>
 * For user delegation SAS tests, use {@link UserDelegationSasTestData} which extends this class.
 */
public class SasTestData {
    // Common fields for all SAS types
    protected OffsetDateTime startTime;
    protected SasIpRange ipRange;
    protected SasProtocol protocol;
    protected String cacheControl;
    protected String disposition;
    protected String encoding;
    protected String language;
    protected String type;
    protected String expectedStringToSign;
    // Regular SAS specific field
    protected String identifier;  // Signed identifier for regular SAS
    public SasTestData setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }
    public SasTestData setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
    public SasTestData setIpRange(SasIpRange ipRange) {
        this.ipRange = ipRange;
        return this;
    }
    public SasTestData setProtocol(SasProtocol protocol) {
        this.protocol = protocol;
        return this;
    }
    public SasTestData setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }
    public SasTestData setDisposition(String disposition) {
        this.disposition = disposition;
        return this;
    }
    public SasTestData setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }
    public SasTestData setLanguage(String language) {
        this.language = language;
        return this;
    }
    public SasTestData setType(String type) {
        this.type = type;
        return this;
    }
    public SasTestData setExpectedStringToSign(String expectedStringToSign) {
        this.expectedStringToSign = expectedStringToSign;
        return this;
    }
    /**
     * Converts to Arguments for regular SAS tests.
     * Returns arguments in this order:
     * startTime, identifier, ipRange, protocol, cacheControl, disposition, encoding, language, type, expectedStringToSign
     *
     * @return Arguments for parameterized tests matching the signature of regular SAS test methods
     */
    public Arguments toArguments() {
        return Arguments.of(startTime, identifier, ipRange, protocol, cacheControl, disposition, encoding, language,
            type, expectedStringToSign);
    }
}
