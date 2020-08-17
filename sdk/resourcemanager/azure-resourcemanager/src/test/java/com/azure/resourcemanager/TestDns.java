// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.AaaaRecordSet;
import com.azure.resourcemanager.dns.models.CNameRecordSet;
import com.azure.resourcemanager.dns.models.DnsRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.models.DnsZones;
import com.azure.resourcemanager.dns.models.MXRecordSet;
import com.azure.resourcemanager.dns.models.MxRecord;
import com.azure.resourcemanager.dns.models.NSRecordSet;
import com.azure.resourcemanager.dns.models.PtrRecordSet;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.SoaRecord;
import com.azure.resourcemanager.dns.models.SoaRecordSet;
import com.azure.resourcemanager.dns.models.SrvRecord;
import com.azure.resourcemanager.dns.models.SrvRecordSet;
import com.azure.resourcemanager.dns.models.TxtRecord;
import com.azure.resourcemanager.dns.models.TxtRecordSet;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

import static com.azure.resourcemanager.dns.models.RecordType.AAAA;
import static com.azure.resourcemanager.dns.models.RecordType.MX;
import static com.azure.resourcemanager.dns.models.RecordType.NS;
import static com.azure.resourcemanager.dns.models.RecordType.PTR;
import static com.azure.resourcemanager.dns.models.RecordType.SOA;
import static com.azure.resourcemanager.dns.models.RecordType.SRV;
import static com.azure.resourcemanager.dns.models.RecordType.TXT;

