/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.List;

/**
 * An immutable client-side representation of an Ns (name server) record set in Azure Dns Zone.
 */
@Fluent
public interface NsRecordSet extends DnsRecordSet {
    /**
     * @return the name server names of Ns (name server) records in this record set
     */
    List<String> nameServers();
}
