package com.azure.cosmos.implementation.diagnostics;

import com.azure.cosmos.implementation.Utils;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ICosmosDiagnostics {
    static final String COSMOS_DIAGNOSTICS_KEY = "cosmosDiagnostics";
    static final String USER_AGENT = Utils.getUserAgent();
    static final String USER_AGENT_KEY = "userAgent";

    Duration getDuration();

    Set<URI> getRegionsContacted();
    Set<String> getContactedRegionNames();
    AtomicBoolean isDiagnosticsCapturedInPagedFlux();
}
