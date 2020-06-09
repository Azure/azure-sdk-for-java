### How to Query Partitioned Azure Cosmos DB Collection

With Azure Cosmos DB, you can configure [partition key](https://docs.microsoft.com/en-us/azure/cosmos-db/partition-data) for your collection.

Below is an example about how to query partitioned collection with this spring data module.

#### Example 

Given a document entity structure:
```
    @Document
    @Data
    @AllArgsConstructor
    public class Address {
        @Id
        String postalCode;
        String street;
        @PartitionKey
        String city;
    }
```

Write the repository interface:
```
    @Repository
    public interface AddressRepository extends DocumentDbRepository<Address, String> {
        // Add query methods here, refer to below
    }
```

Query by field name:
```
    List<Address> findByCity(String city);
```

Delete by field name:
```
    void deleteByStreet(String street);  
```

For `Partitioned collection`, if you want to query records by `findById(id)`, exception will be thrown.
```
   // Incorrect for partitioned collection, exception will be thrown
   Address result = repository.findById(id);  // Caution: Works for non-partitioned collection
```  

Instead, you can query records by ID field name with custom query.
```
   // Correct, postalCode is the ID field in Address domain
   @Repository
   public interface AddressRepository extends DocumentDbRepository<Address, String> {
      List<Address> findByPostalCode(String postalCode);
   }
   
   // Query
   List<Address> result = repository.findByPostalCode(postalCode);
```  

