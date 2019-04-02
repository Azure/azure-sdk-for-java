## __KeyVaultClient__
~~~ csharp
namespace Azure.KeyVault
{
    public class KeyVaultClient : ServiceClient
    {
        public KeyVaultClient(String vaultUri, ServiceClientCredentials credentials);
        public KeyVaultClient(String vaultUri, ServiceClientCredentials credentials, HttpPipeline pipeline);

        public SecretClient getSecretClient(){}

        public void setSecretClient(SecretClient secretClient){}

        //Similarly for Certificate and Key Client
    }
}
~~~

### Usage:
~~~ java
KeyVaultClient client = new KeyVaultClient(vaultUri, new MsalCredentialProvider());

~~~

## __SecretClient__
~~~ java
public class SecretClient : ServiceClient
{
    // constructors
    public SecretClient(String vaultEndPoint, ServiceClientCredentials credentials);
    public SecretClient(String vaultEndPoint, ServiceClientCredentials credentials, HttpPipeline pipeline);
    public SecretClient(String vaultEndPoint, TokenCredentials credentials);
    public SecretClient(String vaultEndPoint, TokenCredentials credentials, HttpPipeline pipeline);

    // methods
    public Mono<RestResponse<Secret>> getAsync(String secretName);
    public Mono<RestResponse<Secret>> getAsync(String secretName, String version);

    public Flux<SecretAttributes> getAllVersionsAsync(String name);
    public Flux<SecretAttributes> getAllVersionsAsync(String name, int maxPageResults);
    public Flux<SecretAttributes> getAllAsync();
    public Flux<SecretAttributes> getAllAsync(int maxPageResults);


    public Mono<RestResponse<SecretAttributes>> updateAsync(SecretAttributes secret);

    public Mono<RestResponse<Secret>> setAsync(String name, String value);
    public Mono<RestResponse<Secret>> setAsync(Secret secret);


    public Mono<RestResponse<DeletedSecret>> deleteAsync(string name);
    public Mono<RestResponse<DeletedSecret>> getDeletedAsync(string name);
    public Flux<DeletedSecret> getAllDeletedAsync();
    public Flux<DeletedSecret> getAllDeletedAsync(int maxPageResults);
    public Mono<RestResponse<Secret>> recoverDeletedAsync(string name);
    public Mono<RestResponse> purgeDeletedAsync(string name);


    public Mono<RestResponse<byte[]>> backupAsync(string name);
    public Mono<RestResponse<Secret>> restoreAsync(byte[] backup);
}
~~~