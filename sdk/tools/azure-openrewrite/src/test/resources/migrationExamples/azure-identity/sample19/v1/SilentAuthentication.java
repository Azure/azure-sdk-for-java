package migrationExamples.azure-identity.sample19.v1;

public class SilentAuthentication {
    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");

    public void silentAuthenticationSnippets() {
        // BEGIN: com.azure.identity.silentauthentication
        String authenticationRecordPath = "path/to/authentication-record.json";
        AuthenticationRecord authenticationRecord = null;
        try {
            // If we have an existing record, deserialize it.
            if (Files.exists(new File(authenticationRecordPath).toPath())) {
                authenticationRecord = AuthenticationRecord.deserialize(new FileInputStream(authenticationRecordPath));
            }
        } catch (FileNotFoundException e) {
            // Handle error as appropriate.
        }

        DeviceCodeCredentialBuilder builder = new DeviceCodeCredentialBuilder().clientId(clientId).tenantId(tenantId);
        if (authenticationRecord != null) {
            // As we have a record, configure the builder to use it.
            builder.authenticationRecord(authenticationRecord);
        }
        DeviceCodeCredential credential = builder.build();
        TokenRequestContext trc = new TokenRequestContext().addScopes("your-appropriate-scope");
        if (authenticationRecord == null) {
            // We don't have a record, so we get one and store it. The next authentication will use it.
            credential.authenticate(trc).flatMap(record -> {
                try {
                    return record.serializeAsync(new FileOutputStream(authenticationRecordPath));
                } catch (FileNotFoundException e) {
                    return Mono.error(e);
                }
            }).subscribe();
        }

        // Now the credential can be passed to another service client or used directly.
        AccessToken token = credential.getTokenSync(trc);

        // END: com.azure.identity.silentauthentication
    }
}
