// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Container()
public class Contact {
    @Id
    private String logicId;

    private String title;

    private int intValue;

    private boolean isActive;

    public Contact() {
    }

    public Contact(String logicId, String title) {
        this.logicId = logicId;
        this.title = title;
    }

    public Contact(final String logicId, final String title, final int intValue, boolean isActive) {
        this.logicId = logicId;
        this.title = title;
        this.intValue = intValue;
        this.isActive = isActive;
    }

    public String getLogicId() {
        return logicId;
    }

    public void setLogicId(String logicId) {
        this.logicId = logicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Setter for property 'status'.
     *
     * @param active Value to set for property 'status'.
     */
    public void setActive(final boolean active) {
        this.isActive = active;
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * Setter for property 'value'.
     *
     * @param intValue Value to set for property 'value'.
     */
    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contact contact = (Contact) o;
        return Objects.equals(logicId, contact.logicId)
            && Objects.equals(title, contact.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicId, title);
    }

    @Override
    public String toString() {
        return "Contact{"
                   + "logicId='"
                   + logicId
                   + '\''
                   + ", title='"
                   + title
                   + '\''
                   + ", value='"
                   + intValue
                   + '\''
                   + ", status='"
                   + isActive
                   + '\''
                   + '}';
    }
}
