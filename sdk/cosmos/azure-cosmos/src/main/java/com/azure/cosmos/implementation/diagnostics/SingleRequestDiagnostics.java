package com.azure.cosmos.implementation.diagnostics;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleRequestDiagnostics implements ICosmosDiagnostics {
    private static Logger logger = LoggerFactory.getLogger(SingleRequestDiagnostics.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AtomicBoolean isDiagnosticsCapturedInPagedFlux = new AtomicBoolean(false);
    private ClientSideRequestStatistics clientSideRequestStatistics;

    public SingleRequestDiagnostics(DiagnosticsClientContext diagnosticsClientContext, GlobalEndpointManager globalEndpointManager) {
        clientSideRequestStatistics = new ClientSideRequestStatistics(diagnosticsClientContext, globalEndpointManager);
    }

    public ClientSideRequestStatistics getClientSideRequestStatistics() {
        return this.clientSideRequestStatistics;
    }

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.clientSideRequestStatistics);
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing diagnostics ", e);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public Duration getDuration() {
        return clientSideRequestStatistics.getDuration();
    }

    @Override
    public Set<URI> getRegionsContacted() {
        return this.clientSideRequestStatistics.getLocationEndpointsContacted();
    }

    @Override
    public Set<String> getContactedRegionNames() {
        return this.clientSideRequestStatistics.getContactedRegionNames();
    }

    @Override
    public AtomicBoolean isDiagnosticsCapturedInPagedFlux() {
        return isDiagnosticsCapturedInPagedFlux;
    }
}
