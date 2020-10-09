// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
            private final int nextLinkId;

            FileContinuationToken(int nextLinkId) {
                this.nextLinkId = nextLinkId;
            }

            public int getNextLinkId() {
                return nextLinkId;
            }
        }

        class File {
            private final String guid;

            File(String guid) {
                this.guid = guid;
            }

            public String getGuid() {
                return guid;
            }
        }

        class FilePage implements ContinuablePage<FileContinuationToken, File> {
            private final IterableStream<File> elements;
            private final FileContinuationToken fileContinuationToken;

            FilePage(List<File> elements, FileContinuationToken fileContinuationToken) {
                this.elements = IterableStream.of(elements);
                this.fileContinuationToken = fileContinuationToken;
            }

            @Override
            public IterableStream<File> getElements() {
                return elements;
            }

            @Override
            public FileContinuationToken getContinuationToken() {
                return fileContinuationToken;
            }
        }

        class FileShareServiceClient {
            Flux<FilePage> getFilePages(FileContinuationToken token) {
                List<File> files = Collections.singletonList(new File(UUID.randomUUID().toString()));
                if (token.getNextLinkId() < 10) {
                    return Flux.just(new FilePage(files, null));
                } else {
                    return Flux.just(new FilePage(files,
                        new FileContinuationToken((int) Math.floor(Math.random() * 20))));
                }
            }
        }

        FileShareServiceClient client = new FileShareServiceClient();

        Supplier<PageRetriever<FileContinuationToken, FilePage>> pageRetrieverProvider = () ->
            (continuationToken, pageSize) -> client.getFilePages(continuationToken);

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
