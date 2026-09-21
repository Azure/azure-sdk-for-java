// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trips the models whose fluent shape the generator customization restores by hand. The customization
 * un-finalizes fields and adds setters after the fact, so these assert that a model written with the restored
 * setters still reads back through the generated {@code fromXml}.
 */
public class RestoredModelXmlRoundTripTests {

    private static <T extends XmlSerializable<T>> T roundTrip(T value, Function<XmlReader, T> reader) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (XmlWriter writer = XmlWriter.toStream(stream)) {
                value.toXml(writer);
                writer.flush();
            }
            try (XmlReader xmlReader = XmlReader.fromBytes(stream.toByteArray())) {
                return reader.apply(xmlReader);
            }
        } catch (Exception e) {
            throw new RuntimeException("Round trip failed for " + value.getClass().getSimpleName(), e);
        }
    }

    private static <T extends XmlSerializable<T>> T roundTripChecked(T value, XmlThrowingReader<T> reader) {
        return roundTrip(value, xmlReader -> {
            try {
                return reader.read(xmlReader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FunctionalInterface
    private interface XmlThrowingReader<T> {
        T read(XmlReader reader) throws Exception;
    }

    @Test
    public void userDelegationKey() {
        UserDelegationKey original = new UserDelegationKey().setSignedObjectId("oid")
            .setSignedTenantId("tid")
            .setSignedStart(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .setSignedExpiry(OffsetDateTime.of(2020, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC))
            .setSignedService("b")
            .setSignedVersion("2020-10-02")
            .setValue("signature");

        UserDelegationKey actual = roundTripChecked(original, UserDelegationKey::fromXml);

        assertEquals("oid", actual.getSignedObjectId());
        assertEquals("tid", actual.getSignedTenantId());
        assertEquals("b", actual.getSignedService());
        assertEquals("2020-10-02", actual.getSignedVersion());
        assertEquals("signature", actual.getValue());
        assertEquals(original.getSignedStart(), actual.getSignedStart());
        assertEquals(original.getSignedExpiry(), actual.getSignedExpiry());
    }

    @Test
    public void geoReplication() {
        GeoReplication original = new GeoReplication().setStatus(GeoReplicationStatus.LIVE)
            .setLastSyncTime(OffsetDateTime.of(2020, 10, 21, 7, 28, 0, 0, ZoneOffset.UTC));

        GeoReplication actual = roundTripChecked(original, GeoReplication::fromXml);

        assertEquals(GeoReplicationStatus.LIVE, actual.getStatus());
        assertEquals(original.getLastSyncTime(), actual.getLastSyncTime());
    }

    @Test
    public void blobCorsRuleKeepsEveryRestoredSetter() {
        BlobCorsRule original = new BlobCorsRule().setAllowedOrigins("https://contoso.com")
            .setAllowedMethods("GET,PUT")
            .setAllowedHeaders("x-ms-meta-target")
            .setExposedHeaders("x-ms-meta-source")
            .setMaxAgeInSeconds(500);

        BlobCorsRule actual = roundTripChecked(original, BlobCorsRule::fromXml);

        assertEquals("https://contoso.com", actual.getAllowedOrigins());
        assertEquals("GET,PUT", actual.getAllowedMethods());
        assertEquals("x-ms-meta-target", actual.getAllowedHeaders());
        assertEquals("x-ms-meta-source", actual.getExposedHeaders());
        assertEquals(500, actual.getMaxAgeInSeconds());
    }

    @Test
    public void blobAnalyticsLoggingWithRetentionPolicy() {
        BlobAnalyticsLogging original = new BlobAnalyticsLogging().setVersion("1.0")
            .setDelete(true)
            .setRead(false)
            .setWrite(true)
            .setRetentionPolicy(new BlobRetentionPolicy().setEnabled(true).setDays(7));

        BlobAnalyticsLogging actual = roundTripChecked(original, BlobAnalyticsLogging::fromXml);

        assertEquals("1.0", actual.getVersion());
        assertTrue(actual.isDelete());
        assertTrue(actual.isWrite());
        assertNotNull(actual.getRetentionPolicy());
        assertTrue(actual.getRetentionPolicy().isEnabled());
        assertEquals(7, actual.getRetentionPolicy().getDays());
    }

    @Test
    public void blobMetricsWithRetentionPolicy() {
        BlobMetrics original = new BlobMetrics().setVersion("1.0")
            .setEnabled(true)
            .setIncludeApis(true)
            .setRetentionPolicy(new BlobRetentionPolicy().setEnabled(true).setDays(3));

        BlobMetrics actual = roundTripChecked(original, BlobMetrics::fromXml);

        assertEquals("1.0", actual.getVersion());
        assertTrue(actual.isEnabled());
        assertTrue(actual.isIncludeApis());
        assertEquals(3, actual.getRetentionPolicy().getDays());
    }

    @Test
    public void blobSignedIdentifierWithAccessPolicy() {
        BlobSignedIdentifier original = new BlobSignedIdentifier().setId("policy-1")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("rw")
                .setStartsOn(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setExpiresOn(OffsetDateTime.of(2020, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC)));

        BlobSignedIdentifier actual = roundTripChecked(original, BlobSignedIdentifier::fromXml);

        assertEquals("policy-1", actual.getId());
        assertEquals("rw", actual.getAccessPolicy().getPermissions());
        assertEquals(original.getAccessPolicy().getStartsOn(), actual.getAccessPolicy().getStartsOn());
        assertEquals(original.getAccessPolicy().getExpiresOn(), actual.getAccessPolicy().getExpiresOn());
    }

    @Test
    public void staticWebsite() {
        StaticWebsite original = new StaticWebsite().setEnabled(true)
            .setIndexDocument("index.html")
            .setErrorDocument404Path("404.html")
            .setDefaultIndexDocumentPath("default.html");

        StaticWebsite actual = roundTripChecked(original, StaticWebsite::fromXml);

        assertTrue(actual.isEnabled());
        assertEquals("index.html", actual.getIndexDocument());
        assertEquals("404.html", actual.getErrorDocument404Path());
        assertEquals("default.html", actual.getDefaultIndexDocumentPath());
    }

    @Test
    public void blockListKeepsBothSizeAccessors() {
        BlockList original
            = new BlockList().setCommittedBlocks(Arrays.asList(new Block().setName("YmxvY2sx").setSizeLong(1024L)))
                .setUncommittedBlocks(Arrays.asList(new Block().setName("YmxvY2sy").setSizeLong(2048L)));

        BlockList actual = roundTripChecked(original, BlockList::fromXml);

        assertEquals(1, actual.getCommittedBlocks().size());
        assertEquals("YmxvY2sx", actual.getCommittedBlocks().get(0).getName());
        assertEquals(1024L, actual.getCommittedBlocks().get(0).getSizeLong());
        // The deprecated int accessor is kept for compatibility and must agree with the long one.
        assertEquals(1024, actual.getCommittedBlocks().get(0).getSize());
        assertEquals(2048L, actual.getUncommittedBlocks().get(0).getSizeLong());
    }

    @Test
    public void blockLookupListWritesRepeatedElements() {
        BlockLookupList original = new BlockLookupList().setLatest(Arrays.asList("YmxvY2sx", "YmxvY2sy"))
            .setCommitted(Arrays.asList("YmxvY2sz"));

        BlockLookupList actual = roundTripChecked(original, BlockLookupList::fromXml);

        assertEquals(Arrays.asList("YmxvY2sx", "YmxvY2sy"), actual.getLatest());
        assertEquals(Arrays.asList("YmxvY2sz"), actual.getCommitted());
    }

    @Test
    public void pageListWithRanges() {
        PageList original = new PageList();
        original.getPageRange().add(new PageRange().setStart(0L).setEnd(511L));
        original.getClearRange().add(new ClearRange().setStart(512L).setEnd(1023L));

        PageList actual = roundTripChecked(original, PageList::fromXml);

        assertEquals(1, actual.getPageRange().size());
        assertEquals(0L, actual.getPageRange().get(0).getStart());
        assertEquals(511L, actual.getPageRange().get(0).getEnd());
        assertEquals(512L, actual.getClearRange().get(0).getStart());
    }

    /**
     * BlobPrefix is no longer generated -- the spec now emits an internal model carrying the BlobName so the
     * Encoded attribute survives, and this public one is maintained by hand. Nothing regenerates it, so its
     * deserialization is only covered here.
     * <p>
     * The shipped model is deliberately asymmetric: {@code fromXml} reads a {@code Name} child element, which is
     * what the service sends, while {@code toXml} writes the name as the element's own text. Nothing in the SDK
     * serializes a BlobPrefix, so that asymmetry is preserved rather than corrected, and the direction that is
     * actually exercised is the one asserted here.
     */
    @Test
    public void blobPrefixIsHandOwnedAndStillReadsTheServiceShape() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><BlobPrefix><Name>dir1/dir2/</Name></BlobPrefix>";

        BlobPrefix actual;
        try (XmlReader reader = XmlReader.fromBytes(xml.getBytes(StandardCharsets.UTF_8))) {
            actual = BlobPrefix.fromXml(reader);
        }

        assertEquals("dir1/dir2/", actual.getName());
    }

    @Test
    public void blobContainerItemWithPropertiesAndMetadata() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("owner", "storage");

        BlobContainerItem original = new BlobContainerItem().setName("container-1")
            .setDeleted(false)
            .setVersion("01D2C2A0")
            .setMetadata(metadata)
            .setProperties(new BlobContainerItemProperties().setETag("0x8D8")
                .setLastModified(OffsetDateTime.of(2020, 10, 21, 7, 28, 0, 0, ZoneOffset.UTC))
                .setLeaseStatus(LeaseStatusType.UNLOCKED)
                .setLeaseState(LeaseStateType.AVAILABLE)
                .setPublicAccess(PublicAccessType.CONTAINER)
                .setHasImmutabilityPolicy(false)
                .setHasLegalHold(false)
                .setDefaultEncryptionScope("$account-encryption-key")
                .setEncryptionScopeOverridePrevented(true));

        BlobContainerItem actual = roundTripChecked(original, BlobContainerItem::fromXml);

        assertEquals("container-1", actual.getName());
        assertEquals("01D2C2A0", actual.getVersion());
        assertEquals("storage", actual.getMetadata().get("owner"));
        assertEquals("0x8D8", actual.getProperties().getETag());
        assertEquals(LeaseStatusType.UNLOCKED, actual.getProperties().getLeaseStatus());
        assertEquals(PublicAccessType.CONTAINER, actual.getProperties().getPublicAccess());
        assertEquals(original.getProperties().getLastModified(), actual.getProperties().getLastModified());
    }

    /**
     * The shipped accessor is a primitive boolean rather than a Boolean, so an absent element has to read back as
     * false rather than throwing or widening the public API.
     */
    @Test
    public void containerPropertiesEncryptionScopeFlagStaysPrimitive() {
        BlobContainerItemProperties prevented = roundTripChecked(
            new BlobContainerItemProperties().setETag("0x8D8").setEncryptionScopeOverridePrevented(true),
            BlobContainerItemProperties::fromXml);
        assertTrue(prevented.isEncryptionScopeOverridePrevented());

        BlobContainerItemProperties absent = roundTripChecked(new BlobContainerItemProperties().setETag("0x8D8"),
            BlobContainerItemProperties::fromXml);
        assertFalse(absent.isEncryptionScopeOverridePrevented());
    }

    @Test
    public void keyInfo() {
        KeyInfo original = new KeyInfo().setStart("2020-01-01T00:00:00Z")
            .setExpiry("2020-01-02T00:00:00Z")
            .setDelegatedUserTenantId("tenant-1");

        KeyInfo actual = roundTripChecked(original, KeyInfo::fromXml);

        assertEquals("2020-01-01T00:00:00Z", actual.getStart());
        assertEquals("2020-01-02T00:00:00Z", actual.getExpiry());
        assertEquals("tenant-1", actual.getDelegatedUserTenantId());
    }
}
