// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityRelationRolePropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link HealthcareEntityRelationRole} model.
 */
@Immutable
public final class HealthcareEntityRelationRole {
    /*
     * Role of entity in the relationship. For example: 'CD20-positive diffuse
     * large B-cell lymphoma' has the following entities with their roles in
     * parenthesis:  CD20 (GeneOrProtein), Positive (Expression), diffuse large
     * B-cell lymphoma (Diagnosis).
     */
    private String name;

    private HealthcareEntity entity;

    static {
        HealthcareEntityRelationRolePropertiesHelper.setAccessor(
            new HealthcareEntityRelationRolePropertiesHelper.HealthcareEntityRelationRoleAccessor() {
                @Override
                public void setName(HealthcareEntityRelationRole healthcareEntityRelationRole, String name) {
                    healthcareEntityRelationRole.setName(name);
                }

                @Override
                public void setEntity(HealthcareEntityRelationRole healthcareEntityRelationRole, HealthcareEntity entity) {
                    healthcareEntityRelationRole.setEntity(entity);
                }
            });
    }

    /**
     * Gets the role property: Role of entity in the relationship. For example: 'CD20-positive diffuse large B-cell
     * lymphoma' has the following entities with their roles in parenthesis: CD20 (GeneOrProtein), Positive
     * (Expression), diffuse large B-cell lymphoma (Diagnosis).
     *
     * @return The role name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the healthcare entity object.
     *
     * @return The healthcare entity object.
     */
    public HealthcareEntity getEntity() {
        return this.entity;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setEntity(HealthcareEntity entity) {
        this.entity = entity;
    }
}
