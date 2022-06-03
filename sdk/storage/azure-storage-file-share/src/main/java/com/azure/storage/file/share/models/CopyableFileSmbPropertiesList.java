// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import java.util.ArrayList;
import java.util.List;

/**
 * This type allows users to specify optional smb properties to be copied from the source file.
 */
public final class CopyableFileSmbPropertiesList {
    private boolean isSetNone;
    private boolean isSetFileAttributes;
    private boolean isSetCreatedOn;
    private boolean isSetLastWrittenOn;
    private boolean isSetChangedOn;
    private boolean isSetAll;

    /***
     * @return a flag indicating if no smb properties should be copied from source file.
     */
    public Boolean isNone() {
        return isSetNone;
    }

    /***
     * @param isNone Flag indicating whether no smb properties should be copied
     * @return the updated {@link CopyableFileSmbPropertiesList}
     */
    public CopyableFileSmbPropertiesList setNone(boolean isNone) {
        isSetNone = isNone;
        return this;
    }

    /***
     * @return a flag indicating whether file attributes should be copied from source file.
     */
    public Boolean isFileAttributes() {
        return isSetFileAttributes;

    }

    /**
     * @param fileAttributes Flag indicating whether to copy file attributes from source file
     * @return the updated {@link CopyableFileSmbPropertiesList}
     */
    public CopyableFileSmbPropertiesList setFileAttributes(boolean fileAttributes) {
        isSetFileAttributes = fileAttributes;
        return this;
    }

    /**
     * @return a flag indicating whether created on timestamp should be copied from source file.
     */
    public Boolean isCreatedOn() {
        return isSetCreatedOn;
    }

    /**
     * @param createdOn Flag indicating whether to copy created on timestamp from source file
     * @return the updated {@link CopyableFileSmbPropertiesList}
     */
    public CopyableFileSmbPropertiesList setCreatedOn(boolean createdOn) {
        isSetCreatedOn = createdOn;
        return this;
    }

    /**
     * @return a flag indicating whether last written on timestamp should be copied from source file.
     */
    public Boolean isLastWrittenOn() {
        return isSetLastWrittenOn;
    }

    /**
     * @param lastWrittenOn Flag indicating whether to copy last written on timestamp from source file
     * @return the updated {@link CopyableFileSmbPropertiesList}
     */
    public CopyableFileSmbPropertiesList setLastWrittenOn(boolean lastWrittenOn) {
        isSetLastWrittenOn = lastWrittenOn;
        return this;
    }

    /**
     * @return a flag indicating whether changed on timestamp should be copied from source file.
     */
    public Boolean isChangedOn() {
        return isSetChangedOn;
    }

    /**
     * @param changedOn Flag indicating whether to copy changed on timestamp from source file
     * @return the updated {@link CopyableFileSmbPropertiesList}
     */
    public CopyableFileSmbPropertiesList setChangedOn(boolean changedOn) {
        isSetChangedOn = changedOn;
        return this;
    }

    /**
     * @return a flag indicating whether all attributes should be copied from source file.
     */
    public Boolean isAll() {
        return isSetAll;
    }

    /**
     * @param setAll Flag indicating whether to copy all attributes from source file
     * @return the updated {@link CopyableFileSmbPropertiesList}
     */
    public CopyableFileSmbPropertiesList setAll(boolean setAll) {
        isSetAll = setAll;
        return this;
    }

    /**
     * @return a list of the flag set to true
     */
    public List<CopyableFileSmbProperties> toList() {
        List<CopyableFileSmbProperties> details = new ArrayList<>();
        if (this.isSetNone) {
            details.add(CopyableFileSmbProperties.NONE);
        }
        if (this.isSetFileAttributes) {
            details.add(CopyableFileSmbProperties.FILE_ATTRIBUTES);
        }
        if (this.isSetFileAttributes) {
            details.add(CopyableFileSmbProperties.FILE_ATTRIBUTES);
        }
        if (this.isSetCreatedOn) {
            details.add(CopyableFileSmbProperties.CREATED_ON);
        }
        if (this.isSetLastWrittenOn) {
            details.add(CopyableFileSmbProperties.LAST_WRITTEN_ON);
        }
        if (this.isSetChangedOn) {
            details.add(CopyableFileSmbProperties.CHANGED_ON);
        }
        if (this.isSetAll) {
            details.add(CopyableFileSmbProperties.ALL);
        }
        return details;
    }
}
