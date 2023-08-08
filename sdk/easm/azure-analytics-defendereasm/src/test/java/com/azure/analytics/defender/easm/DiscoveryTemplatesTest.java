package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.DiscoTemplate;
import com.azure.analytics.defender.easm.models.DiscoTemplatePageResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscoveryTemplatesTest extends EasmDefenderClientTestBase {

    String templateId = "43488";
    String partialName = "ku";

    @Test
    public void testdiscoveryTemplatesListWithResponse(){

        DiscoTemplatePageResponse discoTemplatePageResponse = discoveryTemplatesClient.list(partialName, 0, 25);
        DiscoTemplate discoTemplateResponse = discoTemplatePageResponse.getValue().get(0);
        assertTrue(discoTemplateResponse.getName().toLowerCase().contains(partialName));
        assertNotNull(discoTemplateResponse.getId());

    }

    @Test
    public void testdiscoveryTemplatesGetWithResponse(){
        DiscoTemplate discoTemplateResponse = discoveryTemplatesClient.get(templateId);
        assertNotNull(discoTemplateResponse.getName());
        assertNotNull(discoTemplateResponse.getId());
    }


}
