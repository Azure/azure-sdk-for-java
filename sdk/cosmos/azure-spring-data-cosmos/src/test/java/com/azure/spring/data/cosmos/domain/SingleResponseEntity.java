// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Document()
public class SingleResponseEntity {
    @Id
    private String entityId;

    private String entityTitle;

    public SingleResponseEntity() {
    }

    public SingleResponseEntity(String entityId, String entityTitle) {
        this.entityId = entityId;
        this.entityTitle = entityTitle;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityTitle() {
        return entityTitle;
    }

    public void setEntityTitle(String entityTitle) {
        this.entityTitle = entityTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingleResponseEntity that = (SingleResponseEntity) o;
        return Objects.equals(entityId, that.entityId)
            && Objects.equals(entityTitle, that.entityTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, entityTitle);
    }

    @Override
    public String toString() {
        return "SingleResponseEntity{"
            + "entityId='" + entityId + '\''
            + ", entityTitle='" + entityTitle + '\''
            + '}';
    }
}
