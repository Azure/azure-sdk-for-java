// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import org.junit.jupiter.params.provider.Arguments;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

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

    public OffsetDateTime getStartTime() {
        return startTime;
    }
    public String getIdentifier() {
        return identifier;
    }
    public SasIpRange getIpRange() {
        return ipRange;
    }
    public SasProtocol getProtocol() {
        return protocol;
    }
    public String getSnapshotId() {
        return snapshotId;
    }
    public String getCacheControl() {
        return cacheControl;
    }
    public String getDisposition() {
        return disposition;
    }
    public String getEncoding() {
        return encoding;
    }
    public String getLanguage() {
        return language;
    }
    public String getType() {
        return type;
    }
    public String getVersionId() {
        return versionId;
    }
    public String getEncryptionScope() {
        return encryptionScope;
    }
    public String getExpectedStringToSign() {
        return expectedStringToSign;
    }

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

    public Arguments toDataLakeArguments() {
        return Arguments.of(startTime, identifier, ipRange, protocol, cacheControl, disposition, encoding, language,
            type, expectedStringToSign);
    }

    /*
    We don't test the blob or containerName properties because canonicalized resource is always added as at least
    /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
    sas but the construction of the string to sign.
    Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
    */
    public static Stream<Arguments> blobSasImplUtilStringToSignSupplier() {
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        String expiryTimeStr = Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime);

        return Stream.of(
            //Start Time
            new SasTestData().setStartTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setExpectedStringToSign("r\n"
                    + expiryTimeStr
                    + "\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
                .toArguments(),
            // Identifier
            new SasTestData().setIdentifier("id")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n")
                .toArguments(),
            // Sas IP Range
            new SasTestData().setIpRange(new SasIpRange().setIpMin("ip"))
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n")
                .toArguments(),
            // Sas Protocol
            new SasTestData().setProtocol(SasProtocol.HTTPS_ONLY)
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
                .toArguments(),
            // Snapshot ID
            new SasTestData().setSnapshotId("snapId")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nbs\nsnapId\n\n\n\n\n\n")
                .toArguments(),
            // Cache Control
            new SasTestData().setCacheControl("control")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\ncontrol\n\n\n\n")
                .toArguments(),
            // Content Disposition
            new SasTestData().setDisposition("disposition")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\ndisposition\n\n\n")
                .toArguments(),
            // Content Encoding
            new SasTestData().setEncoding("encoding")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\nencoding\n\n")
                .toArguments(),
            // Content Language
            new SasTestData().setLanguage("language")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\nlanguage\n")
                .toArguments(),
            // Content Type
            new SasTestData().setType("type")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\ntype")
                .toArguments(),
            // Version ID
            new SasTestData().setVersionId("versionId")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nbv\nversionId\n\n\n\n\n\n")
                .toArguments(),
            // Encryption Scope
            new SasTestData().setEncryptionScope("encryptionScope")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\nencryptionScope\n\n\n\n\n")
                .toArguments());
    }

    public static Stream<Arguments> dataLakeSasImplUtilStringToSignSupplier() {
        // We don't test the blob or containerName properties because canonicalized resource is always added as at least
        // /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        // sas but the construction of the string to sign.
        // Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        String expiryTimeStr = Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime);

        return Stream.of(new SasTestData().setStartTime(expiryTime)
                .setExpectedStringToSign("r\n" + expiryTimeStr + "\n" + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
                .toDataLakeArguments(),
            new SasTestData().setIdentifier("id")
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\nid\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
                .toDataLakeArguments(),
            new SasTestData().setIpRange(new SasIpRange())
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\nip\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
                .toDataLakeArguments(),
            new SasTestData().setProtocol(SasProtocol.HTTPS_ONLY)
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\n\n"
                    + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n")
                .toDataLakeArguments(),
            new SasTestData().setCacheControl("control")
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ncontrol\n\n\n\n")
                .toDataLakeArguments(),
            new SasTestData().setDisposition("disposition")
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\ndisposition\n\n\n")
                .toDataLakeArguments(),
            new SasTestData().setEncoding("encoding")
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nencoding\n\n")
                .toDataLakeArguments(),
            new SasTestData().setLanguage("language")
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\nlanguage\n")
                .toDataLakeArguments(),
            new SasTestData().setType("type")
                .setExpectedStringToSign("r\n\n" + expiryTimeStr + "\n/blob/%s/fileSystemName/pathName\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\ntype")
                .toDataLakeArguments());
    }
}
