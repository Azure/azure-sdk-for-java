// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.paging;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Code snippets for {@link PagedFluxCore}
 */
public class PagedFluxCoreJavaDocCodeSnippets {
    /**
     * Code snippets for showing usage of {@link PagedFluxCore} in class docs
     */
    public void classDocSnippet() {
        // BEGIN: com.azure.core.paging.pagedfluxcore.provider

        /**
         * BlobPage that does not have user facing continuation token.
         */
        class BlobPage implements PageCore<Blob> {
            @Override
            public List<Blob> getItems() {
                // The Blob items in the page
                return null;
            }
        }

        /**
         * Service client to make API calls.
         */
        class ContainerServiceClient {
            /**
             * Retrieve next set of pages using the given state.
             *
             * @param state the current state
             * @return the Flux of BlobPage
             */
            Flux<BlobPage> getBlobsPages(State state) {
                return null;
            }
        }
        ContainerServiceClient client = null; // Initialize client

        /**
         * A provider, upon invocation returns Page Retrieval Function.
         * Provider is called for each Subscription to PagedFluxCore, For each call
         * it create a state and associate it with instance of Page Retrieval Function.
         */
        Supplier<Supplier<Flux<BlobPage>>> pageRetrieverProvider = new Supplier<Supplier<Flux<BlobPage>>>() {
            @Override
            public Supplier<Flux<BlobPage>> get() {
                State state = new State();
                return new Supplier<Flux<BlobPage>>() {
                    @Override
                    public Flux<BlobPage> get() {
                        if (state.hasMorePage()) {
                            // Pass current state to service method that make API call.
                            // state contains necessary data that service method needed
                            // to prepare next set of pages. Before returning, the service
                            // method updates the state for the next call.
                            //
                            Flux<BlobPage> pages = client.getBlobsPages(state);
                            return pages;
                        } else {
                            // Null indicates no more Pages, upon receiving this
                            // the PagedFluxCore send completion signal to the subscriber.
                            return null;
                        }
                    }
                };
            }
        };

        /**
         * A Paged Flux specialized to for BlobPage.
         */
        class BlobPagedFlux extends PagedFluxCore<Blob, BlobPage> {
            /**
             * Creates an instance of {@link PagedFluxCore}.
             *
             * @param pageRetrieverProvider a provider that returns Page Retriever Function.
             */
            BlobPagedFlux(Supplier<Supplier<Flux<BlobPage>>> pageRetrieverProvider) {
                super(pageRetrieverProvider);
            }
        }

        BlobPagedFlux blobPagedFlux = new BlobPagedFlux(pageRetrieverProvider);
        // END: com.azure.core.paging.pagedfluxcore.provider
    }

    /**
     * Code snippets for extending from {@link PagedFluxCore} and enabling custom continuation token.
     */
    public void customContinuationTokenSnippet() {
        // BEGIN: com.azure.core.paging.pagedfluxcore.continuationtoken

        abstract class ContinuablePage<C, T> implements PageCore<T> {
            abstract C getContinuationToken();
        }

        class ContinuationState<C> {
            private C lastContinuationToken;
            private boolean isDone;

            ContinuationState(C token) {
                this.lastContinuationToken = token;
            }

            void setLastContinuationToken(C token) {
                this.isDone = token == null ? true : false;
                this.lastContinuationToken = token;
            }

            C getLastContinuationToken() {
                return this.lastContinuationToken;
            }

            boolean isDone() {
                return this.isDone;
            }
        }

        class ContinuablePagedFlux<C, T, P extends ContinuablePage<C, T>>
            extends PagedFluxCore<T, P> {

            private final Supplier<Function<C, Flux<P>>> pageRetrieverProvider;

            ContinuablePagedFlux(Supplier<Function<C, Flux<P>>> pageRetrieverProvider) {
                super(new Supplier<Supplier<Flux<P>>>() {
                    final ContinuationState<C> state = new ContinuationState<>(null);
                    final Function<C, Flux<P>> pageRetriever = pageRetrieverProvider.get();
                    @Override
                    public Supplier<Flux<P>> get() {
                        return () -> {
                            if (state.isDone()) {
                                // PagedFluxCore contract to send completion signal to subscriber.
                                return null;
                            } else {
                                return pageRetriever
                                    .apply(state.getLastContinuationToken())
                                    .doOnNext(p -> state
                                        .setLastContinuationToken(p.getContinuationToken()));
                            }
                        };
                    }
                });
                this.pageRetrieverProvider = pageRetrieverProvider;
            }

            public Flux<P> byPage(C continuationToken) {
                final ContinuationState<C> state = new ContinuationState<>(continuationToken);
                final Function<C, Flux<P>> pageRetriever = this.pageRetrieverProvider.get();
                return Mono.just(true)
                    .repeat(() -> !state.isDone())
                    .concatMap(b -> pageRetriever
                        .apply(state.getLastContinuationToken())
                        .doOnNext(p -> state
                            .setLastContinuationToken(p.getContinuationToken())));
            }
        }

        class FileContinuationToken {
            public int getNextLinkId() {
                return 0;
            }
        }

        class FilePage extends ContinuablePage<FileContinuationToken, File> {
            @Override
            public List<File> getItems() {
                return null;
            }

            @Override
            FileContinuationToken getContinuationToken() {
                return null;
            }
        }

        class FileShareServiceClient {
            Flux<FilePage> getFilePages(FileContinuationToken token) {
                return null;
            }
        }
        FileShareServiceClient client = null; // Initialize client

        Supplier<Function<FileContinuationToken, Flux<FilePage>>> pageRetrieverProvider
            = new Supplier<Function<FileContinuationToken, Flux<FilePage>>>() {
                @Override
                public Function<FileContinuationToken, Flux<FilePage>> get() {
                    return new Function<FileContinuationToken, Flux<FilePage>>() {
                        @Override
                        public Flux<FilePage> apply(FileContinuationToken token) {
                            return client.getFilePages(token);
                        }
                    };
                }
            };

        class FilePagedFlux extends ContinuablePagedFlux<FileContinuationToken, File, FilePage> {
            FilePagedFlux(Supplier<Function<FileContinuationToken, Flux<FilePage>>>
                                     pageRetrieverProvider) {
                super(pageRetrieverProvider);
            }
        }

        FilePagedFlux filePagedFlux = new FilePagedFlux(pageRetrieverProvider);

        // END: com.azure.core.paging.pagedfluxcore.continuationtoken
    }

    /**
     * Implementation not provided.
     *
     * Type of items in a BlobPage
     */
    private static class Blob {
    }

    /**
     * Implementation not provided.
     *
     * Type to store state specific to one subscription
     */
    private static class State {
        Boolean hasMorePage() {
            return null;
        }
        // Other state variables
    }

    /**
     * Implementation not provided.
     *
     * Type of items in a FilePage
     */
    private static class File {
    }
}
