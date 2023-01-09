
# Design approach
* Public API Surface
  * `CosmosContainerIdentity`: An abstraction to represent the container link.
    * ````
      public final class CosmosContainerIdentity {

       private final String databaseName;
       private final String containerName;
       private final String containerLink;


       public CosmosContainerIdentity(String databaseName, String containerName) {
      
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("databaseName is null or empty");
        }

        if (containerName == null || containerName.isEmpty()) {
            throw new IllegalArgumentException("containerName is null or empty");
        }
      
        this.containerName = containerName;
        this.databaseName = databaseName;

        String databaseLink = Utils.joinPath(Paths.DATABASES_ROOT, databaseName);
        this.containerLink = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT) + containerName;
      }

       public CosmosContainerIdentity(String containerLink) {
        if (containerLink == null || containerLink.isEmpty()) {
            throw new IllegalArgumentException("containerLink is null or empty");
        }
      
        this.containerLink = containerLink;
        // TODO: Extract databaseName and containerName
        this.databaseName = "";
        this.containerName = "";
       }

       public String getContainerName() {
        return containerName;
       }

       public String getDatabaseName() {
        return databaseName;
       }

       public String getContainerLink() {
        return containerLink;
       }
      }
      ````
    * Ideas
      * Getters for `databaseName` and `containerName` could be removed.
  * `EagerConnectionConfig`
    * Create `EagerConnectionConfig` to configure a list of `eagerConnectionRegions` and list of `cosmosContainerIdentities`.
      * Implementation
      * ````
        public final class EagerConnectionConfig {
         private final List<CosmosContainerIdentity> cosmosContainerIdentities;
         private final List<String> eagerConnectionRegions;
  
         EagerConnectionConfig(List<CosmosContainerIdentity> cosmosContainerIdentities, List<String> eagerConnectionRegions) {
          this.cosmosContainerIdentities = cosmosContainerIdentities;
          this.eagerConnectionRegions = eagerConnectionRegions;
         }
  
         public List<CosmosContainerIdentity> getCosmosContainerIdentities() {
          return cosmosContainerIdentities;
         }
  
         public List<String> getEagerConnectionRegions() {
          return eagerConnectionRegions;
         }
        }
        ````
    * Options for the application developer to initialize it:
        * Use the builder pattern to instantiate EagerConnectionConfig, an `EagerConnectionConfigBuilder`, perhaps. Keep the `EagerConnectionConfig`
      constructor package-private.
        * Use the `EagerConnectionConfig` constructor.
  * `CosmosClientBuilder`
    * Methods to possibly add/enhance:
      * `public CosmosClientBuilder openConnectionsAndInitCaches(EagerConnectionConfig eagerConnectionConfig)`
      * `public CosmosAsyncClient buildAsync()`
      * `public CosmosClient build()`
  * CosmosAsyncClient
      * `Flux<Void> openConnectionsAndInitCaches()` - this will be invoked in a non-blocking manner provided 
    `EagerConnectionConfig` is not empty.
        * Should be package-private.
        * Invoked on buildAsync
        * Questions
          * Should a boolean be used to invoke openConnectionsAndInitCaches() in a blocking/non-blocking manner?
          * 
  * CosmosClient
      * `void openConnectionsAndInitCaches()` - `openConnectionsAndInitCaches()` defined in CosmosAsyncClient will be invoked in a blocking manner.
        * Should be package-private.
        * Invoked on build
* Open connection enhancement
  * `openConnectionsAndInitCaches(eagerConnectionConfig)` definition can be added to the following interfaces:
    * `AsyncDocumentClient`
    * `RxStoreModel`
    * `IStoreClient`
    * `IAddressResolver`
    * `ReplicatedResourcesClient`
    * `AddressSelector`
    * `IAddressResolver`
  * `GlobalAddressResolver`
    * This would implement `openConnectionsAndInitCaches(eagerConnectionConfig)`
      * This would have logic to stream all read endpoints and pick regions to be eagerly connected with.
      * Open connections to filtered out regions.
      * Questions
        * What should be done if no regions are configured?
        * What should be done in case of incorrect/invalid regions?
        * What to choose from - read/write regions?
* Diagnostics
  * Add eager connection regions to diagnostics.
  
