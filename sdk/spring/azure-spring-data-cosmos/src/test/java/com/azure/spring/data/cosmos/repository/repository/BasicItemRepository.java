package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.BasicItem;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasicItemRepository extends CosmosRepository<BasicItem, String> {
}
