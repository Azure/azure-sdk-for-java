// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * Represents a SQL parameter in the SqlQuerySpec used for queries in the Azure Cosmos DB database service.
 */
public final class SqlParameter extends JsonSerializable {


    /**
     * Initializes a new instance of the SqlParameter class.
     */
    public SqlParameter() {
        super();
    }

    /**
     * Initializes a new instance of the SqlParameter class with the name and value of the parameter.
     *
     * @param name  the name of the parameter.
     * @param value the value of the parameter.
     */
    public SqlParameter(String name, Object value) {
        super();
        this.name(name);
        this.value(value);
    }

    /**
     * Gets the name of the parameter.
     *
     * @return the name of the parameter.
     */
    public String name() {
        return super.getString("name");
    }

    /**
     * Sets the name of the parameter.
     *
     * @param name the name of the parameter.
     * @return the SqlParameter.
     */
    public SqlParameter name(String name) {
        super.set("name", name);
        return this;
    }

    /**
     * Gets the value of the parameter.
     *
     * @param c    the class of the parameter value.
     * @param <T>  the type of the parameter
     * @return     the value of the parameter.
     */
    public <T> Object value(Class<T> c) {
        return super.getObject("value", c);
    }

    /**
     * Sets the value of the parameter.
     *
     * @param value the value of the parameter.
     * @return the SqlParameter.
     */
    public SqlParameter value(Object value) {
        super.set("value", value);
        return this;
    }
}
