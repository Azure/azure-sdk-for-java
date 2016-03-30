/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL type item.
 */
public class USqlType extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the schema associated with this table and
     * database.
     */
    private String schemaName;

    /**
     * Gets or sets the name of type for this type.
     */
    @JsonProperty(value = "typeName")
    private String name;

    /**
     * Gets or sets the type family for this type.
     */
    private String typeFamily;

    /**
     * Gets or sets the C# name for this type.
     */
    private String cSharpName;

    /**
     * Gets or sets the fully qualified C# name for this type.
     */
    private String fullCSharpName;

    /**
     * Gets or sets the system type ID for this type.
     */
    private Integer systemTypeId;

    /**
     * Gets or sets the user type ID for this type.
     */
    private Integer userTypeId;

    /**
     * Gets or sets the schema ID for this type.
     */
    private Integer schemaId;

    /**
     * Gets or sets the principal ID for this type.
     */
    private Integer principalId;

    /**
     * Gets or sets the the switch indicating if this type is nullable.
     */
    private Boolean isNullable;

    /**
     * Gets or sets the the switch indicating if this type is user defined.
     */
    private Boolean isUserDefined;

    /**
     * Gets or sets the the switch indicating if this type is an assembly type.
     */
    private Boolean isAssemblyType;

    /**
     * Gets or sets the the switch indicating if this type is a table type.
     */
    private Boolean isTableType;

    /**
     * Gets or sets the the switch indicating if this type is a complex type.
     */
    private Boolean isComplexType;

    /**
     * Get the databaseName value.
     *
     * @return the databaseName value
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Get the schemaName value.
     *
     * @return the schemaName value
     */
    public String getSchemaName() {
        return this.schemaName;
    }

    /**
     * Set the schemaName value.
     *
     * @param schemaName the schemaName value to set
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the typeFamily value.
     *
     * @return the typeFamily value
     */
    public String getTypeFamily() {
        return this.typeFamily;
    }

    /**
     * Set the typeFamily value.
     *
     * @param typeFamily the typeFamily value to set
     */
    public void setTypeFamily(String typeFamily) {
        this.typeFamily = typeFamily;
    }

    /**
     * Get the cSharpName value.
     *
     * @return the cSharpName value
     */
    public String getCSharpName() {
        return this.cSharpName;
    }

    /**
     * Set the cSharpName value.
     *
     * @param cSharpName the cSharpName value to set
     */
    public void setCSharpName(String cSharpName) {
        this.cSharpName = cSharpName;
    }

    /**
     * Get the fullCSharpName value.
     *
     * @return the fullCSharpName value
     */
    public String getFullCSharpName() {
        return this.fullCSharpName;
    }

    /**
     * Set the fullCSharpName value.
     *
     * @param fullCSharpName the fullCSharpName value to set
     */
    public void setFullCSharpName(String fullCSharpName) {
        this.fullCSharpName = fullCSharpName;
    }

    /**
     * Get the systemTypeId value.
     *
     * @return the systemTypeId value
     */
    public Integer getSystemTypeId() {
        return this.systemTypeId;
    }

    /**
     * Set the systemTypeId value.
     *
     * @param systemTypeId the systemTypeId value to set
     */
    public void setSystemTypeId(Integer systemTypeId) {
        this.systemTypeId = systemTypeId;
    }

    /**
     * Get the userTypeId value.
     *
     * @return the userTypeId value
     */
    public Integer getUserTypeId() {
        return this.userTypeId;
    }

    /**
     * Set the userTypeId value.
     *
     * @param userTypeId the userTypeId value to set
     */
    public void setUserTypeId(Integer userTypeId) {
        this.userTypeId = userTypeId;
    }

    /**
     * Get the schemaId value.
     *
     * @return the schemaId value
     */
    public Integer getSchemaId() {
        return this.schemaId;
    }

    /**
     * Set the schemaId value.
     *
     * @param schemaId the schemaId value to set
     */
    public void setSchemaId(Integer schemaId) {
        this.schemaId = schemaId;
    }

    /**
     * Get the principalId value.
     *
     * @return the principalId value
     */
    public Integer getPrincipalId() {
        return this.principalId;
    }

    /**
     * Set the principalId value.
     *
     * @param principalId the principalId value to set
     */
    public void setPrincipalId(Integer principalId) {
        this.principalId = principalId;
    }

    /**
     * Get the isNullable value.
     *
     * @return the isNullable value
     */
    public Boolean getIsNullable() {
        return this.isNullable;
    }

    /**
     * Set the isNullable value.
     *
     * @param isNullable the isNullable value to set
     */
    public void setIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
    }

    /**
     * Get the isUserDefined value.
     *
     * @return the isUserDefined value
     */
    public Boolean getIsUserDefined() {
        return this.isUserDefined;
    }

    /**
     * Set the isUserDefined value.
     *
     * @param isUserDefined the isUserDefined value to set
     */
    public void setIsUserDefined(Boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
    }

    /**
     * Get the isAssemblyType value.
     *
     * @return the isAssemblyType value
     */
    public Boolean getIsAssemblyType() {
        return this.isAssemblyType;
    }

    /**
     * Set the isAssemblyType value.
     *
     * @param isAssemblyType the isAssemblyType value to set
     */
    public void setIsAssemblyType(Boolean isAssemblyType) {
        this.isAssemblyType = isAssemblyType;
    }

    /**
     * Get the isTableType value.
     *
     * @return the isTableType value
     */
    public Boolean getIsTableType() {
        return this.isTableType;
    }

    /**
     * Set the isTableType value.
     *
     * @param isTableType the isTableType value to set
     */
    public void setIsTableType(Boolean isTableType) {
        this.isTableType = isTableType;
    }

    /**
     * Get the isComplexType value.
     *
     * @return the isComplexType value
     */
    public Boolean getIsComplexType() {
        return this.isComplexType;
    }

    /**
     * Set the isComplexType value.
     *
     * @param isComplexType the isComplexType value to set
     */
    public void setIsComplexType(Boolean isComplexType) {
        this.isComplexType = isComplexType;
    }

}
