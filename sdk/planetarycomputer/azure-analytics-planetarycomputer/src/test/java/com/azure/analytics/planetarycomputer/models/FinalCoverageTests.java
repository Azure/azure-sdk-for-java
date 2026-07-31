// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Final round of tests to push branch coverage above 30%.
 * Covers: {@link RenderOptionLegend}, {@link StacQueryable},
 * {@link OperationStatusHistoryItem}, {@link ErrorInfo}, {@link IngestionSource}.
 */
public final class FinalCoverageTests {

    // ==================== RenderOptionLegend ====================

    @Test
    public void testRenderOptionLegendDeserializeAllFields() throws Exception {
        RenderOptionLegend model = BinaryData
            .fromString("{\"type\":\"continuous\",\"labels\":[\"Low\",\"High\"],"
                + "\"trimStart\":10,\"trimEnd\":90,\"scaleFactor\":2.5," + "\"unknownField\":123}")
            .toObject(RenderOptionLegend.class);
        Assertions.assertNotNull(model.getType());
        Assertions.assertEquals(2, model.getLabels().size());
        Assertions.assertEquals("Low", model.getLabels().get(0));
        Assertions.assertEquals(10, model.getTrimStart());
        Assertions.assertEquals(90, model.getTrimEnd());
        Assertions.assertEquals(2.5, model.getScaleFactor());
    }

    @Test
    public void testRenderOptionLegendRoundTrip() throws Exception {
        RenderOptionLegend model = new RenderOptionLegend().setType(LegendConfigType.fromString("classmap"))
            .setLabels(Arrays.asList("Water", "Forest", "Urban"))
            .setTrimStart(5)
            .setTrimEnd(95)
            .setScaleFactor(1.0);
        model = BinaryData.fromObject(model).toObject(RenderOptionLegend.class);
        Assertions.assertEquals("classmap", model.getType().toString());
        Assertions.assertEquals(3, model.getLabels().size());
        Assertions.assertEquals(5, model.getTrimStart());
        Assertions.assertEquals(95, model.getTrimEnd());
        Assertions.assertEquals(1.0, model.getScaleFactor());
    }

    @Test
    public void testRenderOptionLegendMinimal() throws Exception {
        RenderOptionLegend model = BinaryData.fromString("{}").toObject(RenderOptionLegend.class);
        Assertions.assertNull(model.getType());
        Assertions.assertNull(model.getLabels());
        Assertions.assertNull(model.getTrimStart());
        Assertions.assertNull(model.getTrimEnd());
        Assertions.assertNull(model.getScaleFactor());
    }

    // ==================== StacQueryable ====================

    @Test
    public void testStacQueryableDeserializeAllFields() throws Exception {
        StacQueryable model = BinaryData
            .fromString(
                "{\"name\":\"cloud_cover\"," + "\"definition\":{\"type\":\"number\",\"minimum\":0,\"maximum\":100},"
                    + "\"create_index\":true," + "\"data_type\":\"float\"," + "\"unknownField\":\"skip\"}")
            .toObject(StacQueryable.class);
        Assertions.assertEquals("cloud_cover", model.getName());
        Assertions.assertNotNull(model.getDefinition());
        Assertions.assertEquals(true, model.isCreateIndex());
        Assertions.assertNotNull(model.getDataType());
    }

    @Test
    public void testStacQueryableRoundTrip() throws Exception {
        Map<String, Object> def = new HashMap<>();
        def.put("type", "string");
        StacQueryable model = new StacQueryable("platform", def).setCreateIndex(false)
            .setDataType(StacQueryableDefinitionDataType.fromString("string"));
        model = BinaryData.fromObject(model).toObject(StacQueryable.class);
        Assertions.assertEquals("platform", model.getName());
        Assertions.assertNotNull(model.getDefinition());
        Assertions.assertEquals(false, model.isCreateIndex());
    }

    @Test
    public void testStacQueryableNullOptionals() throws Exception {
        StacQueryable model = BinaryData
            .fromString("{\"name\":\"test\",\"definition\":{\"type\":\"number\"}," + "\"create_index\":null}")
            .toObject(StacQueryable.class);
        Assertions.assertEquals("test", model.getName());
        Assertions.assertNull(model.isCreateIndex());
        Assertions.assertNull(model.getDataType());
    }

    // ==================== OperationStatusHistoryItem ====================

    @Test
    public void testOperationStatusHistoryItemAllFields() throws Exception {
        OperationStatusHistoryItem model = BinaryData
            .fromString("{\"timestamp\":\"2021-06-15T14:30:00Z\"," + "\"status\":\"Succeeded\","
                + "\"errorCode\":\"E001\"," + "\"errorMessage\":\"Something failed\"}")
            .toObject(OperationStatusHistoryItem.class);
        Assertions.assertNotNull(model.getTimestamp());
        Assertions.assertEquals(OperationStatus.fromString("Succeeded"), model.getStatus());
        Assertions.assertEquals("E001", model.getErrorCode());
        Assertions.assertEquals("Something failed", model.getErrorMessage());
    }

