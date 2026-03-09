// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Additional targeted tests to improve branch coverage for serialization/deserialization
 * paths in models: {@link StacItemOrStacItemCollection}, {@link Operation},
 * {@link IngestionDefinition}, {@link StacSearchParameters}, {@link StacCollection},
 * {@link StacProvider}, and nullable-field handling.
 */
public final class BranchCoverageTests {

    // ==================== StacItemOrStacItemCollection discriminator paths ====================

    @Test
    public void testStacItemOrStacItemCollectionFeatureDiscriminator() throws Exception {
        // "type":"Feature" discriminator path
        StacItemOrStacItemCollection model
            = BinaryData
                .fromString("{\"type\":\"Feature\",\"stac_version\":\"1.0.0\","
                    + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"self\"}],"
                    + "\"stac_extensions\":[\"eo\"]," + "\"msft:_created\":\"2021-06-15T14:30:00Z\","
                    + "\"msft:_updated\":\"2021-07-01T10:00:00Z\"," + "\"msft:short_description\":\"Test feature\"}")
                .toObject(StacItemOrStacItemCollection.class);
        Assertions.assertTrue(model instanceof StacItem);
    }

    @Test
    public void testStacItemOrStacItemCollectionFeatureCollectionDiscriminator() throws Exception {
        // "type":"FeatureCollection" discriminator path
        StacItemOrStacItemCollection model = BinaryData
            .fromString("{\"type\":\"FeatureCollection\",\"stac_version\":\"1.0.0\","
                + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"self\"}]," + "\"features\":[]}")
            .toObject(StacItemOrStacItemCollection.class);
        Assertions.assertTrue(model instanceof StacItemCollection);
    }

    @Test
    public void testStacItemOrStacItemCollectionUnknownDiscriminator() throws Exception {
        // Unknown type discriminator → fromJsonKnownDiscriminator fallback
        StacItemOrStacItemCollection model = BinaryData
            .fromString("{\"type\":\"SomethingElse\",\"stac_version\":\"1.0.0\","
                + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"self\"}],"
                + "\"stac_extensions\":[\"eo\",\"sar\"]," + "\"msft:_created\":\"2021-06-15T14:30:00Z\","
                + "\"msft:_updated\":null," + "\"msft:short_description\":\"Unknown type\"}")
            .toObject(StacItemOrStacItemCollection.class);
        Assertions.assertNotNull(model.getStacVersion());
        Assertions.assertNotNull(model.getLinks());
        Assertions.assertNotNull(model.getStacExtensions());
        Assertions.assertNotNull(model.getCreatedOn());
        Assertions.assertNull(model.getUpdatedOn());
        Assertions.assertEquals("Unknown type", model.getShortDescription());
    }

    @Test
    public void testStacItemOrStacItemCollectionNullDates() throws Exception {
        // Nullable date fields with explicit null
        StacItemOrStacItemCollection model = BinaryData
            .fromString("{\"type\":\"SomethingElse\",\"stac_version\":\"1.0.0\"," + "\"links\":[],"
                + "\"msft:_created\":null," + "\"msft:_updated\":null," + "\"unknownField\":\"ignored\"}")
            .toObject(StacItemOrStacItemCollection.class);
        Assertions.assertNull(model.getCreatedOn());
        Assertions.assertNull(model.getUpdatedOn());
    }

    // ==================== Operation (immutable) ====================

