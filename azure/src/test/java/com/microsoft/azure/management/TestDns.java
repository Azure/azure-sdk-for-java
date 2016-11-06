package com.microsoft.azure.management;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.AaaaRecordSet;
import com.microsoft.azure.management.dns.CnameRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsZones;
import com.microsoft.azure.management.dns.MxRecord;
import com.microsoft.azure.management.dns.MxRecordSet;
import com.microsoft.azure.management.dns.NsRecordSet;
import com.microsoft.azure.management.dns.PtrRecordSet;
import com.microsoft.azure.management.dns.SoaRecord;
import com.microsoft.azure.management.dns.SoaRecordSet;
import com.microsoft.azure.management.dns.SrvRecord;
import com.microsoft.azure.management.dns.SrvRecordSet;
import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.dns.TxtRecordSet;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

/**
 * Test of Dns management.
 */
public class TestDns extends TestTemplate<DnsZone, DnsZones> {
    @Override
    public DnsZone createResource(DnsZones dnsZones) throws Exception {
        final Region region = Region.US_EAST;
        final String groupName = "rg" + this.testId;

        DnsZone dnsZone = dnsZones.define("www.contoso.com")
                .withNewResourceGroup(groupName, region)
                .defineARecordSet("www")
                    .withIpv4Address("23.96.104.40")
                    .withIpv4Address("24.97.105.41")
                    .attach()
                .defineAaaaRecordSet("www")
                    .withIpv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                    .withIpv6Address("2002:0db9:85a4:0000:0000:8a2e:0371:7335")
                    .attach()
                .defineMxRecordSet("email")
                    .withMailExchange("mail.contoso-mail-exchange1.com", 1)
                    .withMailExchange("mail.contoso-mail-exchange2.com", 2)
                    .attach()
                .defineNsRecordSet("partners")
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
                .withCnameRecordSet("documents", "doc.contoso.com")
                .withCnameRecordSet("userguide", "doc.contoso.com")
                .withTag("a", "aa")
                .withTag("b", "bb")
                .create();
        return dnsZone;
    }

    @Override
    public DnsZone updateResource(DnsZone dnsZone) throws Exception {
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
                .append("\n\t\tTtl (seconds):").append(soaRecordSet.timeToLive());

        PagedList<ARecordSet> aRecordSets = dnsZone.aRecordSets().list();
        info.append("\n\tA Record sets:");
        for (ARecordSet aRecordSet : aRecordSets) {
            info.append("\n\t\tId: ").append(aRecordSet.id())
                .append("\n\t\tName: ").append(aRecordSet.name())
                .append("\n\t\tTtl (seconds): ").append(aRecordSet.timeToLive())
                .append("\n\t\tIp v4 addresses: ");
            for (String ipAddress : aRecordSet.ipv4Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedList<AaaaRecordSet> aaaaRecordSets = dnsZone.aaaaRecordSets().list();
        info.append("\n\tAAAA Record sets:");
        for (AaaaRecordSet aaaaRecordSet : aaaaRecordSets) {
            info.append("\n\t\tId: ").append(aaaaRecordSet.id())
                    .append("\n\t\tName: ").append(aaaaRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(aaaaRecordSet.timeToLive())
                    .append("\n\t\tIp v6 addresses: ");
            for (String ipAddress : aaaaRecordSet.ipv6Addresses()) {
                info.append("\n\t\t\t").append(ipAddress);
            }
        }

        PagedList<CnameRecordSet> cnameRecordSets = dnsZone.cnameRecordSets().list();
        info.append("\n\tCNAME Record sets:");
        for (CnameRecordSet cnameRecordSet : cnameRecordSets) {
            info.append("\n\t\tId: ").append(cnameRecordSet.id())
                    .append("\n\t\tName: ").append(cnameRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(cnameRecordSet.timeToLive())
                    .append("\n\t\tCanonical name: ").append(cnameRecordSet.canonicalName());
        }

        PagedList<MxRecordSet> mxRecordSets = dnsZone.mxRecordSets().list();
        info.append("\n\tMX Record sets:");
        for (MxRecordSet mxRecordSet : mxRecordSets) {
            info.append("\n\t\tId: ").append(mxRecordSet.id())
                    .append("\n\t\tName: ").append(mxRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(mxRecordSet.timeToLive())
                    .append("\n\t\tRecords: ");
            for (MxRecord mxRecord : mxRecordSet.records()) {
                info.append("\n\t\t\tExchange server, Preference: ")
                        .append(mxRecord.exchange())
                        .append(" ")
                        .append(mxRecord.preference());
            }
        }

        PagedList<NsRecordSet> nsRecordSets = dnsZone.nsRecordSets().list();
        info.append("\n\tNS Record sets:");
        for (NsRecordSet nsRecordSet : nsRecordSets) {
            info.append("\n\t\tId: ").append(nsRecordSet.id())
                    .append("\n\t\tName: ").append(nsRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(nsRecordSet.timeToLive())
                    .append("\n\t\tName servers: ");
            for (String nameServer : nsRecordSet.nameServers()) {
                info.append("\n\t\t\t").append(nameServer);
            }
        }

        PagedList<PtrRecordSet> ptrRecordSets = dnsZone.ptrRecordSets().list();
        info.append("\n\tPTR Record sets:");
        for (PtrRecordSet ptrRecordSet : ptrRecordSets) {
            info.append("\n\t\tId: ").append(ptrRecordSet.id())
                    .append("\n\t\tName: ").append(ptrRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(ptrRecordSet.timeToLive())
                    .append("\n\t\tTarget domain names: ");
            for (String domainNames : ptrRecordSet.targetDomainNames()) {
                info.append("\n\t\t\t").append(domainNames);
            }
        }

        PagedList<SrvRecordSet> srvRecordSets = dnsZone.srvRecordSets().list();
        info.append("\n\tSRV Record sets:");
        for (SrvRecordSet srvRecordSet : srvRecordSets) {
            info.append("\n\t\tId: ").append(srvRecordSet.id())
                    .append("\n\t\tName: ").append(srvRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(srvRecordSet.timeToLive())
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

        PagedList<TxtRecordSet> txtRecordSets = dnsZone.txtRecordSets().list();
        info.append("\n\tTXT Record sets:");
        for (TxtRecordSet txtRecordSet : txtRecordSets) {
            info.append("\n\t\tId: ").append(txtRecordSet.id())
                    .append("\n\t\tName: ").append(txtRecordSet.name())
                    .append("\n\t\tTtl (seconds): ").append(txtRecordSet.timeToLive())
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
