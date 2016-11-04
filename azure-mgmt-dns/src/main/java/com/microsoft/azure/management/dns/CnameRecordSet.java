package com.microsoft.azure.management.dns;

/**
 * An immutable client-side representation of a CName (canonical name) record set in Azure Dns Zone.
 */
public interface CnameRecordSet extends DnsRecordSet {
    /**
     * @return the canonical name (without a terminating dot) of CName record in this record set
     */
    String canonicalName();
}
