// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ClientTelemetrySerializer extends StdSerializer<ClientTelemetryInfo> {

    private static final long serialVersionUID = -2746532297176812860L;

    ClientTelemetrySerializer() {
        super(ClientTelemetryInfo.class);
    }

    @Override
    public void serialize(ClientTelemetryInfo telemetry, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("timeStamp", telemetry.getTimeStamp());
        generator.writeStringField("clientId", telemetry.getClientId());

        if (telemetry.getProcessId() != null) {
            generator.writeStringField("processId", telemetry.getProcessId());
        }

        if (telemetry.getUserAgent() != null) {
            generator.writeStringField("userAgent", telemetry.getUserAgent());
        }

        generator.writeStringField("connectionMode", telemetry.getConnectionMode().toString());
        generator.writeStringField("globalDatabaseAccountName",
            telemetry.getGlobalDatabaseAccountName());

        if (telemetry.getApplicationRegion() != null) {
            generator.writeStringField("applicationRegion", telemetry.getApplicationRegion());
        }

        if (telemetry.getHostEnvInfo() != null) {
            generator.writeStringField("hostEnvInfo", telemetry.getHostEnvInfo());
        }

        if (telemetry.getAcceleratedNetworking() != null) {
            generator.writeStringField("acceleratedNetworking",
                telemetry.getAcceleratedNetworking().toString());
        }

        generator.writeObjectField("systemInfo", telemetry.getSystemInfoMap().keySet());
        generator.writeObjectField("cacheRefreshInfo", telemetry.getCacheRefreshInfoMap().keySet());
        generator.writeObjectField("operationInfo", telemetry.getOperationInfoMap().keySet());
        generator.writeEndObject();
    }
}
