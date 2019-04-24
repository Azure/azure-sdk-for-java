package com.microsoft.azure.servicebus.management;

import java.time.Instant;
import java.util.List;

public abstract class AuthorizationRule {
    private Instant createdTime;
    private Instant modifiedTime;

    public abstract String getClaimType();

    public abstract List<AccessRights> getRights();

    public abstract void setRights(List<AccessRights> rights);

    public abstract String getKeyName();

    public abstract void setKeyName(String keyName);

    public Instant getCreatedTime() {
        return createdTime;
    }

    void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }

    void setModifiedTime(Instant modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    abstract String getClaimValue();
}
