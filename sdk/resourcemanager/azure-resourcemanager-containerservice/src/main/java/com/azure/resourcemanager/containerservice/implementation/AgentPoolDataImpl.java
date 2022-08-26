package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.fluent.models.AgentPoolInner;
import com.azure.resourcemanager.containerservice.models.AgentPoolData;

/**
 * AgentPool implementation that exposes AgentPoolInner as constructor arguments.
 */
public class AgentPoolDataImpl extends AgentPoolData {
    /**
     * Creates an instance of agent pool data.
     *
     * @param innerModel the inner model of agent pool.
     */
    AgentPoolDataImpl(AgentPoolInner innerModel) {
        super(innerModel);
    }
}
