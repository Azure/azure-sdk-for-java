package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a Ptr (pointer) record set in Azure Dns Zone.
 */
public interface PtrRecordSet extends DnsRecordSet {
    /**
     * @return the target domain names of Ptr records in this record set
     */
    List<String> targetDomainNames();
}
