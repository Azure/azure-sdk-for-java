// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.gremlin;

import com.microsoft.azure.spring.data.gremlin.annotation.Edge;
import com.microsoft.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.microsoft.azure.spring.data.gremlin.annotation.EdgeTo;
import org.springframework.data.annotation.Id;

@Edge
public class Relation {

    @Id
    private String id;

    private String name;

    @EdgeFrom
    private Person personFrom;

    @EdgeTo
    private Person personTo;
}
