// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import java.util.ArrayList;

public class SimpleCosmosMappingEventListener extends AbstractCosmosEventListener<Object> {

    public ArrayList<AfterLoadEvent<Object>> onAfterLoadEvents = new ArrayList<>();

    @Override
    public void onAfterLoad(AfterLoadEvent<Object> event) {
        onAfterLoadEvents.add(event);
    }

}
