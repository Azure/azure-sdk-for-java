// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

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
    private String preauthorizedAgentObjectId;
    private String agentObjectId;
    private String correlationId;
    private String delegatedUserObjectId;
    private String delegatedUserTenantId;

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
    public UserDelegationSasTestData setSnapshotId(String snapshotId) {
        super.setSnapshotId(snapshotId);
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
    public UserDelegationSasTestData setVersionId(String versionId) {
        super.setVersionId(versionId);
        return this;
    }

    @Override
    public UserDelegationSasTestData setEncryptionScope(String encryptionScope) {
        super.setEncryptionScope(encryptionScope);
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

    public UserDelegationSasTestData setPreauthorizedAgentObjectId(String preauthorizedAgentObjectId) {
        this.preauthorizedAgentObjectId = preauthorizedAgentObjectId;
        return this;
    }

    public UserDelegationSasTestData setAgentObjectId(String agentObjectId) {
        this.agentObjectId = agentObjectId;
        return this;
    }

    public UserDelegationSasTestData setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public UserDelegationSasTestData setDelegatedUserObjectId(String delegatedUserObjectId) {
        this.delegatedUserObjectId = delegatedUserObjectId;
        return this;
    }

    public UserDelegationSasTestData setDelegatedUserTenantId(String delegatedUserTenantId) {
        this.delegatedUserTenantId = delegatedUserTenantId;
        return this;
    }

    /**
     * Converts to Arguments for user delegation SAS tests with request headers and query parameters.
     *
     * @return Arguments for parameterized tests with headers and query parameters included
     */
    public Arguments toArguments() {
        return Arguments.of(
            getStartTime(), keyOid, keyTid, keyStart, keyExpiry, keyService, keyVersion, keyValue,
            getIpRange(), getProtocol(), getSnapshotId(), getCacheControl(), getDisposition(), getEncoding(),
            getLanguage(), getType(), getVersionId(), preauthorizedAgentObjectId, correlationId,
            getEncryptionScope(), delegatedUserObjectId, delegatedUserTenantId,
            requestHeaders, requestQueryParameters, getExpectedStringToSign()
        );
    }

    public Arguments toDatalakeArguments() {
        return Arguments.of(
            getStartTime(), keyOid, keyTid, keyStart, keyExpiry, keyService, keyVersion, keyValue,
            getIpRange(), getProtocol(), getCacheControl(), getDisposition(), getEncoding(),
            getLanguage(), getType(),  preauthorizedAgentObjectId, agentObjectId, correlationId,
            requestHeaders, requestQueryParameters, getExpectedStringToSign()
        );
    }

    /*
     * We test string-to-sign functionality directly related to user delegation SAS-specific parameters.
     */
    public static Stream<Arguments> blobSasImplUtilStringToSignUserDelegationKeySupplier() {
        // Use LinkedHashMap to ensure deterministic iteration order
        Map<String, String> singleHeader = new LinkedHashMap<>();
        singleHeader.put("x-ms-encryption-key-sha256", "hashvalue");

        Map<String, String> singleQueryParam = new LinkedHashMap<>();
        singleQueryParam.put("comp", "blocklist");

        Map<String, String> multipleHeaders = new LinkedHashMap<>();
        multipleHeaders.put("x-ms-encryption-key-sha256", "hashvalue");
        multipleHeaders.put("x-ms-source-if-match", "etag");

        Map<String, String> multipleQueryParams = new LinkedHashMap<>();
        multipleQueryParams.put("blockid", "blockidvalue");
        multipleQueryParams.put("comp", "blocklist");

        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        String expiryTimeStr = Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime);

        return Stream.of(
            //StartTime
            new UserDelegationSasTestData().setStartTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n"
                    + expiryTimeStr
                    + "\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Key Object ID
            new UserDelegationSasTestData().setKeyOid("11111111-1111-1111-1111-111111111111")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Key Tenant ID
            new UserDelegationSasTestData().setKeyTid("22222222-2222-2222-2222-222222222222")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Key Start Time
            new UserDelegationSasTestData()
                .setKeyStart(OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Key Expiry Time
            new UserDelegationSasTestData()
                .setKeyExpiry(OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Key Service
            new UserDelegationSasTestData().setKeyService("b")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Key Version
            new UserDelegationSasTestData().setKeyVersion("2018-06-17")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Sas Ip Range
            new UserDelegationSasTestData().setIpRange(new SasIpRange().setIpMin("ip"))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\nip\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Sas Protocol
            new UserDelegationSasTestData().setProtocol(SasProtocol.HTTPS_ONLY)
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Snapshot ID
            new UserDelegationSasTestData().setSnapshotId("snapId")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nbs\nsnapId\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Cache Control
            new UserDelegationSasTestData().setCacheControl("control")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\ncontrol\n\n\n\n")
                .toArguments(),
            // Content Disposition
            new UserDelegationSasTestData().setDisposition("disposition")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\ndisposition\n\n\n")
                .toArguments(),
            // Content Encoding
            new UserDelegationSasTestData().setEncoding("encoding")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\nencoding\n\n")
                .toArguments(),
            // Content Language
            new UserDelegationSasTestData().setLanguage("language")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\nlanguage\n")
                .toArguments(),
            // Content Type
            new UserDelegationSasTestData().setType("type")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\ntype")
                .toArguments(),
            // Version ID
            new UserDelegationSasTestData().setVersionId("versionId")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nbv\nversionId\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Preauthorized Agent Object ID
            new UserDelegationSasTestData().setPreauthorizedAgentObjectId("preAuthAgentOid")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\npreAuthAgentOid\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Correlation ID
            new UserDelegationSasTestData().setCorrelationId("cid")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\ncid\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Encryption Scope
            new UserDelegationSasTestData().setEncryptionScope("encryptionScope")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\nencryptionScope\n\n\n\n\n\n\n")
                .toArguments(),
            // Delegated User Tenant ID
            new UserDelegationSasTestData().setDelegatedUserTenantId("delegatedTenantId")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\ndelegatedTenantId\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Delegated User Object ID
            new UserDelegationSasTestData().setDelegatedUserObjectId("delegatedOid")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\ndelegatedOid\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toArguments(),
            // Request Headers (single header)
            new UserDelegationSasTestData().setRequestHeaders(singleHeader)
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\nx-ms-encryption-key-sha256:hashvalue\n\n\n\n\n\n\n")
                .toArguments(),
            // Request Query Params (single param)
            new UserDelegationSasTestData().setRequestQueryParameters(singleQueryParam)
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\ncomp:blocklist\n\n\n\n\n")
                .toArguments(),
            // Request Headers and Query Params (single each)
            new UserDelegationSasTestData().setRequestQueryParameters(singleQueryParam)
                .setRequestHeaders(singleHeader)
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\nx-ms-encryption-key-sha256:hashvalue\n\n\ncomp:blocklist\n\n\n\n\n")
                .toArguments(),
            // Test multiple headers and multiple query parameters
            new UserDelegationSasTestData().setRequestHeaders(multipleHeaders)
                .setRequestQueryParameters(multipleQueryParams)
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\nx-ms-encryption-key-sha256:hashvalue\n"
                    + "x-ms-source-if-match:etag\n\n\nblockid:blockidvalue\n" + "comp:blocklist\n\n\n\n\n")
                .toArguments(),
            // Test with all parameters populated
            new UserDelegationSasTestData().setStartTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setKeyOid("11111111-1111-1111-1111-111111111111")
                .setKeyTid("22222222-2222-2222-2222-222222222222")
                .setKeyStart(OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC))
                .setKeyExpiry(OffsetDateTime.of(LocalDateTime.of(2018, 6, 1, 0, 0), ZoneOffset.UTC))
                .setKeyService("b")
                .setKeyVersion("2018-06-17")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setIpRange(new SasIpRange().setIpMin("ip"))
                .setProtocol(SasProtocol.HTTPS_ONLY)
                .setSnapshotId("snapId")
                .setCacheControl("control")
                .setDisposition("disposition")
                .setEncoding("encoding")
                .setLanguage("language")
                .setType("type")
                .setVersionId(null) // versionId and snapId are mutually exclusive
                .setPreauthorizedAgentObjectId("preAuthAgentOid")
                .setCorrelationId("cid")
                .setEncryptionScope("encryptionScope")
                .setDelegatedUserObjectId("delegatedOid")
                .setDelegatedUserTenantId("delegatedTenantId")
                .setRequestHeaders(multipleHeaders)
                .setRequestQueryParameters(multipleQueryParams)
                .setExpectedStringToSign("r\n" // permissions
                    + expiryTimeStr // startTime
                    + "\n"
                    + expiryTimeStr // expiryTime
                    + "\n/blob/%s/containerName/blobName\n" // canonicalName
                    + "11111111-1111-1111-1111-111111111111\n" // keyOid
                    + "22222222-2222-2222-2222-222222222222\n" // keyTid
                    + "2018-01-01T00:00:00Z\n" // keyStart
                    + "2018-06-01T00:00:00Z\n" // keyExpiry
                    + "b\n" // keyService
                    + "2018-06-17\n" // keyVersion
                    + "preAuthAgentOid\n" // preauthorizedAgentObjectId
                    + "\n" // (always empty for blob, agentObjectId)
                    + "cid\n" // cid (correlationId)
                    + "delegatedTenantId\n" // delegatedUserTenantId
                    + "delegatedOid\n" // delegatedUserObjectId
                    + "ip\n" // sasIpRange
                    + SasProtocol.HTTPS_ONLY + "\n" // protocol
                    + Constants.SAS_SERVICE_VERSION + "\n" // VERSION
                    + "bs\n" // resource (blob snapshot)
                    + "snapId\n" // snapId (versionSegment with snapId)
                    + "encryptionScope\n" // encryptionScope
                    + "x-ms-encryption-key-sha256:hashvalue\n" // requestHeaders (multiple)
                    + "x-ms-source-if-match:etag\n\n" // requestHeaders continuation + newline separator
                    + "\nblockid:blockidvalue\n" // requestQueryParameters (multiple, with prepended newline)
                    + "comp:blocklist\n" // requestQueryParameters continuation
                    + "control\n" // cacheControl
                    + "disposition\n" // contentDisposition
                    + "encoding\n" // contentEncoding
                    + "language\n" // contentLanguage
                    + "type" // contentType (no trailing newline)
                )
                .toArguments());
    }

    public static Stream<Arguments> dataLakeSasImplUtilStringToSignUserDelegationKeySupplier() {
        // Use LinkedHashMap to ensure deterministic iteration order
        Map<String, String> singleHeader = new LinkedHashMap<>();
        singleHeader.put("x-ms-encryption-key-sha256", "hashvalue");

        Map<String, String> singleQueryParam = new LinkedHashMap<>();
        singleQueryParam.put("comp", "blocklist");

        Map<String, String> multipleHeaders = new LinkedHashMap<>();
        multipleHeaders.put("x-ms-encryption-key-sha256", "hashvalue");
        multipleHeaders.put("x-ms-source-if-match", "etag");

        Map<String, String> multipleQueryParams = new LinkedHashMap<>();
        multipleQueryParams.put("blockid", "blockidvalue");
        multipleQueryParams.put("comp", "blocklist");

        OffsetDateTime expiryTime = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        String expiryTimeStr = Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime);

        // We test string to sign functionality directly related to user delegation sas specific parameters
        return Stream.of(
            // Start time
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setStartTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n"
                    + expiryTimeStr
                    + "\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyOid("11111111-1111-1111-1111-111111111111")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyTid("22222222-2222-2222-2222-222222222222")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData()
                .setKeyStart(OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData()
                .setKeyExpiry(OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC))
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyService("b")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\nb\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyVersion("2018-06-17")
                .setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n2018-06-17\n\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setIpRange(new SasIpRange().setIpMin("ip"))
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\nip\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setEncoding("encoding")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\nencoding\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setType("type")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\n\n\n\n\ntype")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setPreauthorizedAgentObjectId("preAuthAgentOid")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\npreAuthAgentOid\n\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setAgentObjectId("agentOid")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\nagentOid\n\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setCorrelationId("cid")
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\ncid\n\n\n\n\n"
                    + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setRequestHeaders(singleHeader)
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\nx-ms-encryption-key-sha256:hashvalue\n\n\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setRequestQueryParameters(singleQueryParam)
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\n\n\ncomp:blocklist\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setRequestHeaders(singleHeader)
                .setRequestQueryParameters(singleQueryParam)
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\nx-ms-encryption-key-sha256:hashvalue\n\n\ncomp:blocklist\n\n\n\n\n")
                .toDatalakeArguments(),
            new UserDelegationSasTestData().setKeyValue("3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=")
                .setRequestHeaders(multipleHeaders)
                .setRequestQueryParameters(multipleQueryParams)
                .setExpectedStringToSign("r\n\n"
                    + expiryTimeStr
                    + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION
                    + "\nb\n\n\nx-ms-encryption-key-sha256:hashvalue\n"
                    + "x-ms-source-if-match:etag\n\n\nblockid:blockidvalue\n" + "comp:blocklist\n\n\n\n\n")
                .toDatalakeArguments());
    }
}
