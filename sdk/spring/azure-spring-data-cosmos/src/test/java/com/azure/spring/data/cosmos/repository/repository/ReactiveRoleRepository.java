// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface ReactiveRoleRepository extends ReactiveCosmosRepository<Role, String> {

    Flux<Role> findByDeveloperAndId(boolean isDeveloper, String id);

    @Query(value = "select * from c where c.id = @id")
    Flux<Role> annotatedFindRoleById(@Param("id") String id);

    @Query(value = "select * from c where c.developer = true and c.level = @level")
    Flux<Role> annotatedFindDeveloperByLevel(@Param("level") String level, Sort sort);

    @Query(value = "select * from c where c.level IN (@levels)")
    Flux<Role> annotatedFindRoleByLevelIn(@Param("levels") List<String> levels, Sort sort);

    @Query(value = "select * from c where (NOT IS_DEFINED(@name) OR c.name = @name)")
    Flux<Role> annotatedFindRoleByNameOptional(@Param("name") Optional<String> name);

    @Query(value = "select * \n from c where \n c.name = @name \n")
    Flux<Role> annotatedFindRoleByNameWithSort(@Param("name") String name, Sort sort);

    @Query(value = "select * \n from c \n where c.name = @name \n")
    Flux<Role> annotatedFindRoleByNameWithSort2(@Param("name") String name, Sort sort);

    @Query(value = "select * from c")
    Flux<Role> annotatedFindAllWithSort(Sort sort);
}
