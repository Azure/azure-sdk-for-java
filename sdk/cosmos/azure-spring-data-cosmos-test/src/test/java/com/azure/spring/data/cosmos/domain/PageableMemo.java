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
public class PageableMemo {
    private String id;
    private String message;
    private Date date;
    private Importance importance;

    public PageableMemo() {
    }

    public PageableMemo(String id, String message, Date date, Importance importance) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.importance = importance;
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
        PageableMemo that = (PageableMemo) o;
        return Objects.equals(id, that.id)
            && Objects.equals(message, that.message)
            && Objects.equals(date, that.date)
            && importance == that.importance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, date, importance);
    }

    @Override
    public String toString() {
        return "PageableMemo{"
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
