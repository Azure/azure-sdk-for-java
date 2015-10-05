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

package com.microsoft.azure.storage.table;

/**
 * Reserved for internal use. A class that holds relevant constants for interacting with OData feeds.
 */
final class ODataConstants {

    /**
     * The <code>String</code> representation of the Atom Entry <em>etag</em> element name.
     */
    public static final String ETAG = "etag";

    /**
     * The <code>String</code> representation of the JSON annotation prefix
     */
    public static final String ODATA_PREFIX = "odata.";

    /**
     * The <code>String</code> representation of the JSON annotation edm type suffix
     */
    public static final String ODATA_TYPE_SUFFIX = "@odata.type";

    /**
     * The <code>String</code> representation of the JSON value object name
     */
    public static final String VALUE = "value";

    /**
     * The <code>String</code> representation of the <em>Edm.DateTime</em> metadata type attribute value.
     */
    public static final String EDMTYPE_DATETIME = "Edm.DateTime";

    /**
     * The <code>String</code> representation of the <em>Edm.Binary</em> metadata type attribute value.
     */
    public static final String EDMTYPE_BINARY = "Edm.Binary";

    /**
     * The <code>String</code> representation of the <em>Edm.Boolean</em> metadata type attribute value.
     */
    public static final String EDMTYPE_BOOLEAN = "Edm.Boolean";

    /**
     * The <code>String</code> representation of the <em>Edm.Double</em> metadata type attribute value.
     */
    public static final String EDMTYPE_DOUBLE = "Edm.Double";

    /**
     * The <code>String</code> representation of the <em>Edm.Guid</em> metadata type attribute value.
     */
    public static final String EDMTYPE_GUID = "Edm.Guid";

    /**
     * The <code>String</code> representation of the <em>Edm.Int32</em> metadata type attribute value.
     */
    public static final String EDMTYPE_INT32 = "Edm.Int32";

    /**
     * The <code>String</code> representation of the <em>Edm.Int64</em> metadata type attribute value.
     */
    public static final String EDMTYPE_INT64 = "Edm.Int64";

    /**
     * The <code>String</code> representation of the <em>Edm.String</em> metadata type attribute value.
     */
    public static final String EDMTYPE_STRING = "Edm.String";
}
