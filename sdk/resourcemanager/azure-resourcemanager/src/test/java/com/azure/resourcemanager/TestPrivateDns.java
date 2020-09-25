// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.privatedns.models.ARecordSet;
import com.azure.resourcemanager.privatedns.models.AaaaRecordSet;
import com.azure.resourcemanager.privatedns.models.CnameRecordSet;
import com.azure.resourcemanager.privatedns.models.MxRecord;
import com.azure.resourcemanager.privatedns.models.MxRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZones;
import com.azure.resourcemanager.privatedns.models.PtrRecordSet;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.SoaRecord;
import com.azure.resourcemanager.privatedns.models.SoaRecordSet;
import com.azure.resourcemanager.privatedns.models.SrvRecord;
import com.azure.resourcemanager.privatedns.models.SrvRecordSet;
import com.azure.resourcemanager.privatedns.models.TxtRecord;
import com.azure.resourcemanager.privatedns.models.TxtRecordSet;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

import static com.azure.resourcemanager.privatedns.models.RecordType.AAAA;
import static com.azure.resourcemanager.privatedns.models.RecordType.MX;
import static com.azure.resourcemanager.privatedns.models.RecordType.PTR;
import static com.azure.resourcemanager.privatedns.models.RecordType.SOA;
import static com.azure.resourcemanager.privatedns.models.RecordType.SRV;
import static com.azure.resourcemanager.privatedns.models.RecordType.TXT;

