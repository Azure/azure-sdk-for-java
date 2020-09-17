// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

/**
 * Utility class for ETag state handling.
 */
public class ETagState {
    private boolean doImplicitETagCheckOnCreate;
    private boolean doImplicitETagCheckOnUpdate;
    private String eTagOnUpdate;
    private String eTagOnDelete;

    /**
     * Specifies if it is in create or update with implicit ETag check.
     * @param isInCreateMode The boolean flag to indicate if it is in create mode.
     * @return the ETag state.
     */
    public ETagState withImplicitETagCheckOnCreateOrUpdate(boolean isInCreateMode) {
        if (isInCreateMode) {
            this.doImplicitETagCheckOnCreate = true;
        } else {
            this.doImplicitETagCheckOnUpdate = true;
        }
        return this;
    }

    /**
     * Specifies the explicit ETag value on update.
     * @param eTagValue The explicit ETag value.
     * @return the ETag state.
     */
    public ETagState withExplicitETagCheckOnUpdate(String eTagValue) {
        this.eTagOnUpdate = eTagValue;
        return this;
    }

    /**
     * Specifies the explicit ETag value on delete.
     * @param eTagValue The explicit ETag value.
     * @return the ETag state.
     */
    public ETagState withExplicitETagCheckOnDelete(String eTagValue) {
        this.eTagOnDelete = eTagValue;
        return this;
    }


    /**
     * Clears the stored values in ETag state
     * @return the ETag state.
     */
    public ETagState clear() {
        this.doImplicitETagCheckOnCreate = false;
        this.doImplicitETagCheckOnUpdate = false;
        this.eTagOnUpdate = null;
        this.eTagOnDelete = null;
        return this;
    }

    /**
     * Specifies the ETag value for If-Match header on update.
     * @param currentETagValue The current ETag value.
     * @return the ETag value.
     */
    public String ifMatchValueOnUpdate(String currentETagValue) {
        String eTagValue = null;
        if (this.doImplicitETagCheckOnUpdate) {
            eTagValue = currentETagValue;
        }
        if (this.eTagOnUpdate != null) {
            eTagValue = this.eTagOnUpdate;
        }
        return eTagValue;
    }

    /**
     * Specifies the ETag value for If-Match header on delete.
     * @return the ETag value.
     */
    public String ifMatchValueOnDelete() {
        return this.eTagOnDelete;
    }

    /**
     * Specifies the ETag value for If-None-Match header on create.
     * @return the ETag value.
     */
    public String ifNonMatchValueOnCreate() {
        if (this.doImplicitETagCheckOnCreate) {
            return "*";
        }
        return null;
    }
}
