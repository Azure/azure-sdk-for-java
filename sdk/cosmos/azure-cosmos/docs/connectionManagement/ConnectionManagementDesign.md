
# Design approach
* Public API Surface
  * EagerConnectionConfig
    * Create `EagerConnectionConfig` to configure a list of `eagerConnectionRegions` and list of `containerLinks`.
      * Implementation
      * ````
        public final class EagerConnectionConfig {
         private final List<String> containerLinks;
         private final List<String> eagerConnectionRegions;

         EagerConnectionConfig(List<String> containerLinks, List<String> eagerConnectionRegions) {
          this.containerLinks = containerLinks;
          this.eagerConnectionRegions = eagerConnectionRegions;
         }

         public List<String> getContainerLinks() {
          return containerLinks;
         }

         public List<String> getEagerConnectionRegions() {
          return eagerConnectionRegions;
         }
        }
        ````
      * Options for the application developer to initialize it:
        * Use the builder pattern to instantiate EagerConnectionConfig, an `EagerConnectionConfigBuilder`, perhaps. 
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