public class TestPrivateDns extends TestTemplate<PrivateDnsZone, PrivateDnsZones> {
    @Override
    public PrivateDnsZone createResource(PrivateDnsZones resources) throws Exception {
        final Region region = Region.US_EAST;
        final String testId = resources.manager().resourceManager().internalContext().randomResourceName("", 8);
        final String groupName = "rg" + testId;
        final String topLevelDomain = "www.contoso" + testId + ".com";

        PrivateDnsZone resource = resources.define(topLevelDomain)
            .withNewResourceGroup(groupName, region)
            .defineARecordSet("www")
            .withIPv4Address("23.96.104.40")
            .withIPv4Address("24.97.105.41")
            .withTimeToLive(7200) // Overwrite default 3600 seconds
            .attach()
            .defineAaaaRecordSet("www")
            .withIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
            .withIPv6Address("2002:0db9:85a4:0000:0000:8a2e:0371:7335")
            .attach()
            .defineMxRecordSet("email")
            .withMailExchange("mail.contoso-mail-exchange1.com", 1)
            .withMailExchange("mail.contoso-mail-exchange2.com", 2)
            .withMetadata("mxa", "mxaa")
            .withMetadata("mxb", "mxbb")
            .attach()
            .defineTxtRecordSet("@")
            .withText("windows-apps-verification=2ZzjfideIJFLFje83")
            .attach()
            .defineTxtRecordSet("www")
            .withText("some info about www.contoso.com")
            .attach()
            .defineSrvRecordSet("_sip._tcp")
            .withRecord("bigbox.contoso-service.com", 5060, 10, 60)
            .withRecord("smallbox1.contoso-service.com", 5060, 10, 20)
            .withRecord("smallbox2.contoso-service.com", 5060, 10, 20)
            .withRecord("backupbox.contoso-service.com", 5060, 10, 0)
            .attach()
            .definePtrRecordSet("40")
            .withTargetDomainName("www.contoso.com")
            .withTargetDomainName("mail.contoso.com")
            .attach()
            .definePtrRecordSet("41")
            .withTargetDomainName("www.contoso.com")
            .withTargetDomainName("mail.contoso.com")
            .attach()
            .withCnameRecordSet("documents", "doc.contoso.com")
            .withCnameRecordSet("userguide", "doc.contoso.com")
            .withTag("a", "aa")
            .withTag("b", "bb")
            .create();

        // Check Dns zone properties
        Assertions.assertTrue(resource.name().startsWith(topLevelDomain));
        Assertions.assertTrue(resource.tags().size() == 2);

        // Check SOA record - external child resource (created by default)
        SoaRecordSet soaRecordSet = resource.getSoaRecordSet();
        Assertions.assertTrue(soaRecordSet.name().startsWith("@"));
        SoaRecord soaRecord = soaRecordSet.record();
        Assertions.assertNotNull(soaRecord);

        // Check explicitly created external child resources [A, AAAA, MX, TXT, SRV, PTR, CNAME]
        //

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = resource.aRecordSets().list();
        Assertions.assertTrue(aRecordSets.stream().count() == 1);
        Assertions.assertTrue(aRecordSets.iterator().next().timeToLive() == 7200);

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = resource.aaaaRecordSets().list();
        Assertions.assertTrue(aaaaRecordSets.stream().count() == 1);
        Assertions.assertTrue(aaaaRecordSets.iterator().next().name().startsWith("www"));
        Assertions.assertTrue(aaaaRecordSets.iterator().next().ipv6Addresses().size() == 2);

        // Check MX records
        PagedIterable<MxRecordSet> mxRecordSets = resource.mxRecordSets().list();
        Assertions.assertTrue(mxRecordSets.stream().count() == 1);
        MxRecordSet mxRecordSet = mxRecordSets.iterator().next();
        Assertions.assertNotNull(mxRecordSet);
        Assertions.assertTrue(mxRecordSet.name().startsWith("email"));
        Assertions.assertTrue(mxRecordSet.metadata().size() == 2);
        Assertions.assertTrue(mxRecordSet.records().size() == 2);
        for (MxRecord mxRecord : mxRecordSet.records()) {
            Assertions.assertTrue(mxRecord.exchange().startsWith("mail.contoso-mail-exchange1.com")
                || mxRecord.exchange().startsWith("mail.contoso-mail-exchange2.com"));
            Assertions.assertTrue(mxRecord.preference() == 1
                || mxRecord.preference() == 2);
        }

        // Check TXT records
        PagedIterable<TxtRecordSet> txtRecordSets = resource.txtRecordSets().list();
        Assertions.assertTrue(txtRecordSets.stream().count() == 2);

        // Check SRV records
        PagedIterable<SrvRecordSet> srvRecordSets = resource.srvRecordSets().list();
        Assertions.assertTrue(srvRecordSets.stream().count() == 1);

        // Check PTR records
        PagedIterable<PtrRecordSet> ptrRecordSets = resource.ptrRecordSets().list();
        Assertions.assertTrue(ptrRecordSets.stream().count() == 2);

        // Check CNAME records
        PagedIterable<CnameRecordSet> cnameRecordSets = resource.cnameRecordSets().list();
        Assertions.assertTrue(cnameRecordSets.stream().count() == 2);

        // Check Generic record set listing
        PagedIterable<PrivateDnsRecordSet> recordSets = resource.listRecordSets();
        HashMap<RecordType, Integer> typeToCount = new HashMap<RecordType, Integer>();
        typeToCount.put(RecordType.A, 0);
        typeToCount.put(AAAA, 0);
        typeToCount.put(RecordType.CNAME, 0);
        typeToCount.put(MX, 0);
        typeToCount.put(PTR, 0);
        typeToCount.put(SOA, 0);
        typeToCount.put(SRV, 0);
        typeToCount.put(TXT, 0);
        for (PrivateDnsRecordSet recordSet : recordSets) {
            Assertions.assertNotNull(recordSet);
            switch (recordSet.recordType()) {
                case TXT:
                    TxtRecordSet txtRS = (TxtRecordSet) recordSet;
                    Assertions.assertNotNull(txtRS);
                    typeToCount.put(TXT, typeToCount.get(TXT) + 1);
                    break;
                case SRV:
                    SrvRecordSet srvRS = (SrvRecordSet) recordSet;
                    Assertions.assertNotNull(srvRS);
                    typeToCount.put(SRV, typeToCount.get(SRV) + 1);
                    break;
                case SOA:
                    SoaRecordSet soaRS = (SoaRecordSet) recordSet;
                    Assertions.assertNotNull(soaRS);
                    typeToCount.put(SOA, typeToCount.get(SOA) + 1);
                    break;
                case PTR:
                    PtrRecordSet ptrRS = (PtrRecordSet) recordSet;
                    Assertions.assertNotNull(ptrRS);
                    typeToCount.put(PTR, typeToCount.get(PTR) + 1);
                    break;
                case A:
                    ARecordSet aRS = (ARecordSet) recordSet;
                    Assertions.assertNotNull(aRS);
                    typeToCount.put(RecordType.A, typeToCount.get(RecordType.A) + 1);
                    break;
                case AAAA:
                    AaaaRecordSet aaaaRS = (AaaaRecordSet) recordSet;
                    Assertions.assertNotNull(aaaaRS);
                    typeToCount.put(AAAA, typeToCount.get(AAAA) + 1);
                    break;
                case CNAME:
                    CnameRecordSet cnameRS = (CnameRecordSet) recordSet;
                    Assertions.assertNotNull(cnameRS);
                    typeToCount.put(RecordType.CNAME, typeToCount.get(RecordType.CNAME) + 1);
                    break;
                case MX:
                    MxRecordSet mxRS = (MxRecordSet) recordSet;
                    Assertions.assertNotNull(mxRS);
                    typeToCount.put(MX, typeToCount.get(MX) + 1);
                    break;
                default:
                    Assertions.assertNotNull(recordSet);
            }
        }
        Assertions.assertTrue(typeToCount.get(SOA) == 1);
        Assertions.assertTrue(typeToCount.get(RecordType.A) == 1);
        Assertions.assertTrue(typeToCount.get(AAAA) == 1);
        Assertions.assertTrue(typeToCount.get(MX) == 1);
        Assertions.assertTrue(typeToCount.get(TXT) == 2);
        Assertions.assertTrue(typeToCount.get(SRV) == 1);
        Assertions.assertTrue(typeToCount.get(PTR) == 2);
        Assertions.assertTrue(typeToCount.get(RecordType.CNAME) == 2);
        return resource;
    }

