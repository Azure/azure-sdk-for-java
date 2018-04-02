/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

/**
 * Represents a SQL parameter in the SqlQuerySpec used for queries in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
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
        this.setName(name);
        this.setValue(value);
    }

    /**
     * Gets the name of the parameter.
     *
     * @return the name of the parameter.
     */
    public String getName() {
        return super.getString("name");
    }

    /**
     * Sets the name of the parameter.
     *
     * @param name the name of the parameter.
     */
    public void setName(String name) {
        super.set("name", name);
    }

    /**
     * Gets the value of the parameter.
     *
     * @param c    the class of the parameter value.
     * @param <T>  the type of the parameter
     * @return     the value of the parameter.
     */
    public <T extends Object> Object getValue(Class<T> c) {
        return super.getObject("value", c);
    }

    /**
     * Sets the value of the parameter.
     *
     * @param value the value of the parameter.
     */
    public void setValue(Object value) {
        super.set("value", value);
    }
}
