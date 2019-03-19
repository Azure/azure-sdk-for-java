## __KeyVaultClient__
~~~ java
package com.azure.keyvault
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
    public SecretClient(String vaultUrl, ServiceClientCredentials credentials);
    public SecretClient(String vaultUrl, ServiceClientCredentials credentials, HttpPipeline pipeline);
    public SecretClient(String vaultUrl, TokenCredentials credentials);
    public SecretClient(String vaultUrl, TokenCredentials credentials, HttpPipeline pipeline);

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
## get / set Operations
### SecretClient set secret operations

~~~ java
public Mono<RestResponse<Secret>> setAsync(String name, String value);
public Mono<RestResponse<Secret>> setAsync(Secret secret);
~~~
#### Usage:
~~~ java
--- USAGE BELOW YET TO BE IMPLEMENTED AND TESTED ---
SecretClient secretClient = new SecretClient(new Uri("https://myvault.vaults.azure.net/"), new KeyVaultCredentials());

// set a simple secret such as password
Secret passwordSecret = secretClient.setAsync("user1pass", "password").block().body()

// set a symmetric key secret with nbf and exp
String encodeString = Base64.getEncoder().encodeToString((secretValue).getBytes());
Secret keySecret = new Secret.Builder("secretkey", encodeString)
                             .withContentType("application/octet-stream")
                             .withExpires(new DateTime().withYear(2050).withMonthOfYear(1))
					         .withNotBefore(new DateTime().withYear(2000).withMonthOfYear(1));

Secret keySecret = secretClient.setAsync(keySecret).block().body();
~~~


### Replaces:
~~~ java
ServiceFuture<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value, final  ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value);
Observable<ServiceResponse<SecretBundle>> setSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String value);
ServiceFuture<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value, Map<String, String> tags, String contentType, SecretAttributes secretAttributes, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> setSecretAsync(String vaultBaseUrl, String secretName, String value, Map<String, String> tags, String contentType, SecretAttributes secretAttributes);
Observable<ServiceResponse<SecretBundle>> setSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String value, Map<String, String> tags, String contentType, SecretAttributes secretAttributes);
~~~
#### Usage:
~~~ java 
// TODO: Add Track one Set Secret usage examples.
~~~


### SecretClient get Secret operations
~~~ java
public Mono<RestResponse<Secret>> getAsync(String secretName);
public Mono<RestResponse<Secret>> getAsync(String secretName, String version);
~~~

#### Usage:
~~~ java
// USAGE BELOW YET TO BE IMPLEMENTED AND TESTED
// get the latest version of a secret
Secret secret = secretClient.getAsync("user1pass").block().body();

// get a specific version of a secret
Secret secret = secretClient.getAsync("user1pass","6A385B124DEF4096AF1361A85B16C204").block().body();
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

### update
~~~ java
public Mono<RestResponse<SecretAttributes>> updateAsync(SecretAttributes secret);
~~~
#### Usage:
~~~ java
// USAGE BELOW YET TO BE IMPLEMENTED AND TESTED 
// Update the expiration of a secret
Secret secret = secretClient.getAsync("secretkey");

secret.setNotBefore(new DateTime().withYear(2020).withMonthOfYear(1));

SecretAttributes updated = secretClient.updateAsync(secret).block().body();
~~~
### Replaces:
~~~ java
Observable<SecretBundle> updateSecretAsync(String vaultBaseUrl, String secretName, String secretVersion);
Observable<ServiceResponse<SecretBundle>> updateSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String secretVersion);
SecretBundle updateSecret(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);
ServiceFuture<SecretBundle> updateSecretAsync(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretAttributes, Map<String, String> tags, final ServiceCallback<SecretBundle> serviceCallback);
Observable<SecretBundle> updateSecretAsync(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);
Observable<ServiceResponse<SecretBundle>> updateSecretWithServiceResponseAsync(String vaultBaseUrl, String secretName, String secretVersion, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);
~~~
#### Usage:
~~~ java
// TODO: Add Track one Update Secret usage examples.
~~~

## List Operations

### getAllAsync, getAllVersions
~~~ java
public Flux<SecretAttributes> getAllVersionsAsync(String name);
public Flux<SecretAttributes> getAllVersionsAsync(String name, int maxPageResults);
public Flux<SecretAttributes> getAllAsync();
public Flux<SecretAttributes> getAllAsync(int maxPageResults);
~~~
#### Usage:
~~~ java
// USAGE BELOW YET TO BE IMPLEMENTED AND TESTED 
// enumerate all secrets in the vault using Flux subscribe - TO BE TESTED.
secretClient.GetAllAsync()
            .subscribe(secretAttr -> System.out.println(secretAttr.getId())); 


// enumerate all secrets by page - TO BE TESTED
int maxPageResults = 5;
secretClient.GetAllAsync(5)
            .subscribe(secretAttr -> System.out.println(secretAttr.getId())); 
            
//TO DO: Explore flatMap and collectList methods of Flux for enumeration purposes.


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

### deleteAsync, getDeletedAsync, getAllDeletedAsync, recoverDeletedAsync, purgeDeletedAsync
~~~ java
public Mono<RestResponse<DeletedSecret>> deleteAsync(string name);
public Mono<RestResponse<DeletedSecret>> getDeletedAsync(string name);
public Flux<DeletedSecret> getAllDeletedAsync();
public Flux<DeletedSecret> getAllDeletedAsync(int maxPageResults);
public Mono<RestResponse<Secret>> recoverDeletedAsync(string name);
public Mono<RestResponse> purgeDeletedAsync(string name);
~~~
#### Usage:
~~~ java

// USAGE BELOW YET TO BE IMPLEMENTED AND TESTED 
// delete a secret
DeletedSecret deletedSecret =  secretClient.deleteAsync("user1pass").block().body();

// get the details of a deleted secret
deletedSecret = secretClient.GetDeletedAsync("user1pass").block().body();

// list all the deleted secrets
secretClient.getAllDeletedAsync()
            .subscribe(delSecret -> System.out.println(delSecret.getId())); 

// recover a deleted secret
Secret secret = secretClient.recoverDeletedAsync("userpass1").block().body();

// purge a deleted secret
secretClient.purgeDeletedAsync("userpass1");
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

### backupAsync, restoreAsync
~~~ java
public Mono<RestResponse<byte[]>> backupAsync(string name);
public Mono<RestResponse<Secret>> restoreAsync(byte[] backup);
~~~
#### Usage:
~~~ java
// USAGE BELOW YET TO BE IMPLEMENTED AND TESTED 
// backup the secret
 byte[] backupBytes = secretClient.backupAsync("secretkey").block().body();

// TODO: Write Backed up secret to a file -- TO

// restore the secret from backup
Secret restored = await secretClient.restoreAsync(backupBytes).block().body();
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

