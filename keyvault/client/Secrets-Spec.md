## __KeyVaultClient__
~~~ java
package com.azure.keyvault
{
    public class KeyVaultClient extends ServiceClient
    {
    // The need for Keyvault Client is not too strong at this point.
    // Will be revisited if its needed.
    // TODO: Add Builder pattern to construct/instantiate KeyVaultClient
        public KeyVaultClient(String vaultUrl, ServiceClientCredentials credentials);
        public KeyVaultClient(String vaultUrl, ServiceClientCredentials credentials, HttpPipeline pipeline);
        
        public SecretClient getSecretClient(){}
	
        //Similarly for Certificate and Key Client
    }
}
~~~

### Usage:
~~~ java
KeyVaultClient client = new KeyVaultClient(vaultUri, new KeyVaultCredentials());

~~~

## __SecretClient__
~~~ java
public class SecretClient extends ServiceClient
{

    // TODO: Add Builder pattern to construct/instantiate KeyVaultClient
    
    // constructors
    private SecretClient(String vaultUrl, HttpPipeline pipeline);
    public static Builder builder() {
        return new Builder();
    }

    // methods
    public Mono<RestResponse<Secret>> getSecretAsync(String secretName);
    public Mono<RestResponse<Secret>> getSecretAsync(String secretName, String version);

    public Flux<SecretInfo> getSecretVersionsAsync(String name);
    public Flux<SecretInfo> getSecretVersionsAsync(String name, int maxPageResults);
    public Flux<SecretInfo> getSecretsAsync();
    public Flux<SecretInfo> getSecretsAsync(int maxPageResults);
    
    public Mono<RestResponse<SecretInfo>> updateSecretAsync(SecretInfo secret);

    public Mono<RestResponse<Secret>> setSecretAsync(String name, String value);
    public Mono<RestResponse<Secret>> setSecretAsync(Secret secret);

    public Mono<RestResponse<DeletedSecret>> deleteSecretAsync(String name);
    public Mono<RestResponse<DeletedSecret>> getDeletedSecretAsync(String name);
    public Flux<DeletedSecret> getDeletedSecretsAsync();
    public Flux<DeletedSecret> getDeletedSecretsAsync(int maxPageResults);
    public Mono<RestResponse<Secret>> recoverDeletedSecretAsync(String name);
    public Mono<RestVoidResponse> purgeDeletedSecretAsync(String name);

    public Mono<RestResponse<SecretBackup>> backupSecretAsync(String name);
    public Mono<RestResponse<Secret>> restoreSecretAsync(SecretBackup backup);
    
    public static final class Builder {

        private Builder() {
        }
     
        public SecretClient build() {
           //Validate and Build the Client
        }

        public Builder vaultEndpoint(String vaultEndpoint) {}

        public Builder credentials(ServiceClientCredentials credentials) {}

        public Builder httpLogDetailLevel(HttpLogDetailLevel logLevel) {}

        public Builder addPolicy(HttpPipelinePolicy policy) {}

        public Builder httpClient(HttpClient client) {}
   }
}

~~~
## Get / Set Operations
### SecretClient Set Secret operations

~~~ java
public Mono<RestResponse<Secret>> setSecretAsync(String name, String value);
public Mono<RestResponse<Secret>> setSecretAsync(Secret secret);
~~~
#### Usage:
~~~ java
SecretClient secretClient = SecretClient.builder()
                            .vaultEndpoint("https://myvault.vault.azure.net/")
                            .credentials(new KeyvaultCredentials())
                            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                            .build();            

// set a simple secret such as password
Secret passwordSecret = secretClient.setSecretAsync("user1pass", "password").block().body();

// set a symmetric key secret with nbf and exp
String encodeString = Base64.getEncoder().encodeToString(("password2").getBytes());

