package com.azure.digitaltwins.core;

import org.apache.commons.cli.*;

public class SamplesArguments {

    private final String DIGITALTWINS_URL = "DigitalTwinsEndpoint";
    private final String TENANT_ID = "tenantId";
    private final String CLIENT_ID = "clientId";
    private final String CLIENT_SECRET = "clientSecret";

    private String digitalTwinUrl;
    private String tenantId;
    private String clientId;
    private String clientSecret;

    public SamplesArguments(String[] args) {
        Options options = new Options();
        Option input = new Option("d", DIGITALTWINS_URL, true, "DigitalTwins endpoint URI");
        Option tenantId = new Option("t", TENANT_ID, true, "AAD Tenant Id");
        Option clientId = new Option("c", CLIENT_ID, true, "AAD Client Id");
        Option clientSecret = new Option("s", CLIENT_SECRET, true, "AAD ClientSecret URI");

        input.setRequired(true);
        tenantId.setRequired(true);
        clientId.setRequired(true);
        clientSecret.setRequired(true);

        options.addOption(input);
        options.addOption(tenantId);
        options.addOption(clientId);
        options.addOption(clientSecret);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Sample option parser", options);

            System.exit(1);
        }

        this.digitalTwinUrl = cmd.getOptionValue(DIGITALTWINS_URL);
        this.tenantId = cmd.getOptionValue(TENANT_ID);
        this.clientId = cmd.getOptionValue(CLIENT_ID);
        this.clientSecret = cmd.getOptionValue(CLIENT_SECRET);
    }

    public String getDigitalTwinUrl() {
        return this.digitalTwinUrl;
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
}
