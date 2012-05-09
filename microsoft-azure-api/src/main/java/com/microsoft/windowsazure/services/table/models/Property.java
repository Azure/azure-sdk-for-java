/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

/**
 * Represents a table entity property value as a data-type and value pair.
 * <p>
 * The following table shows the supported property data types in Windows Azure storage and the corresponding Java types
 * when deserialized.
 * <TABLE BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0">
 * <TR BGCOLOR="#EEEEFF" CLASS="TableSubHeadingColor">
 * <th>Storage Type</th>
 * <th>EdmType Value</th>
 * <th>Java Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><strong>Edm.Binary</strong></td>
 * <td>{@link EdmType#BINARY}</td>
 * <td><code>byte[], Byte[]</code></td>
 * <td>An array of bytes up to 64 KB in size.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Boolean</strong></td>
 * <td>{@link EdmType#BOOLEAN}</td>
 * <td><code>boolean, Boolean</code></td>
 * <td>A Boolean value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Byte</strong></td>
 * <td>{@link EdmType#BYTE}</td>
 * <td><code>byte, Byte</code></td>
 * <td>An 8-bit integer value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.DateTime</strong></td>
 * <td>{@link EdmType#DATE_TIME}</td>
 * <td><code>Date</code></td>
 * <td>A 64-bit value expressed as Coordinated Universal Time (UTC). The supported range begins from 12:00 midnight,
 * January 1, 1601 A.D. (C.E.), UTC. The range ends at December 31, 9999.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Double</strong></td>
 * <td>{@link EdmType#DOUBLE}</td>
 * <td><code>double, Double</code></td>
 * <td>A 64-bit double-precision floating point value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Guid</strong></td>
 * <td>{@link EdmType#GUID}</td>
 * <td><code>UUID</code></td>
 * <td>A 128-bit globally unique identifier.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Int32</strong></td>
 * <td>{@link EdmType#INT32}</td>
 * <td><code>int, Integer</code></td>
 * <td>A 32-bit integer value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.Int64</strong></td>
 * <td>{@link EdmType#INT64}</td>
 * <td><code>long, Long</code></td>
 * <td>A 64-bit integer value.</td>
 * </tr>
 * <tr>
 * <td><strong>Edm.String</strong></td>
 * <td>{@link EdmType#STRING}</td>
 * <td><code>String</code></td>
 * <td>A UTF-16-encoded value. String values may be up to 64 KB in size.</td>
 * </tr>
 * </table>
 * <p>
 * See the MSDN topic <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179338.aspx">Understanding the
 * Table Service Data Model</a> for an overview of tables, entities, and properties as used in the Windows Azure Storage
 * service.
 * <p>
 * For an overview of the available EDM primitive data types and names, see the <a
 * href="http://www.odata.org/developers/protocols/overview#AbstractTypeSystem">Primitive Data Types</a> section of the
 * <a href="http://www.odata.org/developers/protocols/overview">OData Protocol Overview</a>.
 */
public class Property {
    private String edmType;
    private Object value;

    /**
     * Gets the EDM data type of the property.
     * 
     * @return
     *         A <code>String</code> containing the EDM data type of the property.
     */
    public String getEdmType() {
        return edmType;
    }

    /**
     * Sets the EDM data type of the property. The <em>edmType</em> parameter must be set to one of the string constants
     * defined in the {@link EdmType} class.
     * 
     * @param edmType
     *            A {@link String} containing the EDM data type to associate with the property value. This must be one
     *            of the supported EDM types, defined as string constants in the {@link EdmType} class.
     * @return
     *         A reference to this {@link Property} instance.
     */
    public Property setEdmType(String edmType) {
        this.edmType = edmType;
        return this;
    }

    /**
     * Gets the data value of the property.
     * 
     * @return
     *         An {@link Object} containing the data value of the property.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the data value of the property. The <em>value</em> parameter must contain an {@link Object} serializable as
     * the associated EDM data type of the property.
     * 
     * @param value
     *            An {@link Object} containing the data value of the property, serializable as the associated EDM data
     *            type of the property.
     * @return
     *         A reference to this {@link Property} instance.
     */
    public Property setValue(Object value) {
        this.value = value;
        return this;
    }
}