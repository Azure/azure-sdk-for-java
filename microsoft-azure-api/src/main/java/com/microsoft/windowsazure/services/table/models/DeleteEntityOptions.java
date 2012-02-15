package com.microsoft.windowsazure.services.table.models;

public class DeleteEntityOptions extends TableServiceOptions {
    private String etag;

    @Override
    public DeleteEntityOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getEtag() {
        return etag;
    }

    public DeleteEntityOptions setEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
