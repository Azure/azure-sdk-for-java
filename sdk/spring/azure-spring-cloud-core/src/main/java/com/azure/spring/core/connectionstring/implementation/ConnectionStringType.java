// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring.implementation;

enum ConnectionStringType {

    EVENT_HUB(new String[] {
        "Endpoint=<>;SharedAccessKeyName=<>;SharedAccessKey=<>;",
        "Endpoint=<>;SharedAccessKeyName=<>;SharedAccessKey=<>;EntityPath=<>",
    }),
    SERVICE_BUS(new String[] {
        "Endpoint=<>;SharedAccessKeyName=<>;SharedAccessKey=<>;",
        "Endpoint=<>;SharedAccessKeyName=<>;SharedAccessKey=<>;EntityPath=<>",
        "Endpoint=<>;SharedAccessSignature=SharedAccessSignature <>",
        "Endpoint=<>;SharedAccessSignature=SharedAccessSignature <>;EntityPath=<>"
    }),
    STORAGE(new String[] {
        "DefaultEndpointsProtocol=<>;AccountName=<>;AccountKey=<>;EndpointSuffix=<>"
    }),
    APP_CONFIGURATION(new String[] {
        "Endpoint=<>;Id=<>;Secret=<>"
    });
    private final String[] schemas;

    ConnectionStringType(String[] schemas) {
        this.schemas = schemas;
    }

    public String[] getSchemas() {
        return schemas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ConnectionStringType{schemas=[").append(System.lineSeparator());

        for (String schema : this.getSchemas()) {
            sb.append("\t").append(schema).append(System.lineSeparator());
        }

        sb.append("]}");
        return sb.toString();
    }
}
