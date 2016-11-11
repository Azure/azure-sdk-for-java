package com.microsoft.azure.management.dns;

import java.util.List;

/**
 * An immutable client-side representation of a Mx (mail exchange) record set in Azure Dns Zone.
 */
public interface MxRecordSet extends DnsRecordSet {
    /**
     * @return the Mx records in this record set
     */
    List<MxRecord> records();
}
