// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmClientTestBase;
import com.azure.analytics.defender.easm.models.DiscoTemplate;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscoveryTemplatesAsyncTest extends EasmClientTestBase {
    String templateId = "43488";
    String partialName = "ku";

    @Test
    public void testDiscoveryTemplatesAsync() {
        PagedFlux<DiscoTemplate> discoTemplatePagedFlux = easmAsyncClient.listDiscoTemplate(partialName, 0);
        DiscoTemplate discoTemplate = discoTemplatePagedFlux.blockFirst();
        assertTrue(discoTemplate.getName().toLowerCase().contains(partialName));
        assertNotNull(discoTemplate.getId());
    }

    @Test
    public void testDiscoveryTemplateGetAsync() {
        Mono<DiscoTemplate> discoTemplateMono = easmAsyncClient.getDiscoTemplate(templateId);
        discoTemplateMono.subscribe(
            discoTemplate -> {
                assertNotNull(discoTemplate.getName());
                assertNotNull(discoTemplate.getId());
            }
        );
    }
}
