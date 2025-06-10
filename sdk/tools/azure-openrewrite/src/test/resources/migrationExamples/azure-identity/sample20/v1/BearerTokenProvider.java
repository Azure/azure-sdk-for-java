package migrationExamples.azure-identity.sample20.v1;

public class BearerTokenProvider {
    private String tenantId = System.getenv("AZURE_TENANT_ID");

    private String clientId = System.getenv("AZURE_CLIENT_ID");
    private String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
    private String serviceConnectionId = System.getenv("SERVICE_CONNECTION_ID");

    public void bearerTokenProviderSampleSync() {
        // BEGIN: com.azure.identity.util.getBearerTokenSupplier
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        String scope = "https://cognitiveservices.azure.com/.default";
        Supplier<String> supplier = AuthenticationUtil.getBearerTokenSupplier(credential, scope);

        // This example simply uses the Azure SDK HTTP library to demonstrate setting the header.
        // Use the token as is appropriate for your circumstances.
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://www.example.com");
        request.setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + supplier.get());
        HttpClient client = HttpClient.createDefault();
        client.sendSync(request, Context.NONE);
        // END: com.azure.identity.util.getBearerTokenSupplier
    }
}
