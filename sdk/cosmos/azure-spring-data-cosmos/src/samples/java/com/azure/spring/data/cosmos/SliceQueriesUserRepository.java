package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;

public interface SliceQueriesUserRepository extends CosmosRepository<User, String> {
    @Query("select * from c where c.lastName = @lastName")
    Slice<User> getUsersByLastName(@Param("lastName") String lastName, Pageable pageable);
}
