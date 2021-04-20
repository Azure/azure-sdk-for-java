// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.FileHelpers;
import com.azure.digitaltwins.parser.GeneratedCodeCompareBase;
import com.azure.digitaltwins.parser.implementation.parsergen.MetamodelDigest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MetamodelDigestTests extends GeneratedCodeCompareBase {
    @Test
    public void readFullTreeTest() throws IOException {
        String fullDigest = FileHelpers.getFileContentsByFileName("", "digest.json");

        MetamodelDigest digest = new MetamodelDigest(fullDigest);
        Assertions.assertNotNull(digest.getContexts());
        Assertions.assertNotNull(digest.getDtdlVersions());
        Assertions.assertNotNull(digest.getBaseClass());
        Assertions.assertNotNull(digest.getPartitionClasses());
        Assertions.assertNotNull(digest.getRootableClasses());
        Assertions.assertNotNull(digest.getIdentifierDefinitionRestrictions());
        Assertions.assertNotNull(digest.getIdentifierReferenceRestrictions());
        Assertions.assertNotNull(digest.getClassIdentifierDefinitionRestrictions());
        Assertions.assertNotNull(digest.getExtensionKinds());
        Assertions.assertNotNull(digest.getExtensibleMaterialClasses());
        Assertions.assertNotNull(digest.getMaterialClasses());
        Assertions.assertNotNull(digest.getDescendantControls());
        Assertions.assertNotNull(digest.getSupplementalTypes());
        Assertions.assertNotNull(digest.getElementsJsonText());
        Assertions.assertNotNull(digest.getDtdlVersionsAllowingLocalTerms());
    }
}
