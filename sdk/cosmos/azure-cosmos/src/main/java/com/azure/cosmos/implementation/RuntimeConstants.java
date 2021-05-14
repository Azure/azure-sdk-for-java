// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * Used internally. Runtime constants in the Azure Cosmos DB database service Java SDK.
 */
public class RuntimeConstants {
    public static class MediaTypes {
        // http://www.iana.org/assignments/media-types/media-types.xhtml
        public static final String ANY = "*/*";
        public static final String IMAGE_JPEG = "image/jpeg";
        public static final String IMAGE_PNG = "image/png";
        public static final String JAVA_SCRIPT = "application/x-javascript";
        public static final String JSON = "application/json";
        public static final String OCTET_STREAM = "application/octet-stream";
        public static final String QUERY_JSON = "application/query+json";
        public static final String SQL = "application/sql";
        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String XML = "application/xml";
        public static final String JSON_PATCH = "application/json-patch+json";
    }

    public static class ProtocolScheme {
        public static final String HTTPS = "https";
        public static final String TCP = "rntbd";
    }

    public static class Encoding {
        public static final String GZIP = "gzip";
    }

    static class Separators {
        static final char[] Url = new char[] {'/'};
        static final char[] Quote = new char[] {'\''};
        static final char[] DomainId = new char[] {'-'};
        static final char[] Query = new char[] {'?', '&', '='};
        static final char[] Parenthesis = new char[] {'(', ')'};
        static final char[] UserAgentHeader = new char[] {'(', ')', ';', ','};


        //Note that the accept header separator here is ideally comma. Semicolon is used for separators within individual
        //header for now cloud moe does not recognize such accept header hence we allow both semicolon or comma separated
        //accept header
        static final char[] Header = new char[] {';', ','};
        static final char[] CookieSeparator = new char[] {';'};
        static final char[] CookieValueSeparator = new char[] {'='};
        static final char[] PPMUserToken = new char[] {':'};
        static final char[] Identifier = new char[] {'-'};
        static final char[] Host = new char[] {'.'};
        static final char[] Version = new char[] {','};
        static final char[] Pair = new char[] {';'};
        static final char[] ETag = new char[] {'#'};
        static final char[] MemberQuery = new char[] {'+'};

        static final String HeaderEncodingBegin = "=?";
        static final String HeaderEncodingEnd = "?=";
        static final String HeaderEncodingSeparator = "?";
    }
}
