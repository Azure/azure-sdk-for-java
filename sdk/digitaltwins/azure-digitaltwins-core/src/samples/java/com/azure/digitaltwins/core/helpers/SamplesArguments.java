package com.azure.digitaltwins.core.helpers;

import com.azure.core.http.policy.HttpLogDetailLevel;
import org.apache.commons.cli.*;

public class SamplesArguments {

    private final String DIGITALTWINS_URL = "DigitalTwinsEndpoint";
    private final String TENANT_ID = "tenantId";
    private final String CLIENT_ID = "clientId";
    private final String CLIENT_SECRET = "clientSecret";
    private final String LOG_DETAIL_LEVEL = "logLevel";
    private final String EVENT_ROUTE_ENDPOINT_NAME = "eventRouteEndpointName";

    private String digitalTwinEndpoint;
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private HttpLogDetailLevel httpLogDetailLevel = HttpLogDetailLevel.NONE;
    private String eventRouteEndpointName;

    public SamplesArguments(String[] args) {

        Option endpoint = new Option("d", DIGITALTWINS_URL, true, "DigitalTwins endpoint URL");
        Option tenantId = new Option("t", TENANT_ID, true, "AAD Tenant Id");
        Option clientId = new Option("c", CLIENT_ID, true, "AAD Client Id");
        Option clientSecret = new Option("s", CLIENT_SECRET, true, "AAD Client Secret");
        Option logLevel = new Option("l", LOG_DETAIL_LEVEL, true, "Http logging detail level \n 0 -> NONE \n 1 -> BASIC \n 2 -> HEADERS \n 3 -> BODY \n 4 -> BODY_AND_HEADERS");
        Option eventRouteEndpointName = new Option("e", EVENT_ROUTE_ENDPOINT_NAME, true, "(Event route sample only) The name of an existing event route endpoint");

        endpoint.setRequired(true);
        tenantId.setRequired(true);
        clientId.setRequired(true);
        clientSecret.setRequired(true);
        logLevel.setRequired(false);
        eventRouteEndpointName.setRequired(false);

        Options options = new Options()
            .addOption(endpoint)
            .addOption(tenantId)
            .addOption(clientId)
            .addOption(clientSecret)
            .addOption(logLevel)
            .addOption(eventRouteEndpointName);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java <sampleClass>.jar", options);

            System.exit(0);
        }

        this.digitalTwinEndpoint = cmd.getOptionValue(DIGITALTWINS_URL);
        this.tenantId = cmd.getOptionValue(TENANT_ID);
        this.clientId = cmd.getOptionValue(CLIENT_ID);
        this.clientSecret = cmd.getOptionValue(CLIENT_SECRET);
        this.eventRouteEndpointName = cmd.getOptionValue(EVENT_ROUTE_ENDPOINT_NAME);

        String inputLogLevel = cmd.getOptionValue(LOG_DETAIL_LEVEL);

        // If log level has been specified we will parse it and return the correct enum value.
        if (inputLogLevel != null && inputLogLevel.trim().length() > 0) {
            try {
                int providedLogDetailLevel = Integer.parseInt(inputLogLevel);

                this.httpLogDetailLevel = convertFromInt(providedLogDetailLevel);
            }
            catch (NumberFormatException e) {
                System.out.println("Provided log detail level must be an integer ranging from 0 - 4");
                formatter.printHelp("java <sampleClass>.jar", options);

                System.exit(0);
            }
        }
    }

    public String getDigitalTwinEndpoint() {
        return this.digitalTwinEndpoint;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public HttpLogDetailLevel getHttpLogDetailLevel() {
        return this.httpLogDetailLevel;
    }

    public String getEventRouteEndpointName() {
        return this.eventRouteEndpointName;
    }

    private static HttpLogDetailLevel convertFromInt(int input) throws NumberFormatException {
        switch (input)
        {
            case 0:
                return HttpLogDetailLevel.NONE;
            case 1:
                return HttpLogDetailLevel.BASIC;
            case 2:
                return HttpLogDetailLevel.HEADERS;
            case 3:
                return HttpLogDetailLevel.BODY;
            case 4:
                return HttpLogDetailLevel.BODY_AND_HEADERS;
        }

        throw new NumberFormatException("Provided log detail level must be an integer ranging from 0 - 4");
    }
}
