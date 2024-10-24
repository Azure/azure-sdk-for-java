// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.SavedFilter;
import com.azure.analytics.defender.easm.models.SavedFilterData;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SavedFiltersAsyncTest extends EasmClientTestBase {
    String deleteSavedFilterName = "put_filter";
    String putSavedFilterName = "put_filter";
    String knownExistingFilter = "new_put_filter";
    String filter = "name = \"" + putSavedFilterName + "\"";

    @Test
    public void testSavedFiltersListAsync() {
        PagedFlux<SavedFilter> savedFilterPagedFlux = easmAsyncClient.listSavedFilter();
        List<SavedFilter> savedFilterList = new ArrayList<>();

        StepVerifier.create(savedFilterPagedFlux)
            .thenConsumeWhile(savedFilterList::add)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
        for (SavedFilter savedFilter : savedFilterList) {
            assertNotNull(savedFilter.getId());
            assertNotNull(savedFilter.getDescription());
        }
    }

    @Test
    public void testSavedFiltersGetAsync() {
        Mono<SavedFilter> savedFilterMono = easmAsyncClient.getSavedFilter(knownExistingFilter);
        StepVerifier.create(savedFilterMono).assertNext(savedFilter -> {
            assertEquals(knownExistingFilter, savedFilter.getId());
            assertEquals(knownExistingFilter, savedFilter.getName());
            assertNotNull(savedFilter.getDisplayName());
            assertNotNull(savedFilter.getFilter());
            assertNotNull(savedFilter.getDescription());
        }).expectComplete().verify(DEFAULT_TIMEOUT);
    }

    @Test
    public void testSavedFiltersCreateOrReplaceAsync() {
        SavedFilterData savedFilterData = new SavedFilterData(filter, "Sample description");
        Mono<SavedFilter> savedFilterMono
            = easmAsyncClient.createOrReplaceSavedFilter(putSavedFilterName, savedFilterData);
        StepVerifier.create(savedFilterMono).assertNext(savedFilter -> {
            assertEquals(putSavedFilterName, savedFilter.getName());
            assertEquals(putSavedFilterName, savedFilter.getId());
            assertEquals(putSavedFilterName, savedFilter.getDisplayName());
            assertEquals(savedFilterData.getDescription(), savedFilter.getDescription());
        }).expectComplete().verify(DEFAULT_TIMEOUT);

    }

    @Test
    public void testSavedFiltersDeleteAsync() {
        Mono<Void> deleteMono = easmAsyncClient.deleteSavedFilter(deleteSavedFilterName);
        StepVerifier.create(deleteMono).expectComplete().verify(DEFAULT_TIMEOUT);
    }
}
