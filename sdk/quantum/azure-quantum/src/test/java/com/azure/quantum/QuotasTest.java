package com.azure.quantum;


import com.azure.quantum.models.Quota;
import org.junit.jupiter.api.Test;
import java.util.List;

import static com.azure.quantum.TestUtils.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuotasTest {

    @Test
    public void listAsyncTest() {
        Quotas quotas = new Quotas(getClient());
        List<Quota> results = quotas.listAsync().collectSortedList().block();

        assertEquals(0, results.size());
    }


}