    @Test
    public void testOperationDeserializeAllFields() throws Exception {
        Operation model = BinaryData.fromString("{\"id\":\"op-123\",\"status\":\"Running\",\"type\":\"import\","
            + "\"creationTime\":\"2021-06-15T14:30:00Z\","
            + "\"statusHistory\":[{\"status\":\"Created\",\"timestamp\":\"2021-06-15T14:00:00Z\"}],"
            + "\"collectionId\":\"sentinel-2-l2a\"," + "\"startTime\":\"2021-06-15T14:30:00Z\","
            + "\"finishTime\":\"2021-06-15T15:00:00Z\","
            + "\"additionalInformation\":{\"reason\":\"re-import\",\"source\":\"catalog\"},"
            + "\"error\":{\"code\":\"E001\",\"message\":\"Something went wrong\"}}").toObject(Operation.class);
        Assertions.assertEquals("op-123", model.getId());
        Assertions.assertEquals(OperationStatus.RUNNING, model.getStatus());
        Assertions.assertEquals("import", model.getType());
        Assertions.assertNotNull(model.getCreationTime());
        Assertions.assertNotNull(model.getStatusHistory());
        Assertions.assertEquals(1, model.getStatusHistory().size());
        Assertions.assertEquals("sentinel-2-l2a", model.getCollectionId());
        Assertions.assertNotNull(model.getStartTime());
        Assertions.assertNotNull(model.getFinishTime());
        Assertions.assertNotNull(model.getAdditionalInformation());
        Assertions.assertEquals("re-import", model.getAdditionalInformation().get("reason"));
        Assertions.assertNotNull(model.getError());
    }

    @Test
    public void testOperationNullableDateFields() throws Exception {
        Operation model = BinaryData.fromString("{\"id\":\"op-456\",\"status\":\"Succeeded\",\"type\":\"export\","
            + "\"creationTime\":null," + "\"statusHistory\":[]," + "\"startTime\":null," + "\"finishTime\":null,"
            + "\"unknownField\":\"skip\"}").toObject(Operation.class);
        Assertions.assertEquals("op-456", model.getId());
        Assertions.assertNull(model.getCreationTime());
        Assertions.assertNull(model.getStartTime());
        Assertions.assertNull(model.getFinishTime());
        Assertions.assertNull(model.getCollectionId());
        Assertions.assertNull(model.getAdditionalInformation());
        Assertions.assertNull(model.getError());
    }

    // ==================== IngestionDefinition ====================

    @Test
    public void testIngestionDefinitionAllFieldsWithNulls() throws Exception {
        IngestionDefinition model = BinaryData.fromString("{\"id\":\"ingest-001\","
            + "\"creationTime\":\"2021-06-15T14:30:00Z\"," + "\"status\":\"Running\"," + "\"importType\":\"full\","
            + "\"displayName\":\"Test Ingestion\"," + "\"sourceCatalogUrl\":\"https://example.com/catalog\","
            + "\"skipExistingItems\":true," + "\"keepOriginalAssets\":false}").toObject(IngestionDefinition.class);
        Assertions.assertEquals("ingest-001", model.getId());
        Assertions.assertNotNull(model.getCreationTime());
        Assertions.assertEquals(IngestionStatus.fromString("Running"), model.getStatus());
        Assertions.assertEquals("full", model.getImportType().toString());
        Assertions.assertEquals("Test Ingestion", model.getDisplayName());
        Assertions.assertEquals("https://example.com/catalog", model.getSourceCatalogUrl());
        Assertions.assertEquals(true, model.isSkipExistingItems());
        Assertions.assertEquals(false, model.isKeepOriginalAssets());
    }

    @Test
    public void testIngestionDefinitionNullBooleans() throws Exception {
        IngestionDefinition model = BinaryData.fromString("{\"id\":\"ingest-002\"," + "\"creationTime\":null,"
            + "\"status\":\"Succeeded\"," + "\"importType\":\"incremental\"," + "\"displayName\":\"Null Test\","
            + "\"sourceCatalogUrl\":\"https://example.com\"," + "\"skipExistingItems\":null,"
            + "\"keepOriginalAssets\":null}").toObject(IngestionDefinition.class);
        Assertions.assertNull(model.getCreationTime());
        Assertions.assertNull(model.isSkipExistingItems());
        Assertions.assertNull(model.isKeepOriginalAssets());
    }

    // ==================== StacSearchParameters (all fields) ====================

