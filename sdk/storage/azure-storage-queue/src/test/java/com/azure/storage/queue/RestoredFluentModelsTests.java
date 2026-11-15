// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.storage.queue.models.GeoReplication;
import com.azure.storage.queue.models.GeoReplicationStatus;
import com.azure.storage.queue.models.QueueAnalyticsLogging;
import com.azure.storage.queue.models.QueueCorsRule;
import com.azure.storage.queue.models.QueueMetrics;
import com.azure.storage.queue.models.QueueRetentionPolicy;
import com.azure.storage.queue.models.QueueServiceStatistics;
import com.azure.storage.queue.models.UserDelegationKey;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trip (toXml -&gt; fromXml) coverage for the models whose {@code @Fluent} shape is restored by
 * {@code QueueStorageCustomizations.restoreFluentModels}. That customization rewrites the generated constructors and
 * the {@code fromXml} construction, so these tests pin that the rewrite still serializes and deserializes every
 * property faithfully.
 */
public class RestoredFluentModelsTests {

    @FunctionalInterface
    private interface XmlFactory<T> {
        T read(XmlReader reader) throws XMLStreamException;
    }

    private static <T extends XmlSerializable<T>> T roundTrip(T value, XmlFactory<T> factory)
        throws XMLStreamException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (XmlWriter writer = XmlWriter.toStream(stream)) {
            value.toXml(writer);
            writer.flush();
        }
        try (XmlReader reader = XmlReader.fromBytes(stream.toByteArray())) {
            return factory.read(reader);
        }
    }

    @Test
    public void userDelegationKeyRoundTrips() throws XMLStreamException {
        UserDelegationKey original = new UserDelegationKey().setSignedObjectId("objectId")
            .setSignedTenantId("tenantId")
            .setSignedStart(OffsetDateTime.of(2026, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC))
            .setSignedExpiry(OffsetDateTime.of(2026, 1, 3, 3, 4, 5, 0, ZoneOffset.UTC))
            .setSignedService("q")
            .setSignedVersion("2026-04-06")
            .setSignedDelegatedUserTenantId("delegatedTenantId")
            .setValue("aGVsbG8=");

        UserDelegationKey actual = roundTrip(original, UserDelegationKey::fromXml);

        assertEquals(original.getSignedObjectId(), actual.getSignedObjectId());
        assertEquals(original.getSignedTenantId(), actual.getSignedTenantId());
        assertEquals(original.getSignedStart(), actual.getSignedStart());
        assertEquals(original.getSignedExpiry(), actual.getSignedExpiry());
        assertEquals(original.getSignedService(), actual.getSignedService());
        assertEquals(original.getSignedVersion(), actual.getSignedVersion());
        assertEquals(original.getSignedDelegatedUserTenantId(), actual.getSignedDelegatedUserTenantId());
        assertEquals(original.getValue(), actual.getValue());
    }

    @Test
    public void geoReplicationRoundTrips() throws XMLStreamException {
        GeoReplication original = new GeoReplication().setStatus(GeoReplicationStatus.LIVE)
            .setLastSyncTime(OffsetDateTime.of(2026, 2, 3, 4, 5, 6, 0, ZoneOffset.UTC));

        GeoReplication actual = roundTrip(original, GeoReplication::fromXml);

        assertEquals(original.getStatus(), actual.getStatus());
        assertEquals(original.getLastSyncTime(), actual.getLastSyncTime());
    }

    @Test
    public void queueServiceStatisticsRoundTripsNestedGeoReplication() throws XMLStreamException {
        QueueServiceStatistics original = new QueueServiceStatistics()
            .setGeoReplication(new GeoReplication().setStatus(GeoReplicationStatus.BOOTSTRAP)
                .setLastSyncTime(OffsetDateTime.of(2026, 5, 6, 7, 8, 9, 0, ZoneOffset.UTC)));

        QueueServiceStatistics actual = roundTrip(original, QueueServiceStatistics::fromXml);

        assertEquals(original.getGeoReplication().getStatus(), actual.getGeoReplication().getStatus());
        assertEquals(original.getGeoReplication().getLastSyncTime(), actual.getGeoReplication().getLastSyncTime());
    }

    @Test
    public void queueMetricsRoundTripsNestedRetentionPolicy() throws XMLStreamException {
        QueueMetrics original = new QueueMetrics().setVersion("1.0")
            .setEnabled(true)
            .setIncludeApis(true)
            .setRetentionPolicy(new QueueRetentionPolicy().setEnabled(true).setDays(5));

        QueueMetrics actual = roundTrip(original, QueueMetrics::fromXml);

        assertEquals(original.getVersion(), actual.getVersion());
        assertEquals(original.isEnabled(), actual.isEnabled());
        assertEquals(original.isIncludeApis(), actual.isIncludeApis());
        assertEquals(original.getRetentionPolicy().isEnabled(), actual.getRetentionPolicy().isEnabled());
        assertEquals(original.getRetentionPolicy().getDays(), actual.getRetentionPolicy().getDays());
    }

    @Test
    public void queueAnalyticsLoggingRoundTrips() throws XMLStreamException {
        QueueAnalyticsLogging original = new QueueAnalyticsLogging().setVersion("1.0")
            .setDelete(true)
            .setRead(false)
            .setWrite(true)
            .setRetentionPolicy(new QueueRetentionPolicy().setEnabled(true).setDays(3));

        QueueAnalyticsLogging actual = roundTrip(original, QueueAnalyticsLogging::fromXml);

        assertEquals(original.getVersion(), actual.getVersion());
        assertEquals(original.isDelete(), actual.isDelete());
        assertEquals(original.isRead(), actual.isRead());
        assertEquals(original.isWrite(), actual.isWrite());
        assertEquals(original.getRetentionPolicy().getDays(), actual.getRetentionPolicy().getDays());
    }

    @Test
    public void queueCorsRuleRoundTrips() throws XMLStreamException {
        QueueCorsRule original = new QueueCorsRule().setAllowedOrigins("https://contoso.com")
            .setAllowedMethods("GET,PUT")
            .setAllowedHeaders("x-ms-meta-data*")
            .setExposedHeaders("x-ms-request-id")
            .setMaxAgeInSeconds(120);

        QueueCorsRule actual = roundTrip(original, QueueCorsRule::fromXml);

        assertEquals(original.getAllowedOrigins(), actual.getAllowedOrigins());
        assertEquals(original.getAllowedMethods(), actual.getAllowedMethods());
        assertEquals(original.getAllowedHeaders(), actual.getAllowedHeaders());
        assertEquals(original.getExposedHeaders(), actual.getExposedHeaders());
        assertEquals(original.getMaxAgeInSeconds(), actual.getMaxAgeInSeconds());
    }
}
