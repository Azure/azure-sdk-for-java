// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.gremlin;

import com.microsoft.spring.data.gremlin.annotation.Vertex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Vertex
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    @Id
    private String id;

    private String name;

    private String level;
}
