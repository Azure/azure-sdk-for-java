package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a Srv (service) record set in Azure Dns Zone.
 */
public interface SrvRecordSet extends DnsRecordSet {
    /**
     * @return the Srv records in this record set
     */
    List<SrvRecord> records();
}
