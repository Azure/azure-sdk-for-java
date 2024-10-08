// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.analytics.purview.datamap.generated;

import com.azure.analytics.purview.datamap.models.AtlasClassificationDef;
import com.azure.analytics.purview.datamap.models.AtlasEntityDef;
import com.azure.analytics.purview.datamap.models.AtlasEnumDef;
import com.azure.analytics.purview.datamap.models.AtlasEnumElementDef;
import com.azure.analytics.purview.datamap.models.AtlasRelationshipDef;
import com.azure.analytics.purview.datamap.models.AtlasStructDef;
import com.azure.analytics.purview.datamap.models.AtlasTypesDef;
import com.azure.analytics.purview.datamap.models.TypeCategory;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public final class TypeListEnumDefsTests extends DataMapClientTestBase {
    @Test
    @Disabled
    public void testTypeListEnumDefsTests() {
        // method invocation
        AtlasTypesDef response = typeDefinitionClient.get(null, TypeCategory.ENUM);

        // response assertion
        Assertions.assertNotNull(response);
        // verify property "classificationDefs"
        List<AtlasClassificationDef> responseClassificationDefs = response.getClassificationDefs();
        Assertions.assertEquals(0, responseClassificationDefs.size());
        // verify property "entityDefs"
        List<AtlasEntityDef> responseEntityDefs = response.getEntityDefs();
        Assertions.assertEquals(0, responseEntityDefs.size());
        // verify property "enumDefs"
        List<AtlasEnumDef> responseEnumDefs = response.getEnumDefs();
        AtlasEnumDef responseEnumDefsFirstItem = responseEnumDefs.iterator().next();
        Assertions.assertNotNull(responseEnumDefsFirstItem);
        Assertions.assertEquals(TypeCategory.ENUM, responseEnumDefsFirstItem.getCategory());
        Assertions.assertEquals(1604728877305L, responseEnumDefsFirstItem.getCreateTime());
        Assertions.assertEquals("ExampleCreator", responseEnumDefsFirstItem.getCreatedBy());
        Assertions.assertEquals("glossary_term_status_value", responseEnumDefsFirstItem.getDescription());
        Assertions.assertEquals("0ddc2fcf-ad17-4d06-984a-ffb2ffb2a941", responseEnumDefsFirstItem.getGuid());
        Assertions.assertEquals("glossary_term_status_value", responseEnumDefsFirstItem.getName());
        Assertions.assertEquals("1.0", responseEnumDefsFirstItem.getTypeVersion());
        Assertions.assertEquals(1604728877305L, responseEnumDefsFirstItem.getUpdateTime());
        Assertions.assertEquals("ExampleUpdator", responseEnumDefsFirstItem.getUpdatedBy());
        Assertions.assertEquals(1L, responseEnumDefsFirstItem.getVersion());
        Assertions.assertEquals("1", responseEnumDefsFirstItem.getLastModifiedTS());
        List<AtlasEnumElementDef> responseEnumDefsFirstItemElementDefs = responseEnumDefsFirstItem.getElementDefs();
        AtlasEnumElementDef responseEnumDefsFirstItemElementDefsFirstItem
            = responseEnumDefsFirstItemElementDefs.iterator().next();
        Assertions.assertNotNull(responseEnumDefsFirstItemElementDefsFirstItem);
        Assertions.assertEquals(0, responseEnumDefsFirstItemElementDefsFirstItem.getOrdinal());
        Assertions.assertEquals("Approved", responseEnumDefsFirstItemElementDefsFirstItem.getValue());
        // verify property "relationshipDefs"
        List<AtlasRelationshipDef> responseRelationshipDefs = response.getRelationshipDefs();
        Assertions.assertEquals(0, responseRelationshipDefs.size());
        // verify property "structDefs"
        List<AtlasStructDef> responseStructDefs = response.getStructDefs();
        Assertions.assertEquals(0, responseStructDefs.size());
    }
}
