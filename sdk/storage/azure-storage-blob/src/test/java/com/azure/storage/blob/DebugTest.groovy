package com.azure.storage.blob

import com.azure.core.util.serializer.JacksonAdapter
import com.azure.core.util.serializer.SerializerEncoding
import com.azure.storage.blob.models.BlobServiceStatistics
import spock.lang.Specification

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.class DebugTest

class DebugTest extends Specification {
    def "last sync time xml"() {
        setup:
        def xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><StorageServiceStats><GeoReplication><Status>bootstrap</Status><LastSyncTime></LastSyncTime></GeoReplication></StorageServiceStats>"
        def jacksonAdapter = new JacksonAdapter()

        when:
        def statistics = jacksonAdapter.deserialize(xml, BlobServiceStatistics.class, SerializerEncoding.XML) as BlobServiceStatistics

        then:
        assert statistics.getGeoReplication().getLastSyncTime() == null
    }
}
