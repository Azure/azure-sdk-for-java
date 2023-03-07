// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Importance;
import com.azure.spring.data.cosmos.domain.Memo;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface MemoRepository extends CosmosRepository<Memo, String> {
    Iterable<Memo> findMemoByDate(Date date);

    Iterable<Memo> findMemoByImportance(Importance importance);

    Iterable<Memo> findByDateBefore(Date date);

    Iterable<Memo> findByDateBeforeAndMessage(Date date, String message);

    Iterable<Memo> findByDateBeforeOrMessage(Date date, String message);

    Iterable<Memo> findByDateAfter(Date date);

    Iterable<Memo> findByDateAfterAndMessage(Date date, String message);

    Iterable<Memo> findByDateAfterOrMessage(Date date, String message);

    Iterable<Memo> findByDateBetween(Date startDate, Date endDate);

    Iterable<Memo> findByDateBetweenAndMessage(Date startDate, Date endDate, String message);

    Iterable<Memo> findByDateBetweenOrMessage(Date startDate, Date endDate, String message);

    Iterable<Memo> findByMessageStartsWith(String message);
}