Secret keySecret = new Secret("secretkey", encodeString)
        .withContentType("application/octet-stream")
        .withNotBefore(OffsetDateTime.of(LocalDateTime.parse("2000-01-01 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),ZoneOffset.UTC))
        .withExpires(OffsetDateTime.of(LocalDateTime.parse("2050-01-01 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),ZoneOffset.UTC));

Secret retKeySecret = secretClient.setSecretAsync(keySecret).block().body();

~~~


### Replaces:
~~~ java
ServiceFuture<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value, final  ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value);
Observable<ServiceResponse<SecretBundle>> setSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String value);
ServiceFuture<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value, Map<String, String> tags, String contentType, SecretAttributes secretBase, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value, Map<String, String> tags, String contentType, SecretAttributes secretBase);
Observable<ServiceResponse<SecretBundle>> setSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String value, Map<String, String> tags, String contentType, SecretAttributes secretBase);
~~~
#### Usage:
~~~ java 
// TODO: Add Track one Set Secret usage examples.
~~~


### SecretClient Get Secret Operations
~~~ java
public Mono<RestResponse<Secret>> getSecretAsync(String secretName);
public Mono<RestResponse<Secret>> getSecretAsync(String secretName, String version);
~~~

#### Usage:
~~~ java
// get the latest version of a secret
Secret secret = secretClient.getSecretAsync("user1pass").block().body();

// get a specific version of a secret
Secret secretWithVersion = secretClient.getSecretAsync("user1pass","6A385B124DEF4096AF1361A85B16C204").block().body();
~~~

### Replaces:
~~~ java
ServiceFuture<SecretBundle> getSecretAsync(String secretIdentifier, final ServiceCallback<SecretBundle> serviceCallback);
ServiceFuture<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<SecretBundle> serviceCallback);
ServiceFuture<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, String secretVersion, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> getSecretAsync(String vaultBaseUrl, String secretName, String secretVersion);
Observable<ServiceResponse<SecretBundle>> getSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String secretVersion);
~~~
#### Usage:
~~~ java 
// TODO: Add Track one Get Secret usage examples.
~~~

### Update Secret Operation
~~~ java
public Mono<RestResponse<SecretAttributes>> updateSecretAsync(SecretAttributes secret);
~~~
#### Usage:
~~~ java
// Update the expiration of a secret
Secret secret = secretClient.getSecretAsync("secretkey").block().body();

