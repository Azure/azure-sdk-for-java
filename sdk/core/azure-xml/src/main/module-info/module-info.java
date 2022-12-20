// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.xml {
    requires java.xml;

    exports com.azure.xml;
    exports com.azure.xml.implementation;

    uses com.azure.xml.XmlProvider;
}
