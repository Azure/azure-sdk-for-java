package com.microsoft.azure.servicebus.management;

/**
 * The status of the messaging entity.
 */
public enum EntityStatus {

    /**
     * The entity is active.
     */
    Active(0),

    /**
     * The entity is disabled.
     */
    Disabled(1),

    /**
     * Send operation is disabled on the entity.
     */
    SendDisabled(2),

    /**
     * Receive operation is disabled on the entity.
     */
    ReceiveDisabled(3),

    /**
     * The status of the entity is unknown.
     */
    Unknown(99);

    private int entityStatus;

    int getEntityStatus() {
        return this.entityStatus;
    }

    EntityStatus(int i) {
        this.entityStatus = i;
    }
}
