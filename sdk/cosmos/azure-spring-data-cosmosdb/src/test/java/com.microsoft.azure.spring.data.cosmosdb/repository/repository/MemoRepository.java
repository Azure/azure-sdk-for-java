// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.repository;

import com.microsoft.azure.spring.data.cosmosdb.domain.Memo;
import com.microsoft.azure.spring.data.cosmosdb.domain.Importance;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MemoRepository extends CosmosRepository<Memo, String> {
    List<Memo> findMemoByDate(Date date);

    List<Memo> findMemoByImportance(Importance importance);

    List<Memo> findByDateBefore(Date date);

    List<Memo> findByDateBeforeAndMessage(Date date, String message);

    List<Memo> findByDateBeforeOrMessage(Date date, String message);

    List<Memo> findByDateAfter(Date date);

    List<Memo> findByDateAfterAndMessage(Date date, String message);

    List<Memo> findByDateAfterOrMessage(Date date, String message);

    List<Memo> findByDateBetween(Date startDate, Date endDate);

    List<Memo> findByDateBetweenAndMessage(Date startDate, Date endDate, String message);

    List<Memo> findByDateBetweenOrMessage(Date startDate, Date endDate, String message);

    List<Memo> findByMessageStartsWith(String message);
}