    @Override
    public PrivateDnsZone updateResource(PrivateDnsZone resource) throws Exception {
        resource.update()
            .withoutTxtRecordSet("www")
            .withoutCNameRecordSet("userguide")
            .withCnameRecordSet("help", "doc.contoso.com")
            .updateARecordSet("www")
            .withoutIPv4Address("23.96.104.40")
            .withIPv4Address("23.96.104.42")
            .parent()
            .updateSrvRecordSet("_sip._tcp")
            .withoutRecord("bigbox.contoso-service.com", 5060, 10, 60)
            .withRecord("mainbox.contoso-service.com", 5060, 10, 60)
            .parent()
            .updateSoaRecord()
            .withNegativeResponseCachingTimeToLiveInSeconds(600)
            .withTimeToLive(7200)
            .parent()
            .defineMxRecordSet("email-internal")
            .withMailExchange("mail.contoso-mail-exchange1.com", 1)
            .withMailExchange("mail.contoso-mail-exchange2.com", 2)
            .attach()
            .apply();

        // Check TXT records
        PagedIterable<TxtRecordSet> txtRecordSets = resource.txtRecordSets().list();
        Assertions.assertEquals(txtRecordSets.stream().count(), 1);

        // Check CNAME records
        PagedIterable<CnameRecordSet> cnameRecordSets = resource.cnameRecordSets().list();
        Assertions.assertEquals(cnameRecordSets.stream().count(), 2);
        for (CnameRecordSet cnameRecordSet : cnameRecordSets) {
            Assertions.assertTrue(cnameRecordSet.canonicalName().startsWith("doc.contoso.com"));
            Assertions.assertTrue(cnameRecordSet.name().startsWith("documents")
                || cnameRecordSet.name().startsWith("help"));
        }

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = resource.aRecordSets().list();
        Assertions.assertEquals(aRecordSets.stream().count(), 1);
        ARecordSet aRecordSet = aRecordSets.iterator().next();
        Assertions.assertEquals(aRecordSet.ipv4Addresses().size(), 2);
        for (String ipV4Address : aRecordSet.ipv4Addresses()) {
            Assertions.assertFalse(ipV4Address.startsWith("23.96.104.40"));
        }

        // Check SRV records
        PagedIterable<SrvRecordSet> srvRecordSets = resource.srvRecordSets().list();
        Assertions.assertTrue(srvRecordSets.stream().count() == 1);
        SrvRecordSet srvRecordSet = srvRecordSets.iterator().next();
        Assertions.assertTrue(srvRecordSet.records().size() == 4);
        for (SrvRecord srvRecord : srvRecordSet.records()) {
            Assertions.assertFalse(srvRecord.target().startsWith("bigbox.contoso-service.com"));
        }

        // Check SOA Records
        SoaRecordSet soaRecordSet = resource.getSoaRecordSet();
        Assertions.assertTrue(soaRecordSet.name().startsWith("@"));
        SoaRecord soaRecord = soaRecordSet.record();
        Assertions.assertNotNull(soaRecord);
        Assertions.assertEquals(soaRecord.minimumTtl(), Long.valueOf(600));
        Assertions.assertTrue(soaRecordSet.timeToLive() == 7200);

        // Check MX records
        PagedIterable<MxRecordSet> mxRecordSets = resource.mxRecordSets().list();
        Assertions.assertTrue(mxRecordSets.stream().count() == 2);

        resource.update()
            .updateMxRecordSet("email")
            .withoutMailExchange("mail.contoso-mail-exchange2.com", 2)
            .withoutMetadata("mxa")
            .withMetadata("mxc", "mxcc")
            .withMetadata("mxd", "mxdd")
            .parent()
            .withTag("d", "dd")
            .apply();

        Assertions.assertTrue(resource.tags().size() == 3);
        // Check "mail" MX record
        MxRecordSet mxRecordSet = resource.mxRecordSets().getByName("email");
        Assertions.assertTrue(mxRecordSet.records().size() == 1);
        Assertions.assertTrue(mxRecordSet.metadata().size() == 3);
        Assertions.assertTrue(mxRecordSet.records().get(0).exchange().startsWith("mail.contoso-mail-exchange1.com"));

        return resource;
    }

