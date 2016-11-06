package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of an Ns (name server) record set in Azure Dns Zone.
 */
public interface NsRecordSet extends DnsRecordSet {
    /**
     * @return the name server names of Ns (name server) records in this record set
     */
    List<String> nameServers();
}
