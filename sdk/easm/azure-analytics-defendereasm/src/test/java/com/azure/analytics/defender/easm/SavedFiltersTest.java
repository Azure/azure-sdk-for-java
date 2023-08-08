package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmDefenderClientTestBase;
import com.azure.analytics.defender.easm.models.SavedFilter;
import com.azure.analytics.defender.easm.models.SavedFilterData;
import com.azure.analytics.defender.easm.models.SavedFilterPageResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SavedFiltersTest extends EasmDefenderClientTestBase {

    String deleteSavedFilterName = "put_filter";
    String putSavedFilterName = "put_filter";
    String knownExistingFilter = "new_put_filter";
    String filter = "name = \"" + putSavedFilterName + "\"";

    @Test
    public void testsavedFiltersListWithResponse(){
        SavedFilterPageResponse savedFilterPageResponse = savedFiltersClient.list();
        SavedFilter savedFilterResponse = savedFilterPageResponse.getValue().get(0);
        assertNotNull(savedFilterResponse.getId());
        assertNotNull(savedFilterResponse.getDescription());
    }

    @Test
    public void testsavedFiltersGetWithResponse(){
        SavedFilter savedFilterResponse = savedFiltersClient.get(knownExistingFilter);
        assertEquals(knownExistingFilter, savedFilterResponse.getId());
        assertEquals(knownExistingFilter, savedFilterResponse.getName());
        assertNotNull(savedFilterResponse.getDisplayName());
        assertNotNull(savedFilterResponse.getFilter());
        assertNotNull(savedFilterResponse.getDescription());
    }

    @Test
    public void testsavedFiltersPutWithResponse(){
        SavedFilterData savedFilterRequest = new SavedFilterData(filter, "Sample description");
        SavedFilter savedFilterResponse = savedFiltersClient.put(putSavedFilterName, savedFilterRequest);
        assertEquals(putSavedFilterName, savedFilterResponse.getName());
        assertEquals(putSavedFilterName, savedFilterResponse.getId());
        assertEquals(putSavedFilterName, savedFilterResponse.getDisplayName());
        assertEquals(savedFilterRequest.getDescription(), savedFilterResponse.getDescription());
    }

    @Test
    public void testsavedFiltersDeleteWithResponse(){
        savedFiltersClient.delete(deleteSavedFilterName);
    }

}
