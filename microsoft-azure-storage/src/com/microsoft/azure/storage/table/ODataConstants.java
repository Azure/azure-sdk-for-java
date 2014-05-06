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
     * The <code>String</code> representation of the Atom namespace.
     */
    public static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    /**
     * The <code>String</code> representation of the OData Data namespace.
     */
    public static final String DATA_SERVICES_NS = "http://schemas.microsoft.com/ado/2007/08/dataservices";

    /**
     * The <code>String</code> representation of the OData Metadata namespace.
     */
    public static final String DATA_SERVICES_METADATA_NS = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";

    /**
     * The <code>String</code> representation of the Atom namespace in brackets.
     */
    public static final String BRACKETED_ATOM_NS = "{" + ATOM_NS + "}"; // default

    /**
     * The <code>String</code> representation of the OData Data namespace in brackets.
     */
    public static final String BRACKETED_DATA_SERVICES_NS = "{" + DATA_SERVICES_NS + "}"; // d:

    /**
     * The <code>String</code> representation of the OData Metadata namespace in brackets.
     */
    public static final String BRACKETED_DATA_SERVICES_METADATA_NS = "{" + DATA_SERVICES_METADATA_NS + "}"; // m:

    /**
     * The <code>String</code> representation of the Atom Entry <em>feed</em> element name.
     */
    public static final String FEED = "feed";

    /**
     * The <code>String</code> representation of the Atom Entry <em>title</em> element name.
     */
    public static final String TITLE = "title";

    /**
     * The <code>String</code> representation of the Atom Entry <em>id</em> element name.
     */
    public static final String ID = "id";

    /**
     * The <code>String</code> representation of the Atom Entry <em>updated</em> element name.
     */
    public static final String UPDATED = "updated";

    /**
     * The <code>String</code> representation of the Atom Entry <em>link</em> element name.
     */
    public static final String LINK = "link";

    /**
     * The <code>String</code> representation of the Atom Entry <em>author</em> element name.
     */
    public static final String AUTHOR = "author";

    /**
     * The <code>String</code> representation of the Atom Entry <em>name</em> element name.
     */
    public static final String NAME = "name";

    /**
     * The <code>String</code> representation of the Atom Entry <em>entry</em> element name.
     */
    public static final String ENTRY = "entry";

    /**
     * The <code>String</code> representation of the Atom Entry <em>category</em> element name.
     */
    public static final String CATEGORY = "category";

    /**
     * The <code>String</code> representation of the Atom Entry <em>content</em> element name.
     */
    public static final String CONTENT = "content";

    /**
     * The <code>String</code> representation of the OData Metadata <em>properties</em> element name.
     */
    public static final String PROPERTIES = "properties";

    /**
     * The <code>String</code> representation of the Atom Entry <em>etag</em> element name.
     */
    public static final String ETAG = "etag";

    /**
     * The <code>String</code> representation of the <em>type</em> attribute name.
     */
    public static final String TYPE = "type";

    /**
     * The <code>String</code> representation of the <em>term</em> element name.
     */
    public static final String TERM = "term";

    /**
     * The <code>String</code> representation of <em>scheme</em>.
     */
    public static final String SCHEME = "scheme";

    /**
     * The <code>String</code> representation of <em>href</em>.
     */
    public static final String HREF = "href";

    /**
     * The <code>String</code> representation of <em>rel</em>.
     */
    public static final String REL = "rel";

    /**
     * The <code>String</code> representation of the <em>null</em> attribute name.
     */
    public static final String NULL = "null";

    /**
     * The <code>String</code> representation of the content type attribute value to send.
     */
    public static final String ODATA_CONTENT_TYPE = "application/xml";

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
