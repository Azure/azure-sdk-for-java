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

package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.namespace.QName;

/**
 * This class provides a set of constants for element names and namespaces used
 * throughout the serialization of media services entities.
 */

public final class Constants {

    private Constants() {
    }

    /**
     * XML Namespace for Atom syndication format, as defined by IETF RFC 4287
     */
    public static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    /**
     * XML Namespace for OData data as serialized inside Atom syndication
     * format.
     */
    public static final String ODATA_DATA_NS = "http://schemas.microsoft.com/ado/2007/08/dataservices";

    /**
     * XML Namespace for OData metadata as serialized inside Atom syndication
     * format.
     */
    public static final String ODATA_METADATA_NS = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";

    /**
     * EDM namespace for Azure Media Services entities, as defined in Media
     * Services EDMX file.
     */
    public static final String MEDIA_SERVICES_EDM_NAMESPACE = "Microsoft.Cloud.Media.Vod.Rest.Data.Models";

    /**
     * Element name for Atom content element, including namespace
     */
    public static final QName ATOM_CONTENT_ELEMENT_NAME = new QName("content",
            ATOM_NS);

    /**
     * Element name for OData action elements, including namespace
     */
    public static final QName ODATA_ACTION_ELEMENT_NAME = new QName("action",
            ODATA_METADATA_NS);

    /**
     * Element name for the metadata properties element, including namespace.
     */
    public static final QName ODATA_PROPERTIES_ELEMENT_NAME = new QName(
            "properties", ODATA_METADATA_NS);
}