    @Override
    public void print(PrivateDnsZone resource) {
        StringBuilder info = new StringBuilder();
        info.append("Dns Zone: ").append(resource.id())
            .append("\n\tName (Top level domain): ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.regionName())
            .append("\n\tTags: ").append(resource.tags());
        SoaRecordSet soaRecordSet = resource.getSoaRecordSet();
        SoaRecord soaRecord = soaRecordSet.record();
        info.append("\n\tSOA Record:")
            .append("\n\t\tHost:").append(soaRecord.host())
            .append("\n\t\tEmail:").append(soaRecord.email())
            .append("\n\t\tExpire time (seconds):").append(soaRecord.expireTime())
            .append("\n\t\tRefresh time (seconds):").append(soaRecord.refreshTime())
            .append("\n\t\tRetry time (seconds):").append(soaRecord.retryTime())
            .append("\n\t\tNegative response cache ttl (seconds):").append(soaRecord.minimumTtl())
            .append("\n\t\tTTL (seconds):").append(soaRecordSet.timeToLive());

        PagedIterable<ARecordSet> aRecordSets = resource.aRecordSets().list();
        info.append("\n\tA Record sets:");
        for (ARecordSet aRecordSet : aRecordSets) {
            info.append("\n\t\tId: ").append(aRecordSet.id())
                .append("\n\t\tName: ").append(aRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(aRecordSet.timeToLive())
                .append("\n\t\tIP v4 addresses: ");
            for (String ipAddress : aRecordSet.ipv4Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedIterable<AaaaRecordSet> aaaaRecordSets = resource.aaaaRecordSets().list();
        info.append("\n\tAAAA Record sets:");
        for (AaaaRecordSet aaaaRecordSet : aaaaRecordSets) {
            info.append("\n\t\tId: ").append(aaaaRecordSet.id())
                .append("\n\t\tName: ").append(aaaaRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(aaaaRecordSet.timeToLive())
                .append("\n\t\tIP v6 addresses: ");
            for (String ipAddress : aaaaRecordSet.ipv6Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedIterable<CnameRecordSet> cnameRecordSets = resource.cnameRecordSets().list();
        info.append("\n\tCNAME Record sets:");
        for (CnameRecordSet cnameRecordSet : cnameRecordSets) {
            info.append("\n\t\tId: ").append(cnameRecordSet.id())
                .append("\n\t\tName: ").append(cnameRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(cnameRecordSet.timeToLive())
                .append("\n\t\tCanonical name: ").append(cnameRecordSet.canonicalName());
        }

        PagedIterable<MxRecordSet> mxRecordSets = resource.mxRecordSets().list();
        info.append("\n\tMX Record sets:");
        for (MxRecordSet mxRecordSet : mxRecordSets) {
            info.append("\n\t\tId: ").append(mxRecordSet.id())
                .append("\n\t\tName: ").append(mxRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(mxRecordSet.timeToLive())
                .append("\n\t\tRecords: ");
            for (MxRecord mxRecord : mxRecordSet.records()) {
                info.append("\n\t\t\tExchange server, Preference: ")
                    .append(mxRecord.exchange())
                    .append(" ")
                    .append(mxRecord.preference());
            }
        }

        PagedIterable<PtrRecordSet> ptrRecordSets = resource.ptrRecordSets().list();
        info.append("\n\tPTR Record sets:");
        for (PtrRecordSet ptrRecordSet : ptrRecordSets) {
            info.append("\n\t\tId: ").append(ptrRecordSet.id())
                .append("\n\t\tName: ").append(ptrRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(ptrRecordSet.timeToLive())
                .append("\n\t\tTarget domain names: ");
            for (String domainNames : ptrRecordSet.targetDomainNames()) {
                info.append("\n\t\t\t").append(domainNames);
            }
        }

        PagedIterable<SrvRecordSet> srvRecordSets = resource.srvRecordSets().list();
        info.append("\n\tSRV Record sets:");
        for (SrvRecordSet srvRecordSet : srvRecordSets) {
            info.append("\n\t\tId: ").append(srvRecordSet.id())
                .append("\n\t\tName: ").append(srvRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(srvRecordSet.timeToLive())
                .append("\n\t\tRecords: ");
            for (SrvRecord srvRecord : srvRecordSet.records()) {
                info.append("\n\t\t\tTarget, Port, Priority, Weight: ")
                    .append(srvRecord.target())
                    .append(", ")
                    .append(srvRecord.port())
                    .append(", ")
                    .append(srvRecord.priority())
                    .append(", ")
                    .append(srvRecord.weight());
            }
        }

        PagedIterable<TxtRecordSet> txtRecordSets = resource.txtRecordSets().list();
        info.append("\n\tTXT Record sets:");
        for (TxtRecordSet txtRecordSet : txtRecordSets) {
            info.append("\n\t\tId: ").append(txtRecordSet.id())
                .append("\n\t\tName: ").append(txtRecordSet.name())
                .append("\n\t\tTTL (seconds): ").append(txtRecordSet.timeToLive())
                .append("\n\t\tRecords: ");
            for (TxtRecord txtRecord : txtRecordSet.records()) {
                if (txtRecord.value().size() > 0) {
                    info.append("\n\t\t\tValue: ").append(txtRecord.value().get(0));
                }
            }
        }
        System.out.println(info.toString());
    }
}
