package com.microsoft.azure.management.dns;

import java.util.List;

public interface NsRecordSet extends DnsRecordSet {
    /**
     * @return the name server names of Ns (name server) records in this record set
     */
    List<String> nameServers();
}
