package com.azure.spring.data.cosmos.core.mapping.event;

import java.util.ArrayList;

public class SimpleMappingEventListener extends AbstractCosmosEventListener<Object> {

    public ArrayList<AfterLoadEvent<Object>> onAfterLoadEvents = new ArrayList<>();

    @Override
    public void onAfterLoad(AfterLoadEvent<Object> event) {
        onAfterLoadEvents.add(event);
    }

}
