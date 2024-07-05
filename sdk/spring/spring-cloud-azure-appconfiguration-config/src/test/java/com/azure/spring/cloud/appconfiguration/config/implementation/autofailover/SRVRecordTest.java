// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SRVRecordTest {
    
    @Test
    public void createSRVRecord() {
        String[] record = {"1", "2", "3", "fake-uri."};
        SRVRecord srvRecord = new SRVRecord(record);
        assertEquals(1, srvRecord.getPriority());
        assertEquals(2, srvRecord.getWeight());
        assertEquals(3, srvRecord.getPort());
        assertEquals("fake-uri", srvRecord.getTarget());
        assertEquals("https://fake-uri", srvRecord.getEndpoint());
    }
    
    @Test
    public void compareTest() {
        String[] p1Record = {"1", "1", "1", "p1."};
        SRVRecord p1 = new SRVRecord(p1Record);
        String[] p2Record = {"2", "2", "2", "p1."};
        SRVRecord p2 = new SRVRecord(p2Record);
        assertEquals(-1, p1.compareTo(p2));
        assertEquals(1, p2.compareTo(p1));
        
        String[] p1w2Record = {"1", "2", "1", "p1."};
        SRVRecord p1w2 = new SRVRecord(p1w2Record);
        assertEquals(-1, p1.compareTo(p1w2));
        assertEquals(1, p1w2.compareTo(p1));
        
        assertEquals(0, p1.compareTo(p1));
    }

}
