package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a Aaaa (Ipv6) record set in Azure Dns Zone.
 */
public interface AaaaRecordSet extends DnsRecordSet {
    /**
     * @return the IPv6 addresses of Aaaa records in this record set
     */
    List<String> ipv6Addresses();
}
