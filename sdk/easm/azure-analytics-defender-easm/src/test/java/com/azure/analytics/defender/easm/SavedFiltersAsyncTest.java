package com.azure.analytics.defender.easm;

import com.azure.analytics.defender.easm.generated.EasmClientTestBase;
import com.azure.analytics.defender.easm.models.SavedFilter;
import com.azure.analytics.defender.easm.models.SavedFilterData;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

public class SavedFiltersAsyncTest extends EasmClientTestBase {
    String deleteSavedFilterName = "put_filter";
    String putSavedFilterName = "put_filter";
    String knownExistingFilter = "new_put_filter";
    String filter = "name = \"" + putSavedFilterName + "\"";

    @Test
    public void testSavedFiltersListAsync() {
        PagedFlux<SavedFilter> savedFilterPagedFlux = easmAsyncClient.listSavedFilter();
        SavedFilter savedFilter = savedFilterPagedFlux.blockFirst();
        assertNotNull(savedFilter.getId());
        assertNotNull(savedFilter.getDescription());
    }

    @Test
    public void testSavedFiltersGetAsync() {
        Mono<SavedFilter> savedFilterMono = easmAsyncClient.getSavedFilter(knownExistingFilter);
        savedFilterMono.subscribe(
            savedFilter -> {
                assertEquals(knownExistingFilter, savedFilter.getId());
                assertEquals(knownExistingFilter, savedFilter.getName());
                assertNotNull(savedFilter.getDisplayName());
                assertNotNull(savedFilter.getFilter());
                assertNotNull(savedFilter.getDescription());
            }
        );
    }

    @Test
    public void testSavedFiltersCreateOrReplaceAsync() {
        SavedFilterData savedFilterData = new SavedFilterData(filter, "Sample description");
        Mono<SavedFilter> savedFilterMono = easmAsyncClient.createOrReplaceSavedFilter(putSavedFilterName, savedFilterData);
        savedFilterMono.subscribe(
          savedFilter -> {
              assertEquals(putSavedFilterName, savedFilter.getName());
              assertEquals(putSavedFilterName, savedFilter.getId());
              assertEquals(putSavedFilterName, savedFilter.getDisplayName());
              assertEquals(savedFilterData.getDescription(), savedFilter.getDescription());
          }
        );
    }

    @Test
    public void testSavedFiltersDeleteAsync() {
        assertDoesNotThrow(() -> {
            easmAsyncClient.deleteSavedFilter(deleteSavedFilterName);
        });
    }
}
