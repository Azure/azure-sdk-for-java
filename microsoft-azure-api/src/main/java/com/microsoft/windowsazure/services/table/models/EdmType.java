/**
 * Copyright Microsoft Corporation
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
 * A class containing string constants used to represent the primitive types of the Entity Data Model (EDM) in the Open
 * Data Protocol (OData). The EDM is the underlying abstract data model used by OData services. The subset defined in
 * this class is used for data type definitions of the properties of a table entity in the Windows Azure Storage
 * service.
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
 * <td><strong>Edm.DateTime</strong></td>
 * <td>{@link EdmType#DATETIME}</td>
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
 * For more information about OData, see the <a href="http://www.odata.org/">Open Data Protocol</a> website.
 * <p>
 * For an overview of the available EDM primitive data types and names, see the <a
 * href="http://www.odata.org/developers/protocols/overview#AbstractTypeSystem">Primitive Data Types</a> section of the
 * <a href="http://www.odata.org/developers/protocols/overview">OData Protocol Overview</a>.
 * <p>
 * The Abstract Type System used to define the primitive types supported by OData is defined in detail in <a
 * href="http://msdn.microsoft.com/en-us/library/dd541474.aspx">[MC-CSDL] (section 2.2.1).
 */
public class EdmType {
    /**
     * <strong>Edm.DateTime</strong> Represents date and time with values ranging from 12:00:00 midnight, January 1,
     * 1753 A.D. through 11:59:59 P.M, December 9999 A.D.
     */
    public static final String DATETIME = "Edm.DateTime";

    /**
     * <strong>Edm.Binary</strong> Represents fixed- or variable-length binary data.
     */
    public static final String BINARY = "Edm.Binary";

    /**
     * <strong>Edm.Boolean</strong> Represents the mathematical concept of binary-valued logic.
     */
    public static final String BOOLEAN = "Edm.Boolean";

    /**
     * <strong>Edm.Double</strong> Represents a floating point number with 15 digits precision that can represent values
     * with approximate range of +/- 2.23e -308 through +/- 1.79e +308.
     */
    public static final String DOUBLE = "Edm.Double";

    /**
     * <strong>Edm.Guid</strong> Represents a 16-byte (128-bit) unique identifier value.
     */
    public static final String GUID = "Edm.Guid";

    /**
     * <strong>Edm.Int32</strong> Represents a signed 32-bit integer value.
     */
    public static final String INT32 = "Edm.Int32";

    /**
     * <strong>Edm.Int64</strong> Represents a signed 64-bit integer value.
     */
    public static final String INT64 = "Edm.Int64";

    /**
     * <strong>Edm.String</strong> Represents fixed- or variable-length character data.
     */
    public static final String STRING = "Edm.String";
}
