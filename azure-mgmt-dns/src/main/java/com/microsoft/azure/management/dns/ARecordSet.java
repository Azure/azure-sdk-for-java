package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a A (Ipv4) record set in Azure Dns Zone.
 */
public interface ARecordSet extends DnsRecordSet {
    /**
     * @return the Ipv4 addresses of A records in this record set
     */
    List<String> ipv4Addresses();
}
