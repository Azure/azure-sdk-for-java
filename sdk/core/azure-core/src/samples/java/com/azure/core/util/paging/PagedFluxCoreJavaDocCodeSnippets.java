// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

/**
 * Code snippets for {@link ContinuablePagedFluxCore}
 */
public class PagedFluxCoreJavaDocCodeSnippets {
    /**
     * Code snippets for extending from {@link ContinuablePagedFluxCore} and enabling custom continuation token.
     */
    public void customContinuationTokenSnippet() {
        // BEGIN: com.azure.core.util.paging.pagedfluxcore.continuationtoken
        class ContinuationState<C> {
            private C lastContinuationToken;
            private boolean isDone;

            ContinuationState(C token) {
                this.lastContinuationToken = token;
            }

            void setLastContinuationToken(C token) {
                this.isDone = token == null;
                this.lastContinuationToken = token;
            }

            C getLastContinuationToken() {
                return this.lastContinuationToken;
            }

            boolean isDone() {
                return this.isDone;
            }
        }

        class FileContinuationToken {
            public int getNextLinkId() {
                return 0;
            }
        }

        class File {
        }

        class FilePage implements ContinuablePage<FileContinuationToken, File> {
            @Override
            public IterableStream<File> getElements() {
                return null;
            }

            @Override
            public FileContinuationToken getContinuationToken() {
                return null;
            }
        }

        class FileShareServiceClient {
            Flux<FilePage> getFilePages(FileContinuationToken token) {
                return null;
            }
        }
        FileShareServiceClient client = null; // Initialize client

        Supplier<PageRetriever<FileContinuationToken, FilePage>> pageRetrieverProvider
            = new Supplier<PageRetriever<FileContinuationToken, FilePage>>() {
                @Override
                public PageRetriever<FileContinuationToken, FilePage> get() {
                    return (continuationToken, pageSize) -> client.getFilePages(continuationToken);
                }
            };

        class FilePagedFlux extends ContinuablePagedFluxCore<FileContinuationToken, File, FilePage> {
            FilePagedFlux(Supplier<PageRetriever<FileContinuationToken, FilePage>>
                                     pageRetrieverProvider) {
                super(pageRetrieverProvider);
            }
        }

        FilePagedFlux filePagedFlux = new FilePagedFlux(pageRetrieverProvider);

        // END: com.azure.core.util.paging.pagedfluxcore.continuationtoken
    }
}
