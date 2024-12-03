// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/** The properties that enable an account to host a static website. */
public final class DataLakeStaticWebsite {
    /*
     * Indicates whether this account is hosting a static website
     */
    private boolean enabled;

    /*
     * The default name of the index page under each directory
     */
    private String indexDocument;

    /*
     * The absolute path of the custom 404 page
     */
    private String errorDocument404Path;

    /*
     * Absolute path of the default index page
     */
    private String defaultIndexDocumentPath;

    /**
     * Get the enabled property: Indicates whether this account is hosting a static website.
     *
     * @return the enabled value.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled property: Indicates whether this account is hosting a static website.
     *
     * @param enabled the enabled value to set.
     * @return the DataLakeStaticWebsite object itself.
     */
    public DataLakeStaticWebsite setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the indexDocument property: The default name of the index page under each directory.
     *
     * @return the indexDocument value.
     */
    public String getIndexDocument() {
        return this.indexDocument;
    }

    /**
     * Set the indexDocument property: The default name of the index page under each directory.
     *
     * @param indexDocument the indexDocument value to set.
     * @return the DataLakeStaticWebsite object itself.
     */
    public DataLakeStaticWebsite setIndexDocument(String indexDocument) {
        this.indexDocument = indexDocument;
        return this;
    }

    /**
     * Get the errorDocument404Path property: The absolute path of the custom 404 page.
     *
     * @return the errorDocument404Path value.
     */
    public String getErrorDocument404Path() {
        return this.errorDocument404Path;
    }

    /**
     * Set the errorDocument404Path property: The absolute path of the custom 404 page.
     *
     * @param errorDocument404Path the errorDocument404Path value to set.
     * @return the DataLakeStaticWebsite object itself.
     */
    public DataLakeStaticWebsite setErrorDocument404Path(String errorDocument404Path) {
        this.errorDocument404Path = errorDocument404Path;
        return this;
    }

    /**
     * Get the defaultIndexDocumentPath property: Absolute path of the default index page.
     *
     * @return the defaultIndexDocumentPath value.
     */
    public String getDefaultIndexDocumentPath() {
        return this.defaultIndexDocumentPath;
    }

    /**
     * Set the defaultIndexDocumentPath property: Absolute path of the default index page.
     *
     * @param defaultIndexDocumentPath the defaultIndexDocumentPath value to set.
     * @return the DataLakeStaticWebsite object itself.
     */
    public DataLakeStaticWebsite setDefaultIndexDocumentPath(String defaultIndexDocumentPath) {
        this.defaultIndexDocumentPath = defaultIndexDocumentPath;
        return this;
    }

}
