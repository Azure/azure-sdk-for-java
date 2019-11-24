// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is a Flux that can operate on a type that extends {@link ContinuablePage} and also
 * provides the ability to operate on individual items. This type support user providing continuation
 * token and retrieve pages using such token.
 *
 * The constructor of this type takes a provider method, that when called should provides Page Retriever
 * Function which accepts continuation token. The provider is called for each Subscription to the
 * ContinuablePagedFlux instance. The Page Retriever Function can get called multiple times in serial
 * fashion, first time with {@code null} as continuation token and then each time with the non-null
 * continuation token of the {@link ContinuablePage} emitted from the Flux returned by the last Page
 * Retriever invocation. Completion signal will be send to the subscriber when the last {@link ContinuablePage}
 * emitted from the Flux has {@code null} continuation token.
 *
 * Note that similar to PagedFluxCore, if needed the provider implementation can create state and associate
 * it with Page Retriever Function instance, most of the cases such state is not needed as continuation
 * token is sufficient to identify next set of pages.
 *
 * Example:
 * ----------------------------------------------------------------------------------
 * // FilesPage is list of files. This type has concept of continuation token that can be exposed
 * // to the user, hence extends from ContinuablePage and exposes getContinuationToken method.
 *
 * // The continuation token type FilesContinuationToken is used to retrieve one or more FilesPage.
 * //
 * public class FilesPage implements ContinuablePage<FilesContinuationToken, File> {
 *      @Override
 *      public List<File> getItems() {..}
 *
 *      @Override
 *      public FilesContinuationToken getContinuationToken() {..}
 * }
 *
 * // The user facing Continuation Token type to retrieve Flux of FilesPage.
 * //
 * public class FilesContinuationToken implements ContinuationToken {
 *     public int getNextLinkId();
 * }
 *
 * // The provider impl that when called provides Page Retrieval Function that returns Flux of FilesPage.
 * // Provider method is called for each Subscription to ContinuablePagedFlux.
 * //
 * private final Supplier<Function<FilesContinuationToken, Flux<FilesPage>>> pageRetrieverProvider
 *     = new Supplier<Function<FilesContinuationToken, Flux<FilesPage>>>() {
 *         @Override
 *         public Function<FilesContinuationToken, Flux<FilesPage>> get() {
 *             // optional: create any state if needed
 *             return new Function<FilesContinuationToken, Flux<FilesPage>>() {
 *                 @Override
 *                 public Flux<P> apply(FilesContinuationToken token) {
 *                     // optional: use any state if needed
 *                     return shareClient.getFilePages(token);
 *                 }
 *            };
 *        }
 *    };
 * ----------------------------------------------------------------------------------
 *
 * @param <C> The continuation token type
 * @param <T> The type of items in a {@link ContinuablePage}
 * @param <P> The {@link ContinuablePage} holding items of type {@code T}.
 *
 * @see ContinuablePage
 * @see PageCore
 * @see Flux
 */
public abstract class ContinuablePagedFlux<C extends ContinuationToken, T, P extends ContinuablePage<C, T>>
    extends PagedFluxCore<T, P> {

    private final Supplier<Function<C, Flux<P>>> pageRetrieverProvider;

    /**
     * Creates an instance of {@link ContinuablePagedFlux}.
     *
     * @param pageRetrieverProvider a provider that returns Page Retriever Function.
     */
    public ContinuablePagedFlux(Supplier<Function<C, Flux<P>>> pageRetrieverProvider) {
        super(new Supplier<>() {
            final ContinuationState<C> state = new ContinuationState<>(null);
            final Function<C, Flux<P>> pageRetriever = pageRetrieverProvider.get();
            @Override
            public Supplier<Flux<P>> get() {
                return () -> {
                    if (state.isDone()) {
                        // PagedFluxCore contract to send completion signal to subscriber.
                        return null;
                    } else {
                        return pageRetriever.apply(state.getLastContinuationToken())
                            .doOnNext(p -> state.setLastContinuationToken(p.getContinuationToken()));
                    }
                };
            }
        });
        this.pageRetrieverProvider = pageRetrieverProvider;
    }

    /**
     * @return a flux of {@link ContinuablePage} starting from the Page identified
     * by the given token.
     */
    public Flux<P> byPage(C continuationToken) {
        final ContinuationState<C> state = new ContinuationState<>(continuationToken);
        final Function<C, Flux<P>> pageRetriever = this.pageRetrieverProvider.get();
        return Mono.just(true)
            .repeat(() -> !state.isDone())
            .concatMap(b -> pageRetriever.apply(state.getLastContinuationToken())
                .doOnNext(p -> state.setLastContinuationToken(p.getContinuationToken())));
    }
}