    @Test
    public void testOperationStatusHistoryItemMinimal() throws Exception {
        OperationStatusHistoryItem model
            = BinaryData.fromString("{\"timestamp\":null,\"status\":\"Running\"," + "\"unknownField\":\"skip\"}")
                .toObject(OperationStatusHistoryItem.class);
        Assertions.assertNull(model.getTimestamp());
        Assertions.assertNull(model.getErrorCode());
        Assertions.assertNull(model.getErrorMessage());
    }

    // ==================== ErrorInfo ====================

    @Test
    public void testErrorInfoDeserialize() throws Exception {
        ErrorInfo model = BinaryData
            .fromString(
                "{\"error\":{\"code\":\"InvalidRequest\",\"message\":\"Bad data\"}," + "\"unknownField\":\"skip\"}")
            .toObject(ErrorInfo.class);
        Assertions.assertNotNull(model.getError());
        Assertions.assertEquals("InvalidRequest", model.getError().getCode());
        Assertions.assertEquals("Bad data", model.getError().getMessage());
    }

    @Test
    public void testErrorInfoMinimal() throws Exception {
        ErrorInfo model = BinaryData.fromString("{}").toObject(ErrorInfo.class);
        Assertions.assertNull(model.getError());
    }

    // ==================== IngestionSource discriminator ====================

    @Test
    public void testIngestionSourceUnknownKind() throws Exception {
        IngestionSource model
            = BinaryData
                .fromString("{\"id\":\"src-001\"," + "\"kind\":\"UnknownType\","
                    + "\"created\":\"2021-06-15T14:30:00Z\"," + "\"unknownField\":\"skip\"}")
                .toObject(IngestionSource.class);
        Assertions.assertEquals("src-001", model.getId());
        Assertions.assertNotNull(model.getKind());
        Assertions.assertNotNull(model.getCreated());
    }

    @Test
    public void testIngestionSourceNullCreated() throws Exception {
        IngestionSource model
            = BinaryData.fromString("{\"id\":\"src-002\"," + "\"kind\":\"SasToken\"," + "\"created\":null}")
                .toObject(IngestionSource.class);
        Assertions.assertEquals("src-002", model.getId());
        Assertions.assertNull(model.getCreated());
    }

    // ==================== Additional nullable tests for StacExtensionExtent ====================

    @Test
    public void testStacExtensionExtentDeserialize() throws Exception {
        StacExtensionExtent model
            = BinaryData
                .fromString("{\"spatial\":{\"bbox\":[[-180,-90,180,90]]},"
                    + "\"temporal\":{\"interval\":[[\"2015-06-27T00:00:00Z\",null]]}}")
                .toObject(StacExtensionExtent.class);
        Assertions.assertNotNull(model.getSpatial());
        Assertions.assertNotNull(model.getTemporal());
    }

    // ==================== StacSortExtension ====================

    @Test
    public void testStacSortExtensionDeserialize() throws Exception {
        StacSortExtension model = BinaryData.fromString("{\"field\":\"datetime\",\"direction\":\"desc\"}")
            .toObject(StacSortExtension.class);
        Assertions.assertEquals("datetime", model.getField());
        Assertions.assertNotNull(model.getDirection());
    }

    @Test
    public void testStacSortExtensionMinimal() throws Exception {
        StacSortExtension model
            = BinaryData.fromString("{\"field\":\"id\",\"unknownProp\":\"skip\"}").toObject(StacSortExtension.class);
        Assertions.assertEquals("id", model.getField());
        Assertions.assertNull(model.getDirection());
    }

    // ==================== SearchOptionsFields ====================

    @Test
    public void testSearchOptionsFieldsDeserialize() throws Exception {
        SearchOptionsFields model
            = BinaryData.fromString("{\"include\":[\"id\",\"geometry\"]," + "\"exclude\":[\"properties.datetime\"],"
                + "\"unknownProp\":\"skip\"}").toObject(SearchOptionsFields.class);
        Assertions.assertNotNull(model.getInclude());
        Assertions.assertEquals(2, model.getInclude().size());
        Assertions.assertNotNull(model.getExclude());
        Assertions.assertEquals(1, model.getExclude().size());
    }

    @Test
    public void testSearchOptionsFieldsMinimal() throws Exception {
        SearchOptionsFields model = BinaryData.fromString("{}").toObject(SearchOptionsFields.class);
        Assertions.assertNull(model.getInclude());
        Assertions.assertNull(model.getExclude());
    }
}
