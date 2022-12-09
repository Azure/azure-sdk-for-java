// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package theo.tests;

//import com.azure.spring.data.cosmos.samples.common.User;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactiveUserRepository extends ReactiveCosmosRepository<User, String> {

    Flux<User> findByFirstName(String firstName);
}

