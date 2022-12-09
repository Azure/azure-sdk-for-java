// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package theo.tests;

//import com.azure.spring.data.cosmos.samples.common.User;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {

    Iterable<User> findByFirstName(String firstName);

    User findByIdAndLastName(String id, String lastName);

    // Query for all documents
    @Query(value = "SELECT * FROM c")
    List<User> getAllUsers();

    // Get all records where last name is in a given array (equivalent to using IN)
    @Query(value = "select * from c where ARRAY_CONTAINS(@lastNames, c.lastName)")
    List<User> getUsersByLastNames(@Param("lastNames") List<String> lastNames);

    // Query for equality using ==
    @Query(value = "SELECT * FROM c WHERE c.id = @documentId")
    List<User> getUsersWithEquality(@Param("documentId") String documentId);

    // Query for inequality using !=
    @Query(value = "SELECT * FROM c WHERE c.id != @documentId")
    List<User> getUsersWithInequalityMethod1(@Param("documentId") String documentId);

    // Query for inequality using NOT
    @Query(value = "SELECT * FROM c WHERE c.id <> @documentId")
    List<User> getUsersWithInequalityMethod2(@Param("documentId") String documentId);

    // Query combining equality and inequality
    @Query(value = "SELECT * FROM c WHERE c.lastName = @documentLastName AND c.id != @documentId")
    List<User> getUsersWithEqualityAndInequality(@Param("documentLastName") String documentLastName, @Param("documentId") String documentId);

    // Query using range operators like >, <, >=, <=
    @Query(value = "SELECT * FROM Families f WHERE f.Children[0].Grade > 5")
    List<User> getUsersWithRange();

    // Query using range operators against strings
    @Query(value = "SELECT * FROM Families f WHERE f.Address.State > 'NY'")
    List<User> getUsersWithRangeAgainstStrings();

    // Query using offset and limit
    @Query(value = "select * from c offset @offset limit @limit")
    List<User> getUsersWithOffsetLimit(@Param("offset") int offset, @Param("limit") int limit);

    // Query with ORDER BY
    @Query(value = "SELECT * FROM Families f WHERE f.LastName = 'Andersen' ORDER BY f.Children[0].Grade")
    List<User> getUsersWithOrderBy();

    // Query with DISTINCT
    @Query(value = "SELECT DISTINCT c.lastName from c")
    List<User> getUsersWithDistinct();

    // Query with aggregate functions
    @Query(value = "SELECT VALUE COUNT(f) FROM Families f WHERE f.LastName = 'Andersen'")
    List<User> getUsersWithAggregate();

    // Query with aggregate functions within documents
    @Query(value = "SELECT VALUE COUNT(child) FROM child IN f.Children")
    List<User> getUsersWithAggregateWithinDocuments();

    // Work with subdocuments
    @Query(value = "SELECT VALUE c FROM c IN f.Children")
    List<User> getUsersSubdocuments();

    // Query with single intra-document join
    @Query(value = "SELECT f.id FROM Families f JOIN c IN f.Children")
    List<User> getUsersWithSingleJoin();

    // Query with two joins
    @Query(value = "SELECT f.id as family, c.FirstName AS child, p.GivenName AS pet FROM Families f JOIN c IN f.Children join p IN c.Pets")
    List<User> getUsersWithTwoJoins();

    // Query with two joins and a filter
    @Query(value = "SELECT f.id as family, c.FirstName AS child, p.GivenName AS pet FROM Families f JOIN c IN f.Children join p IN c.Pets WHERE p.GivenName = 'Fluffy'")
    List<User> getUsersByWithTwoJoinsAndFilter();

    // Query with String STARTSWITH operator
    @Query(value = "SELECT * FROM family WHERE STARTSWITH(family.LastName, 'An')")
    List<User> getUsersWithStringStartswith();

    // Query with math FLOOR operator
    @Query(value = "SELECT VALUE FLOOR(family.Children[0].Grade) FROM family")
    List<User> getUsersWithMathFloor();

    // Query with array length operator
    @Query(value = "SELECT VALUE ARRAY_LENGTH(family.Children) FROM family")
    List<User> getUsersWithArrayLength();
}
