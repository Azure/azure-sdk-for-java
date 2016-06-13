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
     * the name of the database.
     */
    private String databaseName;

    /**
     * the name of the schema associated with this table and database.
     */
    private String schemaName;

    /**
     * the name of type for this type.
     */
    @JsonProperty(value = "typeName")
    private String name;

    /**
     * the type family for this type.
     */
    private String typeFamily;

    /**
     * the C# name for this type.
     */
    private String cSharpName;

    /**
     * the fully qualified C# name for this type.
     */
    private String fullCSharpName;

    /**
     * the system type ID for this type.
     */
    private Integer systemTypeId;

    /**
     * the user type ID for this type.
     */
    private Integer userTypeId;

    /**
     * the schema ID for this type.
     */
    private Integer schemaId;

    /**
     * the principal ID for this type.
     */
    private Integer principalId;

    /**
     * the the switch indicating if this type is nullable.
     */
    private Boolean isNullable;

    /**
     * the the switch indicating if this type is user defined.
     */
    private Boolean isUserDefined;

    /**
     * the the switch indicating if this type is an assembly type.
     */
    private Boolean isAssemblyType;

    /**
     * the the switch indicating if this type is a table type.
     */
    private Boolean isTableType;

    /**
     * the the switch indicating if this type is a complex type.
     */
    private Boolean isComplexType;

    /**
     * Get the databaseName value.
     *
     * @return the databaseName value
     */
    public String databaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     * @return the USqlType object itself.
     */
    public USqlType withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    /**
     * Get the schemaName value.
     *
     * @return the schemaName value
     */
    public String schemaName() {
        return this.schemaName;
    }

    /**
     * Set the schemaName value.
     *
     * @param schemaName the schemaName value to set
     * @return the USqlType object itself.
     */
    public USqlType withSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the USqlType object itself.
     */
    public USqlType withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the typeFamily value.
     *
     * @return the typeFamily value
     */
    public String typeFamily() {
        return this.typeFamily;
    }

    /**
     * Set the typeFamily value.
     *
     * @param typeFamily the typeFamily value to set
     * @return the USqlType object itself.
     */
    public USqlType withTypeFamily(String typeFamily) {
        this.typeFamily = typeFamily;
        return this;
    }

    /**
     * Get the cSharpName value.
     *
     * @return the cSharpName value
     */
    public String cSharpName() {
        return this.cSharpName;
    }

    /**
     * Set the cSharpName value.
     *
     * @param cSharpName the cSharpName value to set
     * @return the USqlType object itself.
     */
    public USqlType withCSharpName(String cSharpName) {
        this.cSharpName = cSharpName;
        return this;
    }

    /**
     * Get the fullCSharpName value.
     *
     * @return the fullCSharpName value
     */
    public String fullCSharpName() {
        return this.fullCSharpName;
    }

    /**
     * Set the fullCSharpName value.
     *
     * @param fullCSharpName the fullCSharpName value to set
     * @return the USqlType object itself.
     */
    public USqlType withFullCSharpName(String fullCSharpName) {
        this.fullCSharpName = fullCSharpName;
        return this;
    }

    /**
     * Get the systemTypeId value.
     *
     * @return the systemTypeId value
     */
    public Integer systemTypeId() {
        return this.systemTypeId;
    }

    /**
     * Set the systemTypeId value.
     *
     * @param systemTypeId the systemTypeId value to set
     * @return the USqlType object itself.
     */
    public USqlType withSystemTypeId(Integer systemTypeId) {
        this.systemTypeId = systemTypeId;
        return this;
    }

    /**
     * Get the userTypeId value.
     *
     * @return the userTypeId value
     */
    public Integer userTypeId() {
        return this.userTypeId;
    }

    /**
     * Set the userTypeId value.
     *
     * @param userTypeId the userTypeId value to set
     * @return the USqlType object itself.
     */
    public USqlType withUserTypeId(Integer userTypeId) {
        this.userTypeId = userTypeId;
        return this;
    }

    /**
     * Get the schemaId value.
     *
     * @return the schemaId value
     */
    public Integer schemaId() {
        return this.schemaId;
    }

    /**
     * Set the schemaId value.
     *
     * @param schemaId the schemaId value to set
     * @return the USqlType object itself.
     */
    public USqlType withSchemaId(Integer schemaId) {
        this.schemaId = schemaId;
        return this;
    }

    /**
     * Get the principalId value.
     *
     * @return the principalId value
     */
    public Integer principalId() {
        return this.principalId;
    }

    /**
     * Set the principalId value.
     *
     * @param principalId the principalId value to set
     * @return the USqlType object itself.
     */
    public USqlType withPrincipalId(Integer principalId) {
        this.principalId = principalId;
        return this;
    }

    /**
     * Get the isNullable value.
     *
     * @return the isNullable value
     */
    public Boolean isNullable() {
        return this.isNullable;
    }

    /**
     * Set the isNullable value.
     *
     * @param isNullable the isNullable value to set
     * @return the USqlType object itself.
     */
    public USqlType withIsNullable(Boolean isNullable) {
        this.isNullable = isNullable;
        return this;
    }

    /**
     * Get the isUserDefined value.
     *
     * @return the isUserDefined value
     */
    public Boolean isUserDefined() {
        return this.isUserDefined;
    }

    /**
     * Set the isUserDefined value.
     *
     * @param isUserDefined the isUserDefined value to set
     * @return the USqlType object itself.
     */
    public USqlType withIsUserDefined(Boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
        return this;
    }

    /**
     * Get the isAssemblyType value.
     *
     * @return the isAssemblyType value
     */
    public Boolean isAssemblyType() {
        return this.isAssemblyType;
    }

    /**
     * Set the isAssemblyType value.
     *
     * @param isAssemblyType the isAssemblyType value to set
     * @return the USqlType object itself.
     */
    public USqlType withIsAssemblyType(Boolean isAssemblyType) {
        this.isAssemblyType = isAssemblyType;
        return this;
    }

    /**
     * Get the isTableType value.
     *
     * @return the isTableType value
     */
    public Boolean isTableType() {
        return this.isTableType;
    }

    /**
     * Set the isTableType value.
     *
     * @param isTableType the isTableType value to set
     * @return the USqlType object itself.
     */
    public USqlType withIsTableType(Boolean isTableType) {
        this.isTableType = isTableType;
        return this;
    }

    /**
     * Get the isComplexType value.
     *
     * @return the isComplexType value
     */
    public Boolean isComplexType() {
        return this.isComplexType;
    }

    /**
     * Set the isComplexType value.
     *
     * @param isComplexType the isComplexType value to set
     * @return the USqlType object itself.
     */
    public USqlType withIsComplexType(Boolean isComplexType) {
        this.isComplexType = isComplexType;
        return this;
    }

}
