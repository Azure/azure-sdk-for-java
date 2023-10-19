// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.CountPagedIterable;
import com.azure.analytics.defender.easm.models.DiscoTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscoveryTemplatesTest extends EasmClientTestBase {

    String templateId = "43488";
    String partialName = "ku";

    @Test
    public void testdiscoveryTemplatesListWithResponse() {

        CountPagedIterable<DiscoTemplate> discoTemplatePageResponse = easmClient.listDiscoTemplate(partialName, 0, 25);
        DiscoTemplate discoTemplateResponse = discoTemplatePageResponse.stream().iterator().next();
        assertTrue(discoTemplateResponse.getName().toLowerCase().contains(partialName));
        assertNotNull(discoTemplateResponse.getId());
    }

    @Test
    public void testdiscoveryTemplatesGetWithResponse() {
        DiscoTemplate discoTemplateResponse = easmClient.getDiscoTemplate(templateId);
        assertNotNull(discoTemplateResponse.getName());
        assertNotNull(discoTemplateResponse.getId());
    }


}
