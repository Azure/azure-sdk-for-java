package com.azure.quantum;

import com.azure.quantum.models.ProviderStatus;
import org.junit.jupiter.api.Test;
import java.util.List;

import static com.azure.quantum.TestUtils.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProvidersTest {

    // Get status
    @Test
    public void getStatusAsyncTest() {
        Providers providers = new Providers(getClient());
        List<ProviderStatus> results = providers.getStatusAsync().collectSortedList().block();

        assertEquals(0, results.size());
        assertEquals("id", results.get(0).getId());
    }


}
