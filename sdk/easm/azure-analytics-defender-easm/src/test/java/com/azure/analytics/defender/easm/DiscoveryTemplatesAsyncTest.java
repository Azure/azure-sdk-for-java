// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.DiscoTemplate;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiscoveryTemplatesAsyncTest extends EasmClientTestBase {
    String templateId = "43488";
    String partialName = "sample";

    @Test
    public void testDiscoveryTemplatesListAsync() {
        PagedFlux<DiscoTemplate> discoTemplatePagedFlux = easmAsyncClient.listDiscoTemplate(partialName, 0);
        List<DiscoTemplate> discoTemplateList = new ArrayList<>();

        StepVerifier.create(discoTemplatePagedFlux)
            .thenConsumeWhile(discoTemplateList::add)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        assertTrue(discoTemplateList.size() > 1);
        for (DiscoTemplate discoTemplate : discoTemplateList) {
            if (!discoTemplate.getName().toLowerCase().contains(partialName)) {
                System.out.println("That name is: " + discoTemplate.getName().toLowerCase());
            }
            assertTrue(discoTemplate.getName().toLowerCase().contains(partialName));
            assertNotNull(discoTemplate.getId());
        }
    }

    @Test
    public void testDiscoveryTemplateGetAsync() {
        Mono<DiscoTemplate> discoTemplateMono = easmAsyncClient.getDiscoTemplate(templateId);
        StepVerifier.create(discoTemplateMono).assertNext(discoTemplate -> {
            assertNotNull(discoTemplate.getName());
            assertNotNull(discoTemplate.getId());
        });
    }
}
