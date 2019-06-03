package com.azure.keyvault.certificates.models;

import java.time.OffsetDateTime;

public class IssuerBase {
    private String id;
    private String provider;
    private String name;
    private Boolean enabled;
    private OffsetDateTime created;
    private OffsetDateTime updated;

    public IssuerBase(String name){
        this.name = name;
    }

    public IssuerBase(){

    }

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public String provider() {
        return provider;
    }

    public void provider(String provider) {
        this.provider = provider;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public Boolean enabled() {
        return enabled;
    }

    public void enabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public OffsetDateTime created() {
        return created;
    }

    public void created(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime updated() {
        return updated;
    }

    public void updated(OffsetDateTime updated) {
        this.updated = updated;
    }
}