    @Test
    public void testStacSearchParametersAllFields() throws Exception {
        StacSearchParameters model
            = BinaryData.fromString("{\"collections\":[\"sentinel-2-l2a\"]," + "\"ids\":[\"item-1\"],"
                + "\"bbox\":[-180,-90,180,90]," + "\"intersects\":{\"type\":\"Point\",\"coordinates\":[-73.99,40.71]},"
                + "\"datetime\":\"2021-01-01T00:00:00Z/2021-12-31T23:59:59Z\"," + "\"limit\":10,"
                + "\"conf\":{\"maxWait\":30}," + "\"query\":{\"cloud_cover\":{\"lt\":20}},"
                + "\"sortby\":[{\"field\":\"datetime\",\"direction\":\"desc\"}],"
                + "\"fields\":[{\"include\":[\"id\",\"geometry\"],\"exclude\":[\"properties.eo:cloud_cover\"]}],"
                + "\"filter\":{\"op\":\"<=\",\"args\":[{\"property\":\"cloud_cover\"},20]},"
                + "\"filter-crs\":\"EPSG:4326\"," + "\"filter-lang\":\"cql2-json\"," + "\"token\":\"next:abc123\","
                + "\"unknownField\":\"skip\"}").toObject(StacSearchParameters.class);
        Assertions.assertEquals(1, model.getCollections().size());
        Assertions.assertEquals(1, model.getIds().size());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertNotNull(model.getIntersects());
        Assertions.assertEquals("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z", model.getDatetime());
        Assertions.assertEquals(10, model.getLimit());
        Assertions.assertNotNull(model.getConformanceClass());
        Assertions.assertNotNull(model.getQuery());
        Assertions.assertNotNull(model.getSortBy());
        Assertions.assertNotNull(model.getFields());
        Assertions.assertNotNull(model.getFilter());
        Assertions.assertEquals("EPSG:4326", model.getFilterCoordinateReferenceSystem());
        Assertions.assertNotNull(model.getFilterLang());
        Assertions.assertEquals("next:abc123", model.getToken());
    }

    @Test
    public void testStacSearchParametersNullableLimit() throws Exception {
        StacSearchParameters model = BinaryData.fromString("{\"collections\":[\"sentinel-2-l2a\"],\"limit\":null}")
            .toObject(StacSearchParameters.class);
        Assertions.assertNull(model.getLimit());
    }

    // ==================== StacCollection (additionalProperties branch) ====================

    @Test
    public void testStacCollectionWithAllFieldsAndAdditionalProperties() throws Exception {
        StacCollection model = BinaryData.fromString("{\"id\":\"sentinel-2-l2a\","
            + "\"description\":\"Sentinel-2 Level-2A\","
            + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"self\"}]," + "\"license\":\"proprietary\","
            + "\"extent\":{\"spatial\":{\"bbox\":[[-180,-90,180,90]]},\"temporal\":{\"interval\":[[\"2015-06-27T00:00:00Z\",null]]}},"
            + "\"msft:_created\":\"2021-06-15T14:30:00Z\"," + "\"msft:_updated\":null,"
            + "\"msft:short_description\":\"Sentinel imagery\"," + "\"stac_extensions\":[\"eo\",\"sat\"],"
            + "\"stac_version\":\"1.0.0\"," + "\"title\":\"Sentinel-2\"," + "\"type\":\"Collection\","
            + "\"assets\":{\"thumbnail\":{\"title\":\"Thumbnail\",\"type\":\"image/png\"}},"
            + "\"item_assets\":{\"B04\":{\"title\":\"Red\",\"type\":\"image/tiff\"}},"
            + "\"keywords\":[\"satellite\",\"earth\"]," + "\"providers\":[{\"name\":\"ESA\",\"roles\":[\"producer\"]}],"
            + "\"summaries\":{\"constellation\":[\"sentinel-2\"]}," + "\"customProp\":\"additionalValue\"}")
            .toObject(StacCollection.class);
        Assertions.assertEquals("sentinel-2-l2a", model.getId());
        Assertions.assertEquals("Sentinel-2 Level-2A", model.getDescription());
        Assertions.assertNotNull(model.getLinks());
        Assertions.assertEquals("proprietary", model.getLicense());
        Assertions.assertNotNull(model.getExtent());
        Assertions.assertNotNull(model.getCreatedOn());
        Assertions.assertNull(model.getUpdatedOn());
        Assertions.assertEquals("Sentinel imagery", model.getShortDescription());
        Assertions.assertNotNull(model.getStacExtensions());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("Sentinel-2", model.getTitle());
        Assertions.assertEquals("Collection", model.getType());
        Assertions.assertNotNull(model.getAssets());
        Assertions.assertNotNull(model.getItemAssets());
        Assertions.assertEquals(2, model.getKeywords().size());
        Assertions.assertNotNull(model.getProviders());
        Assertions.assertNotNull(model.getSummaries());
    }

