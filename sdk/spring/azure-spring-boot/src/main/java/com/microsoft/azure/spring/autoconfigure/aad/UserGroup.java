/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import java.io.Serializable;
import java.util.Objects;

public class UserGroup implements Serializable {
    private static final long serialVersionUID = 9064197572478554735L;

    private String objectID;
    private String displayName;

    public UserGroup(String objectID, String displayName) {
        this.objectID = objectID;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getObjectID() {
        return objectID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof UserGroup)) {
            return false;
        }
        final UserGroup group = (UserGroup) o;
        return this.getDisplayName().equals(group.getDisplayName())
                && this.getObjectID().equals(group.getObjectID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectID, displayName);
    }
}
