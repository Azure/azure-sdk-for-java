package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.CourseWithEtag;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

public interface ReactiveCourseWithEtagRepository extends ReactiveCosmosRepository<CourseWithEtag, String> {
}
