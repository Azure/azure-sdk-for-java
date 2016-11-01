package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Base interface for various Dns record sets.
 *
 * @param <FluentRecordSetModelT> the type of the record set
 */
@Fluent
public interface DnsRecordSets<FluentRecordSetModelT> {
    /**
     * Gets the information about a Dns record set from Azure based on name.
     *
     * @param name the name of the record set
     * @return the record set
     */
    FluentRecordSetModelT getByName(String name);
}
