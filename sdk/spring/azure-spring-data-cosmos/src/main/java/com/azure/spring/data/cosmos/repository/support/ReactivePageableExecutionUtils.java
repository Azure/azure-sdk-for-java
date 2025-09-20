// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Support for query execution using {@link Pageable}. Using {@link ReactivePageableExecutionUtils} assumes that data
 * queries are cheaper than {@code COUNT} queries and so some cases can take advantage of optimizations.
 */
abstract class ReactivePageableExecutionUtils {

    private ReactivePageableExecutionUtils() {}

    /**
     * Constructs a {@link Page} based on the given {@code content}, {@link Pageable} and {@link Mono} applying
     * optimizations. The construction of {@link Page} omits a count query if the total can be determined based on the
     * result size and {@link Pageable}.
     *
     * @param content must not be {@literal null}.
     * @param pageable must not be {@literal null}.
     * @param totalSupplier must not be {@literal null}.
     * @return the {@link Page}.
     */
    public static <T> Mono<Page<T>> getPage(List<T> content, Pageable pageable, Mono<Long> totalSupplier) {

        Assert.notNull(content, "Content must not be null");
        Assert.notNull(pageable, "Pageable must not be null");
        Assert.notNull(totalSupplier, "TotalSupplier must not be null");

        if (pageable.isUnpaged() || pageable.getOffset() == 0) {

            if (pageable.isUnpaged() || pageable.getPageSize() > content.size()) {
                return Mono.just(new PageImpl<>(content, pageable, content.size()));
            }

            return totalSupplier.map(total -> new PageImpl<>(content, pageable, total));
        }

        if (content.size() != 0 && pageable.getPageSize() > content.size()) {
            return Mono.just(new PageImpl<>(content, pageable, pageable.getOffset() + content.size()));
        }

        return totalSupplier.map(total -> new PageImpl<>(content, pageable, total));
    }
}