secret.withNotBefore(OffsetDateTime.of(LocalDateTime.parse("2020-01-01 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),ZoneOffset.UTC));

SecretInfo updatedInfo = secretClient.updateSecretAsync(secret).block().body();
~~~
### Replaces:
~~~ java
Observable<SecretBundle> updateSecretAsync(String vaultBaseUrl, String secretName, String secretVersion);
Observable<ServiceResponse<SecretBundle>> updateSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String secretVersion);
SecretBundle updateSecret(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretBase, Map<String, String> tags);
ServiceFuture<SecretBundle> updateSecretAsync(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretBase, Map<String, String> tags, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> updateSecretAsync(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretBase, Map<String, String> tags);
Observable<ServiceResponse<SecretBundle>> updateSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretBase, Map<String, String> tags);
~~~
#### Usage:
~~~ java
// TODO: Add Track one Update Secret usage examples.
~~~

## List Operations

### listSecretsAsync, listSecretVersionsAsync
~~~ java
public Flux<SecretAttributes> getSecretVersionsAsync(String name);
public Flux<SecretAttributes> getSecretVersionsAsync(String name, int maxPageResults);
public Flux<SecretAttributes> getSecretsAsync();
public Flux<SecretAttributes> getSecretsAsync(int maxPageResults);
~~~
#### Usage:
~~~ java
// enumerate all secrets in the vault using Flux subscribe - TO BE TESTED.
secretClient.listSecretsAsync()
	.subscribe(secretInfo -> System.out.println(secretInfo.id()));

int maxPageResults = 5;
secretClient.listSecretsAsync(5)
	.subscribe(secretInfo -> System.out.println(secretInfo.id()));
~~~

### Replaces:
~~~ java
ServiceFuture<List<SecretItem>> getSecretsAsync(final String vaultBaseUrl, final ListOperationCallback<SecretItem> serviceCallback);
Observable<Page<SecretItem>> getSecretsAsync(final String vaultBaseUrl);
Observable<ServiceResponse<Page<SecretItem>>> getSecretsWithServiceResponseAsync(final String vaultBaseUrl);
ServiceFuture<List<SecretItem>> getSecretsAsync(final String vaultBaseUrl, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback);
Observable<Page<SecretItem>> getSecretsAsync(final String vaultBaseUrl, final Integer maxresults);
Observable<ServiceResponse<Page<SecretItem>>> getSecretsWithServiceResponseAsync(final String vaultBaseUrl, final Integer maxresults);
ServiceFuture<List<SecretItem>> getSecretsNextAsync(final String nextPageLink, final ServiceFuture<List<SecretItem>> serviceFuture, final ListOperationCallback<SecretItem> serviceCallback);
Observable<Page<SecretItem>> getSecretsNextAsync(final String nextPageLink);
Observable<ServiceResponse<Page<SecretItem>>> getSecretsNextWithServiceResponseAsync(final String nextPageLink);
ServiceFuture<List<SecretItem>> getSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final ListOperationCallback<SecretItem> serviceCallback);
Observable<Page<SecretItem>> getSecretVersionsAsync(final String vaultBaseUrl, final String secretName);
Observable<ServiceResponse<Page<SecretItem>>> getSecretVersionsWithServiceResponseAsync(final String vaultBaseUrl, final String secretName);
ServiceFuture<List<SecretItem>> getSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults, final ListOperationCallback<SecretItem> serviceCallback);
Observable<Page<SecretItem>> getSecretVersionsAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults);
Observable<ServiceResponse<Page<SecretItem>>> getSecretVersionsWithServiceResponseAsync(final String vaultBaseUrl, final String secretName, final Integer maxresults);
ServiceFuture<List<SecretItem>> getSecretVersionsNextAsync(final String nextPageLink, final ServiceFuture<List<SecretItem>> serviceFuture, final ListOperationCallback<SecretItem> serviceCallback);
Observable<Page<SecretItem>> getSecretVersionsNextAsync(final String nextPageLink);
Observable<ServiceResponse<Page<SecretItem>>> getSecretVersionsNextWithServiceResponseAsync(final String nextPageLink);
~~~
#### Usage:
~~~ java
// TODO: Add Track one List Secret usage examples.
~~~

## Deleted Secret Operations

### deleteSecretAsync, getDeletedSecretAsync, getDeletedSecretsAsync, recoverDeletedSecretAsync, purgeDeletedSecretAsync
~~~ java
public Mono<RestResponse<DeletedSecret>> deleteSecretAsync(string name);
public Mono<RestResponse<DeletedSecret>> getDeletedSecretAsync(string name);
public Flux<DeletedSecret> getDeletedSecretsAsync();
public Flux<DeletedSecret> getDeletedSecretsAsync(int maxPageResults);
public Mono<RestResponse<Secret>> recoverDeletedSecretAsync(string name);
public Mono<RestResponse> purgeDeletedSecretAsync(string name);
~~~
#### Usage:
~~~ java

// Delete a secret
DeletedSecret deletedSecret =  secretClient.deleteSecretAsync("user1pass").block().body();

// Wait for few seconds.
Thread.sleep(5000);

// Get the details of a deleted secret
 deletedSecret = secretClient.getDeletedSecretAsync("user1pass").block().body();

// List all the deleted secrets
secretClient.listDeletedSecretsAsync()
	.subscribe(delSecret -> System.out.println(delSecret.id()));

// Recover a deleted secret
Secret secret = secretClient.recoverDeletedSecretAsync("user1pass").block().body();

// Wait for few seconds.
Thread.sleep(5000);

// Delete the secret again after recovering it.
deletedSecret =  secretClient.deleteSecretAsync("user1pass").block().body();

// Wait for few seconds.
Thread.sleep(5000);

