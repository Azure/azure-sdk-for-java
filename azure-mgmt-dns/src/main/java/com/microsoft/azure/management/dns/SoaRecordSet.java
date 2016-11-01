package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a Soa record set in Azure Dns Zone.
 */
public interface SoaRecordSet extends DnsRecordSet<SoaRecordSet, DnsZone> {
    /**
     * @return the Soa records in this record set
     */
    List<SoaRecord> records();
}
