package com.microsoft.azure.management.dns;

/**
 * An immutable client-side representation of a Soa (start of authority) record set in Azure Dns Zone.
 */
public interface SoaRecordSet extends DnsRecordSet {
    /**
     * @return the Soa record in this record set
     */
    SoaRecord record();
}