public class TestDns extends TestTemplate<DnsZone, DnsZones> {
    @Override
    public DnsZone createResource(DnsZones dnsZones) throws Exception {
        final Region region = Region.US_EAST;
        final String testId = dnsZones.manager().sdkContext().randomResourceName("", 8);
        final String groupName = "rg" + testId;
        final String topLevelDomain = "www.contoso" + testId + ".com";

        DnsZone dnsZone = dnsZones.define(topLevelDomain)
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
                .defineMXRecordSet("email")
                    .withMailExchange("mail.contoso-mail-exchange1.com", 1)
                    .withMailExchange("mail.contoso-mail-exchange2.com", 2)
                    .withMetadata("mxa", "mxaa")
                    .withMetadata("mxb", "mxbb")
                    .attach()
                .defineNSRecordSet("partners")
                    .withNameServer("ns1-05.azure-dns.com")
                    .withNameServer("ns2-05.azure-dns.net")
                    .withNameServer("ns3-05.azure-dns.org")
                    .withNameServer("ns4-05.azure-dns.info")
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
                .withCNameRecordSet("documents", "doc.contoso.com")
                .withCNameRecordSet("userguide", "doc.contoso.com")
                .withTag("a", "aa")
                .withTag("b", "bb")
                .create();

        // Check Dns zone properties
        Assertions.assertTrue(dnsZone.name().startsWith(topLevelDomain));
        Assertions.assertTrue(dnsZone.nameServers().size() > 0); // Default '@' name servers
        Assertions.assertTrue(dnsZone.tags().size() == 2);

        // Check SOA record - external child resource (created by default)
        SoaRecordSet soaRecordSet = dnsZone.getSoaRecordSet();
        Assertions.assertTrue(soaRecordSet.name().startsWith("@"));
        SoaRecord soaRecord = soaRecordSet.record();
        Assertions.assertNotNull(soaRecord);

        // Check explicitly created external child resources [A, AAAA, MX, NS, TXT, SRV, PTR, CNAME]
        //

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertTrue(aRecordSets.stream().count() == 1);
        Assertions.assertTrue(aRecordSets.iterator().next().timeToLive() == 7200);

        // Check AAAA records
        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        Assertions.assertTrue(aaaaRecordSets.stream().count() == 1);
        Assertions.assertTrue(aaaaRecordSets.iterator().next().name().startsWith("www"));
        Assertions.assertTrue(aaaaRecordSets.iterator().next().ipv6Addresses().size() == 2);

        // Check MX records
        PagedIterable<MXRecordSet> mxRecordSets = dnsZone.mxRecordSets().list();
        Assertions.assertTrue(mxRecordSets.stream().count() == 1);
        MXRecordSet mxRecordSet = mxRecordSets.iterator().next();
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

        // Check NS records
        PagedIterable<NSRecordSet> nsRecordSets = dnsZone.nsRecordSets().list();
        Assertions.assertTrue(nsRecordSets.stream().count() == 2); // One created above with name 'partners' + the default '@'

        // Check TXT records
        PagedIterable<TxtRecordSet> txtRecordSets = dnsZone.txtRecordSets().list();
        Assertions.assertTrue(txtRecordSets.stream().count() == 2);

        // Check SRV records
        PagedIterable<SrvRecordSet> srvRecordSets = dnsZone.srvRecordSets().list();
        Assertions.assertTrue(srvRecordSets.stream().count() == 1);

        // Check PTR records
        PagedIterable<PtrRecordSet> ptrRecordSets = dnsZone.ptrRecordSets().list();
        Assertions.assertTrue(ptrRecordSets.stream().count() == 2);

        // Check CNAME records
        PagedIterable<CNameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        Assertions.assertTrue(cnameRecordSets.stream().count() == 2);

        // Check Generic record set listing
        PagedIterable<DnsRecordSet> recordSets = dnsZone.listRecordSets();
        HashMap<RecordType, Integer> typeToCount = new HashMap<RecordType, Integer>();
        typeToCount.put(RecordType.A, 0);
        typeToCount.put(AAAA, 0);
        typeToCount.put(RecordType.CNAME, 0);
        typeToCount.put(MX, 0);
        typeToCount.put(NS, 0);
        typeToCount.put(PTR, 0);
        typeToCount.put(SOA, 0);
        typeToCount.put(SRV, 0);
        typeToCount.put(TXT, 0);
        for (DnsRecordSet recordSet : recordSets) {
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
                    CNameRecordSet cnameRS = (CNameRecordSet) recordSet;
                    Assertions.assertNotNull(cnameRS);
                    typeToCount.put(RecordType.CNAME, typeToCount.get(RecordType.CNAME) + 1);
                    break;
                case MX:
                    MXRecordSet mxRS = (MXRecordSet) recordSet;
                    Assertions.assertNotNull(mxRS);
                    typeToCount.put(MX, typeToCount.get(MX) + 1);
                    break;
                case NS:
                    NSRecordSet nsRS = (NSRecordSet) recordSet;
                    Assertions.assertNotNull(nsRS);
                    typeToCount.put(NS, typeToCount.get(NS) + 1);
                    break;
                default:
                    Assertions.assertNotNull(recordSet);
            }
        }
        Assertions.assertTrue(typeToCount.get(SOA) == 1);
        Assertions.assertTrue(typeToCount.get(RecordType.A) == 1);
        Assertions.assertTrue(typeToCount.get(AAAA) == 1);
        Assertions.assertTrue(typeToCount.get(MX) == 1);
        Assertions.assertTrue(typeToCount.get(NS) == 2);
        Assertions.assertTrue(typeToCount.get(TXT) == 2);
        Assertions.assertTrue(typeToCount.get(SRV) == 1);
        Assertions.assertTrue(typeToCount.get(PTR) == 2);
        Assertions.assertTrue(typeToCount.get(RecordType.CNAME) == 2);
        return dnsZone;
    }

    @Override
    public DnsZone updateResource(DnsZone dnsZone) throws Exception {
        dnsZone.update()
                .withoutTxtRecordSet("www")
                .withoutCNameRecordSet("userguide")
                .withCNameRecordSet("help", "doc.contoso.com")
                .updateNSRecordSet("partners")
                    .withoutNameServer("ns4-05.azure-dns.info")
                    .withNameServer("ns4-06.azure-dns.info")
                    .parent()
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
                .defineMXRecordSet("email-internal")
                    .withMailExchange("mail.contoso-mail-exchange1.com", 1)
                    .withMailExchange("mail.contoso-mail-exchange2.com", 2)
                    .attach()
                .apply();

        // Check TXT records
        PagedIterable<TxtRecordSet> txtRecordSets = dnsZone.txtRecordSets().list();
        Assertions.assertEquals(txtRecordSets.stream().count(), 1);

        // Check CNAME records
        PagedIterable<CNameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        Assertions.assertEquals(cnameRecordSets.stream().count(), 2);
        for (CNameRecordSet cnameRecordSet : cnameRecordSets) {
            Assertions.assertTrue(cnameRecordSet.canonicalName().startsWith("doc.contoso.com"));
            Assertions.assertTrue(cnameRecordSet.name().startsWith("documents")
                || cnameRecordSet.name().startsWith("help"));
        }

        // Check NS records
        PagedIterable<NSRecordSet> nsRecordSets = dnsZone.nsRecordSets().list();
        Assertions.assertTrue(nsRecordSets.stream().count() == 2); // One created above with name 'partners' + the default '@'
        for (NSRecordSet nsRecordSet : nsRecordSets) {
            Assertions.assertTrue(nsRecordSet.name().startsWith("partners") || nsRecordSet.name().startsWith("@"));
            if (nsRecordSet.name().startsWith("partners")) {
                Assertions.assertEquals(nsRecordSet.nameServers().size(), 4);
                for (String nameServer : nsRecordSet.nameServers()) {
                    Assertions.assertFalse(nameServer.startsWith("ns4-05.azure-dns.info"));
                }
            }
        }

        // Check A records
        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        Assertions.assertEquals(aRecordSets.stream().count(), 1);
        ARecordSet aRecordSet = aRecordSets.iterator().next();
        Assertions.assertEquals(aRecordSet.ipv4Addresses().size(), 2);
        for (String ipV4Address : aRecordSet.ipv4Addresses()) {
            Assertions.assertFalse(ipV4Address.startsWith("23.96.104.40"));
        }

        // Check SRV records
        PagedIterable<SrvRecordSet> srvRecordSets = dnsZone.srvRecordSets().list();
        Assertions.assertTrue(srvRecordSets.stream().count() == 1);
        SrvRecordSet srvRecordSet = srvRecordSets.iterator().next();
        Assertions.assertTrue(srvRecordSet.records().size() == 4);
        for (SrvRecord srvRecord : srvRecordSet.records()) {
            Assertions.assertFalse(srvRecord.target().startsWith("bigbox.contoso-service.com"));
        }

        // Check SOA Records
        SoaRecordSet soaRecordSet = dnsZone.getSoaRecordSet();
        Assertions.assertTrue(soaRecordSet.name().startsWith("@"));
        SoaRecord soaRecord = soaRecordSet.record();
        Assertions.assertNotNull(soaRecord);
        Assertions.assertEquals(soaRecord.minimumTtl(), Long.valueOf(600));
        Assertions.assertTrue(soaRecordSet.timeToLive() == 7200);

        // Check MX records
        PagedIterable<MXRecordSet> mxRecordSets = dnsZone.mxRecordSets().list();
        Assertions.assertTrue(mxRecordSets.stream().count() == 2);

        dnsZone.update()
                .updateMXRecordSet("email")
                    .withoutMailExchange("mail.contoso-mail-exchange2.com", 2)
                    .withoutMetadata("mxa")
                    .withMetadata("mxc", "mxcc")
                    .withMetadata("mxd", "mxdd")
                    .parent()
                .withTag("d", "dd")
                .apply();

        Assertions.assertTrue(dnsZone.tags().size() == 3);
        // Check "mail" MX record
        MXRecordSet mxRecordSet = dnsZone.mxRecordSets().getByName("email");
        Assertions.assertTrue(mxRecordSet.records().size() == 1);
        Assertions.assertTrue(mxRecordSet.metadata().size() == 3);
        Assertions.assertTrue(mxRecordSet.records().get(0).exchange().startsWith("mail.contoso-mail-exchange1.com"));

        return dnsZone;
    }

    @Override
    public void print(DnsZone dnsZone) {
        StringBuilder info = new StringBuilder();
        info.append("Dns Zone: ").append(dnsZone.id())
                .append("\n\tName (Top level domain): ").append(dnsZone.name())
                .append("\n\tResource group: ").append(dnsZone.resourceGroupName())
                .append("\n\tRegion: ").append(dnsZone.regionName())
                .append("\n\tTags: ").append(dnsZone.tags())
                .append("\n\tName servers:");
        for (String nameServer: dnsZone.nameServers()) {
            info.append("\n\t\t").append(nameServer);
        }
        SoaRecordSet soaRecordSet = dnsZone.getSoaRecordSet();
        SoaRecord soaRecord = soaRecordSet.record();
        info.append("\n\tSOA Record:")
                .append("\n\t\tHost:").append(soaRecord.host())
                .append("\n\t\tEmail:").append(soaRecord.email())
                .append("\n\t\tExpire time (seconds):").append(soaRecord.expireTime())
                .append("\n\t\tRefresh time (seconds):").append(soaRecord.refreshTime())
                .append("\n\t\tRetry time (seconds):").append(soaRecord.retryTime())
                .append("\n\t\tNegative response cache ttl (seconds):").append(soaRecord.minimumTtl())
                .append("\n\t\tTTL (seconds):").append(soaRecordSet.timeToLive());

        PagedIterable<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
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

        PagedIterable<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
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

        PagedIterable<CNameRecordSet> cnameRecordSets = dnsZone.cNameRecordSets().list();
        info.append("\n\tCNAME Record sets:");
        for (CNameRecordSet cnameRecordSet : cnameRecordSets) {
            info.append("\n\t\tId: ").append(cnameRecordSet.id())
                    .append("\n\t\tName: ").append(cnameRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(cnameRecordSet.timeToLive())
                    .append("\n\t\tCanonical name: ").append(cnameRecordSet.canonicalName());
        }

        PagedIterable<MXRecordSet> mxRecordSets = dnsZone.mxRecordSets().list();
        info.append("\n\tMX Record sets:");
        for (MXRecordSet mxRecordSet : mxRecordSets) {
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

        PagedIterable<NSRecordSet> nsRecordSets = dnsZone.nsRecordSets().list();
        info.append("\n\tNS Record sets:");
        for (NSRecordSet nsRecordSet : nsRecordSets) {
            info.append("\n\t\tId: ").append(nsRecordSet.id())
                    .append("\n\t\tName: ").append(nsRecordSet.name())
                    .append("\n\t\tTTL (seconds): ").append(nsRecordSet.timeToLive())
                    .append("\n\t\tName servers: ");
            for (String nameServer : nsRecordSet.nameServers()) {
                info.append("\n\t\t\t").append(nameServer);
            }
        }

        PagedIterable<PtrRecordSet> ptrRecordSets = dnsZone.ptrRecordSets().list();
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

        PagedIterable<SrvRecordSet> srvRecordSets = dnsZone.srvRecordSets().list();
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

        PagedIterable<TxtRecordSet> txtRecordSets = dnsZone.txtRecordSets().list();
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
