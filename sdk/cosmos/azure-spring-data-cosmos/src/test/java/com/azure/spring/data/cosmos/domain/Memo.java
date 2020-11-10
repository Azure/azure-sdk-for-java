// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;

import java.util.Date;
import java.util.Objects;

/**
 * For testing date and enum purpose
 */
@Container()
public class Memo {
    private String id;
    private String message;
    private Date date;
    private Importance importance;

    public Memo(String id, String message, Date date, Importance importance) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.importance = importance;
    }

    public Memo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Importance getImportance() {
        return importance;
    }

    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Memo memo = (Memo) o;
        return Objects.equals(id, memo.id)
            && Objects.equals(message, memo.message)
            && Objects.equals(date, memo.date)
            && importance == memo.importance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, date, importance);
    }

    @Override
    public String toString() {
        return "Memo{"
            + "id='"
            + id
            + '\''
            + ", message='"
            + message
            + '\''
            + ", date="
            + date
            + ", importance="
            + importance
            + '}';
    }
}

