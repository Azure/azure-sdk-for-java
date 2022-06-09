// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module azure.core.http.apache {
    requires transitive com.azure.core;

    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.apache.httpcomponents.core5.httpcore5.reactive;
    requires org.apache.httpcomponents.core5.httpcore5.h2;

    exports com.azure.core.http.apache;


}
