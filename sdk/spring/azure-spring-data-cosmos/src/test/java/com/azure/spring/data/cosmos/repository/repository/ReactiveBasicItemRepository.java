package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.BasicItem;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactiveBasicItemRepository extends ReactiveCosmosRepository<BasicItem, String> {
}