    @Test
    public void testStacCollectionNullDates() throws Exception {
        StacCollection model = BinaryData.fromString("{\"description\":\"Test\","
            + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"self\"}]," + "\"license\":\"MIT\","
            + "\"extent\":{\"spatial\":{\"bbox\":[[-180,-90,180,90]]},\"temporal\":{\"interval\":[[null,null]]}},"
            + "\"msft:_created\":null," + "\"msft:_updated\":null}").toObject(StacCollection.class);
        Assertions.assertNull(model.getCreatedOn());
        Assertions.assertNull(model.getUpdatedOn());
    }

    // ==================== StacProvider (roles field) ====================

    @Test
    public void testStacProviderAllFields() throws Exception {
        StacProvider model = BinaryData
            .fromString("{\"name\":\"ESA\",\"description\":\"European Space Agency\","
                + "\"roles\":[\"producer\",\"licensor\"]," + "\"url\":\"https://esa.int\"}")
            .toObject(StacProvider.class);
        Assertions.assertEquals("ESA", model.getName());
        Assertions.assertEquals("European Space Agency", model.getDescription());
        Assertions.assertNotNull(model.getRoles());
        Assertions.assertEquals(2, model.getRoles().size());
        Assertions.assertEquals("producer", model.getRoles().get(0));
        Assertions.assertEquals("https://esa.int", model.getUrl());
    }

    // ==================== StacAsset nullable/additional branches ====================

    @Test
    public void testStacAssetAllFieldsWithNulls() throws Exception {
        StacAsset model = BinaryData.fromString("{\"href\":\"https://example.com/asset.tif\","
            + "\"title\":\"Red Band\"," + "\"description\":\"Band 4 red\"," + "\"type\":\"image/tiff\","
            + "\"roles\":[\"data\",\"reflectance\"]," + "\"table:storage_options\":{\"account_name\":\"test\"},"
            + "\"xarray:storage_options\":{\"token\":\"anon\"}," + "\"xarray:open_kwargs\":{\"engine\":\"zarr\"},"
            + "\"msft:expiry\":\"2025-01-01T00:00:00Z\"," + "\"unknownProp\":\"skip\"}").toObject(StacAsset.class);
        Assertions.assertEquals("https://example.com/asset.tif", model.getHref());
        Assertions.assertEquals("Red Band", model.getTitle());
        Assertions.assertEquals("Band 4 red", model.getDescription());
        Assertions.assertEquals("image/tiff", model.getType());
        Assertions.assertNotNull(model.getRoles());
        Assertions.assertEquals(2, model.getRoles().size());
        Assertions.assertNotNull(model.getAdditionalProperties());
        Assertions.assertNotNull(model.getAdditionalProperties().get("table:storage_options"));
        Assertions.assertNotNull(model.getAdditionalProperties().get("xarray:storage_options"));
        Assertions.assertNotNull(model.getAdditionalProperties().get("xarray:open_kwargs"));
        Assertions.assertNotNull(model.getAdditionalProperties().get("msft:expiry"));
    }

    @Test
    public void testStacAssetNullableExpiry() throws Exception {
        StacAsset model
            = BinaryData.fromString("{\"href\":\"https://example.com/asset.tif\"," + "\"msft:expiry\":null}")
                .toObject(StacAsset.class);
        // msft:expiry goes into additionalProperties as null
        Assertions.assertTrue(
            model.getAdditionalProperties() == null || model.getAdditionalProperties().get("msft:expiry") == null);
    }

