// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.models.SavedFilterData;
import com.azure.analytics.defender.easm.models.SavedFilter;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SavedFiltersTest extends EasmClientTestBase {

    String deleteSavedFilterName = "put_filter";
    String putSavedFilterName = "put_filter";
    String knownExistingFilter = "new_put_filter";
    String filter = "name = \"" + putSavedFilterName + "\"";

    @Test
    public void testsavedFiltersListWithResponse() {
        PagedIterable<SavedFilter> savedFilters = easmClient.listSavedFilter();
        SavedFilter savedFilterResponse = savedFilters.stream().iterator().next();
        assertNotNull(savedFilterResponse.getId());
        assertNotNull(savedFilterResponse.getDescription());

    }

    @Test
    public void testsavedFiltersGetWithResponse() {
        SavedFilter savedFilterResponse = easmClient.getSavedFilter(knownExistingFilter);
        assertEquals(knownExistingFilter, savedFilterResponse.getId());
        assertEquals(knownExistingFilter, savedFilterResponse.getName());
        assertNotNull(savedFilterResponse.getDisplayName());
        assertNotNull(savedFilterResponse.getFilter());
        assertNotNull(savedFilterResponse.getDescription());
    }

    @Test
    public void testsavedFiltersPutWithResponse() {
        SavedFilterData savedFilterData = new SavedFilterData(filter, "Sample description");
        SavedFilter savedFilterResponse = easmClient.createOrReplaceSavedFilter(putSavedFilterName, savedFilterData);
        assertEquals(putSavedFilterName, savedFilterResponse.getName());
        assertEquals(putSavedFilterName, savedFilterResponse.getId());
        assertEquals(putSavedFilterName, savedFilterResponse.getDisplayName());
        assertEquals(savedFilterData.getDescription(), savedFilterResponse.getDescription());
    }

    @Test
    public void testsavedFiltersDeleteWithResponse() {
        easmClient.deleteSavedFilter(deleteSavedFilterName);
    }

}
