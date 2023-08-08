// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.dns;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.dns.models.CnameRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecordSetTests extends DnsTestBase {

    @Test
    public void canCRUDCname() {
        final Region region = Region.US_EAST;
        final String topLevelDomain = "www.contoso" + rgName + ".com";

        DnsZone dnsZone =
            zoneManager
                .zones()
                .define(topLevelDomain)
                .withNewResourceGroup(rgName, region)
                .defineCNameRecordSet("www")
                .withAlias("cname.contoso.com")
                .withTimeToLive(7200)
                .attach()
                .create();

        // Check CNAME records
        dnsZone.refresh();
        PagedIterable<CnameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        Assertions.assertEquals(1, TestUtilities.getSize(cnameRecordSets));
        CnameRecordSet cnameRecordSet = cnameRecordSets.iterator().next();
        Assertions.assertEquals("www", cnameRecordSet.name());
        Assertions.assertEquals(7200, cnameRecordSet.timeToLive());
        Assertions.assertEquals("cname.contoso.com", cnameRecordSet.canonicalName());

        // Update alias and ttl:
        dnsZone
            .update()
            .updateCNameRecordSet("www")
            .withAlias("new.contoso.com")
            .withTimeToLive(3600)
            .parent()
            .apply();

        // Check CNAME records
        dnsZone.refresh();
        PagedIterable<CnameRecordSet> updatedCnameRecordSets = dnsZone.cNameRecordSets().list();
        Assertions.assertEquals(1, TestUtilities.getSize(updatedCnameRecordSets));
        CnameRecordSet updatedCnameRecordSet = updatedCnameRecordSets.iterator().next();
        Assertions.assertEquals(3600, updatedCnameRecordSet.timeToLive());
        Assertions.assertEquals("new.contoso.com", updatedCnameRecordSet.canonicalName());
    }
}