    // ==================== StacItem all fields ====================

    @Test
    public void testStacItemAllFields() throws Exception {
        StacItem model = BinaryData.fromString("{\"type\":\"Feature\"," + "\"stac_version\":\"1.0.0\","
            + "\"stac_extensions\":[\"eo\"]," + "\"id\":\"item-001\","
            + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-73.99,40.71]}," + "\"bbox\":[-74,40,-73,41],"
            + "\"properties\":{\"datetime\":\"2021-06-15T14:30:00Z\"},"
            + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"self\"}],"
            + "\"assets\":{\"B04\":{\"href\":\"https://example.com/B04.tif\",\"title\":\"Red\",\"type\":\"image/tiff\"}},"
            + "\"collection\":\"sentinel-2-l2a\"," + "\"msft:_created\":\"2021-06-15T14:30:00Z\","
            + "\"msft:_updated\":\"2021-07-01T10:00:00Z\"," + "\"msft:short_description\":\"Test item\"}")
            .toObject(StacItem.class);
        Assertions.assertEquals("item-001", model.getId());
        Assertions.assertNotNull(model.getGeometry());
        Assertions.assertNotNull(model.getBoundingBox());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertNotNull(model.getProperties());
        Assertions.assertNotNull(model.getAssets());
        Assertions.assertEquals("sentinel-2-l2a", model.getCollection());
        Assertions.assertNotNull(model.getCreatedOn());
        Assertions.assertNotNull(model.getUpdatedOn());
        Assertions.assertEquals("Test item", model.getShortDescription());
    }

    @Test
    public void testStacItemNullDates() throws Exception {
        StacItem model
            = BinaryData
                .fromString("{\"type\":\"Feature\"," + "\"stac_version\":\"1.0.0\"," + "\"links\":[],"
                    + "\"msft:_created\":null," + "\"msft:_updated\":null}")
                .toObject(StacItem.class);
        Assertions.assertNull(model.getCreatedOn());
        Assertions.assertNull(model.getUpdatedOn());
    }

    // ==================== StacItemCollection all fields ====================

    @Test
    public void testStacItemCollectionAllFields() throws Exception {
        StacItemCollection model = BinaryData.fromString(
            "{\"type\":\"FeatureCollection\"," + "\"stac_version\":\"1.0.0\"," + "\"stac_extensions\":[\"context\"],"
                + "\"features\":[{\"type\":\"Feature\",\"stac_version\":\"1.0.0\","
                + "\"links\":[],\"id\":\"i1\",\"properties\":{\"datetime\":\"2021-06-15T14:30:00Z\"}}],"
                + "\"links\":[{\"href\":\"http://example.com\",\"rel\":\"next\"}]," + "\"numberReturned\":1,"
                + "\"numberMatched\":100," + "\"context\":{\"returned\":1,\"matched\":100},"
                + "\"msft:_created\":\"2021-06-15T14:30:00Z\"," + "\"msft:_updated\":\"2021-07-01T10:00:00Z\","
                + "\"msft:short_description\":\"Test collection\"}")
            .toObject(StacItemCollection.class);
        Assertions.assertNotNull(model.getFeatures());
        Assertions.assertEquals(1, model.getFeatures().size());
        Assertions.assertNotNull(model.getContext());
        Assertions.assertNotNull(model.getCreatedOn());
        Assertions.assertNotNull(model.getUpdatedOn());
    }

    @Test
    public void testStacItemCollectionNullableFields() throws Exception {
        StacItemCollection model
            = BinaryData.fromString("{\"type\":\"FeatureCollection\"," + "\"stac_version\":\"1.0.0\","
                + "\"features\":[]," + "\"links\":[]," + "\"numberReturned\":null," + "\"numberMatched\":null,"
                + "\"msft:_created\":null," + "\"msft:_updated\":null}").toObject(StacItemCollection.class);
        Assertions.assertNull(model.getCreatedOn());
        Assertions.assertNull(model.getUpdatedOn());
    }

    // ==================== StacLink all fields ====================

    @Test
    public void testStacLinkAllFields() throws Exception {
        StacLink model = BinaryData
            .fromString("{\"href\":\"http://example.com\"," + "\"rel\":\"self\"," + "\"type\":\"application/json\","
                + "\"title\":\"Self Link\"," + "\"method\":\"GET\"," + "\"headers\":{\"Accept\":\"application/json\"},"
                + "\"body\":{\"param\":\"value\"}," + "\"merge\":true," + "\"unknownField\":\"skip\"}")
            .toObject(StacLink.class);
        Assertions.assertEquals("http://example.com", model.getHref());
        Assertions.assertEquals("self", model.getRel());
        Assertions.assertEquals("application/json", model.getType().toString());
        Assertions.assertEquals("Self Link", model.getTitle());
        Assertions.assertEquals("GET", model.getMethod().toString());
        Assertions.assertNotNull(model.getHeaders());
        Assertions.assertNotNull(model.getBody());
        Assertions.assertEquals(true, model.isMerge());
    }

    @Test
    public void testStacLinkNullableMerge() throws Exception {
        StacLink model
            = BinaryData.fromString("{\"href\":\"http://example.com\"," + "\"rel\":\"self\"," + "\"merge\":null}")
                .toObject(StacLink.class);
        Assertions.assertNull(model.isMerge());
    }

    // ==================== StacItemProperties all fields ====================

    @Test
    public void testStacItemPropertiesAllFields() throws Exception {
        StacItemProperties model = BinaryData
            .fromString("{\"datetime\":\"2021-06-15T14:30:00Z\"," + "\"start_datetime\":\"2021-06-15T14:00:00Z\","
                + "\"end_datetime\":\"2021-06-15T15:00:00Z\"," + "\"created\":\"2021-06-10T10:00:00Z\","
                + "\"updated\":\"2021-06-20T10:00:00Z\"," + "\"title\":\"Test Properties\","
                + "\"description\":\"A test item\"," + "\"license\":\"MIT\"," + "\"providers\":[{\"name\":\"Test\"}],"
                + "\"platform\":\"Sentinel-2\"," + "\"instruments\":[\"MSI\"]," + "\"constellation\":\"sentinel-2\","
                + "\"mission\":\"sentinel\"," + "\"gsd\":10.0," + "\"unknownProp\":\"additionalValue\"}")
            .toObject(StacItemProperties.class);
        Assertions.assertNotNull(model.getDatetime());
        Assertions.assertNotNull(model.getStartDatetime());
        Assertions.assertNotNull(model.getEndDatetime());
        Assertions.assertNotNull(model.getCreated());
        Assertions.assertNotNull(model.getUpdated());
        Assertions.assertEquals("Test Properties", model.getTitle());
        Assertions.assertEquals("A test item", model.getDescription());
        // license goes into additionalProperties
        Assertions.assertNotNull(model.getAdditionalProperties());
        Assertions.assertNotNull(model.getProviders());
        Assertions.assertEquals("Sentinel-2", model.getPlatform());
        Assertions.assertNotNull(model.getInstruments());
        Assertions.assertEquals("sentinel-2", model.getConstellation());
        Assertions.assertEquals("sentinel", model.getMission());
        Assertions.assertEquals(10.0, model.getGsd());
    }

    @Test
    public void testStacItemPropertiesNullableDateFields() throws Exception {
        StacItemProperties model
            = BinaryData
                .fromString("{\"datetime\":null," + "\"start_datetime\":null," + "\"end_datetime\":null,"
                    + "\"created\":null," + "\"updated\":null," + "\"gsd\":null}")
                .toObject(StacItemProperties.class);
        Assertions.assertNull(model.getDatetime());
        Assertions.assertNull(model.getStartDatetime());
        Assertions.assertNull(model.getEndDatetime());
        Assertions.assertNull(model.getCreated());
        Assertions.assertNull(model.getUpdated());
        Assertions.assertNull(model.getGsd());
    }
}