// Purge the deleted secret -- permanenetly delete it.
secretClient.purgeDeletedSecretAsync("user1pass").block();
~~~
### Replaces:
~~~ java
ServiceFuture<DeletedSecretBundle> deleteSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<DeletedSecretBundle> serviceCallback);
Observable<DeletedSecretBundle> deleteSecretAsync(String vaultBaseUrl, String secretName);
Observable<ServiceResponse<DeletedSecretBundle>> deleteSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName);
ServiceFuture<List<DeletedSecretItem>> getDeletedSecretsAsync(final String vaultBaseUrl, final ListOperationCallback<DeletedSecretItem> serviceCallback);
Observable<Page<DeletedSecretItem>> getDeletedSecretsAsync(final String vaultBaseUrl);
Observable<ServiceResponse<Page<DeletedSecretItem>>> getDeletedSecretsWithServiceResponseAsync(final String vaultBaseUrl);
PagedList<DeletedSecretItem> getDeletedSecrets(final String vaultBaseUrl, final Integer maxresults);
Observable<Page<DeletedSecretItem>> getDeletedSecretsAsync(final String vaultBaseUrl, final Integer maxresults);
Observable<ServiceResponse<Page<DeletedSecretItem>>> getDeletedSecretsWithServiceResponseAsync(final String vaultBaseUrl, final Integer maxresults);
ServiceFuture<DeletedSecretBundle> getDeletedSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<DeletedSecretBundle> serviceCallback);
Observable<DeletedSecretBundle> getDeletedSecretAsync(String vaultBaseUrl, String secretName);
Observable<ServiceResponse<DeletedSecretBundle>> getDeletedSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName);
ServiceFuture<List<DeletedSecretItem>> getDeletedSecretsNextAsync(final String nextPageLink, final ServiceFuture<List<DeletedSecretItem>> serviceFuture, final ListOperationCallback<DeletedSecretItem> serviceCallback);
Observable<Page<DeletedSecretItem>> getDeletedSecretsNextAsync(final String nextPageLink);
Observable<ServiceResponse<Page<DeletedSecretItem>>> getDeletedSecretsNextWithServiceResponseAsync(final String nextPageLink);
ServiceFuture<SecretBundle> recoverDeletedSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> recoverDeletedSecretAsync(String vaultBaseUrl, String secretName);
Observable<ServiceResponse<SecretBundle>> recoverDeletedSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName);
ServiceFuture<Void> purgeDeletedSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<Void> serviceCallback);
Observable<Void> purgeDeletedSecretAsync(String vaultBaseUrl, String secretName);
Observable<ServiceResponse<Void>> purgeDeletedSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName);
~~~
#### Usage:
~~~ java
// TODO: Add Track one Delete, Recover and Purge Secret usage examples.
~~~

### backupSecretAsync, restoreSecretAsync
~~~ java
public Mono<RestResponse<byte[]>> backupSecretAsync(string name);
public Mono<RestResponse<Secret>> restoreSecretAsync(byte[] backup);
~~~
#### Usage:
~~~ java
// backup the secret
SecretBackup backup = secretClient.backupSecretAsync("secretkey").block().body();

DeletedSecret deletedSecret =  secretClient.deleteSecretAsync("secretkey").block().body();

Thread.sleep(30000);

secretClient.purgeDeletedSecretAsync("secretkey").block();

//restore the secret from backup
Secret restored = secretClient.restoreSecretAsync(backup).block().body();
~~~
### Replaces:
~~~ java
ServiceFuture<BackupSecretResult> backupSecretAsync(String vaultBaseUrl, String secretName, final ServiceCallback<BackupSecretResult> serviceCallback);
Observable<BackupSecretResult> backupSecretAsync(String vaultBaseUrl, String secretName);
Observable<ServiceResponse<BackupSecretResult>> backupSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName);
ServiceFuture<SecretBundle> restoreSecretAsync(String vaultBaseUrl, byte[] secretBundleBackup, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> restoreSecretAsync(String vaultBaseUrl, byte[] secretBundleBackup);
Observable<ServiceResponse<SecretBundle>> restoreSecretWithServiceResponseAsync(String vaultBaseUrl, byte[] secretBundleBackup);
~~~
#### Usage:
~~~ java
// TODO: Add Track one Backup and Restore Secret usage examples.
~~~

