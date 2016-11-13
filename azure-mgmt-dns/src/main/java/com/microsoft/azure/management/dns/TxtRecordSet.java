package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a Txt (text) record set in Azure Dns Zone.
 */
public interface TxtRecordSet extends DnsRecordSet {
    /**
     * @return the Txt records in this record set
     */
    List<TxtRecord> records();
}
