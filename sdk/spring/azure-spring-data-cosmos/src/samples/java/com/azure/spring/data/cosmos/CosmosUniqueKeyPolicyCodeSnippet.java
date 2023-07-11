// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosUniqueKey;
import com.azure.spring.data.cosmos.core.mapping.CosmosUniqueKeyPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

// BEGIN: readme-sample-CosmosUniqueKeyPolicyCodeSnippet
@Container
@CosmosUniqueKeyPolicy(uniqueKeys = {
    @CosmosUniqueKey(paths = {"/lastName", "/zipCode"}),
    @CosmosUniqueKey(paths = {"/city"})
})
public class CosmosUniqueKeyPolicyCodeSnippet {

    @Id
    String id;

    @PartitionKey
    String firstName;

    String lastName;
    String zipCode;
    String city;
}
// END: readme-sample-CosmosUniqueKeyPolicyCodeSnippet
