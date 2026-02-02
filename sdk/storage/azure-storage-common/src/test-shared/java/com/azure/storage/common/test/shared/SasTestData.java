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
    private OffsetDateTime startTime;
    private SasIpRange ipRange;
    private SasProtocol protocol;
    private String snapshotId;
    private String cacheControl;
    private String disposition;
    private String encoding;
    private String language;
    private String type;
    private String versionId;
    private String encryptionScope;
    private String expectedStringToSign;
    private String identifier;

    /**
     * Default constructor.
     * All fields default to null.
     */
    public SasTestData() {
    }

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

    public SasTestData setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
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

    public SasTestData setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public SasTestData setEncryptionScope(String encryptionScope) {
        this.encryptionScope = encryptionScope;
        return this;
    }

    public SasTestData setExpectedStringToSign(String expectedStringToSign) {
        this.expectedStringToSign = expectedStringToSign;
        return this;
    }

    public OffsetDateTime getStartTime() { return startTime; }
    public String getIdentifier() { return identifier; }
    public SasIpRange getIpRange() { return ipRange; }
    public SasProtocol getProtocol() { return protocol; }
    public String getSnapshotId() { return snapshotId; }
    public String getCacheControl() { return cacheControl; }
    public String getDisposition() { return disposition; }
    public String getEncoding() { return encoding; }
    public String getLanguage() { return language; }
    public String getType() { return type; }
    public String getVersionId() { return versionId; }
    public String getEncryptionScope() { return encryptionScope; }
    public String getExpectedStringToSign() { return expectedStringToSign; }

    /**
     * Converts to Arguments for regular SAS tests.
     * Returns arguments in this order:
     * startTime, identifier, ipRange, protocol, cacheControl, disposition, encoding, language, type, expectedStringToSign
     *
     * @return Arguments for parameterized tests matching the signature of regular SAS test methods
     */
    public Arguments toArguments() {
        return Arguments.of(startTime, identifier, ipRange, protocol, snapshotId, cacheControl, disposition, encoding,
            language, type, versionId, encryptionScope, expectedStringToSign);
    }
}
