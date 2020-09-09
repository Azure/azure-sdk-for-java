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

    public Contact() {
    }

    public Contact(String logicId, String title) {
        this.logicId = logicId;
        this.title = title;
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
            + '}';
    }
}
