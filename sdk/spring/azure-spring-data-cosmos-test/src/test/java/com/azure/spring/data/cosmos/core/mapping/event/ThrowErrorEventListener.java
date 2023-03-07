// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

public class ThrowErrorEventListener extends AbstractCosmosEventListener<Object> {

    @Override
    public void onAfterLoad(AfterLoadEvent<Object> event) {
        throw new RuntimeException();
    }

}
