// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import org.springframework.data.annotation.Id;

// BEGIN: readme-sample-UserSample
@Container(containerName = "myContainer", autoScale = true, ru = "4000")
public class UserSample {
    @Id
    private String emailAddress;

}
// END: readme-sample-UserSample
