// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The default implementation of {@link ContinuablePagedFlux}.
 *
 * This type is a Flux that provides the ability to operate on pages of type {@link ContinuablePage} and individual
 * items in such pages. This type supports user-provided continuation tokens, allowing for restarting from a
 * previously-retrieved continuation token.
 *
 * The type is backed by the Page Retriever provider provided in it's constructor. The provider is expected to return
 * {@link PageRetriever} when called. The provider is invoked for each Subscription to this Flux. Given provider is
 * called per Subscription, the provider implementation can create one or more objects to store any state and Page
 * Retriever can capture and use those objects. This indirectly associate the state objects to the Subscription. The
 * Page Retriever can get called multiple times in serial fashion, each time after the completion of the Flux returned
 * by the previous invocation. The final completion signal will be send to the Subscriber when the last Page emitted by
 * the Flux returned by the Page Retriever has {@code null} continuation token.
 *
 * <p><strong>Extending PagedFluxCore for Custom Continuation Token support</strong></p>
 * <!-- src_embed com.azure.core.util.paging.pagedfluxcore.continuationtoken -->
 * <pre>
 * class ContinuationState&lt;C&gt; &#123;
 *     private C lastContinuationToken;
 *     private boolean isDone;
 *
 *     ContinuationState&#40;C token&#41; &#123;
 *         this.lastContinuationToken = token;
 *     &#125;
 *
 *     void setLastContinuationToken&#40;C token&#41; &#123;
 *         this.isDone = token == null;
 *         this.lastContinuationToken = token;
 *     &#125;
 *
 *     C getLastContinuationToken&#40;&#41; &#123;
 *         return this.lastContinuationToken;
 *     &#125;
 *
 *     boolean isDone&#40;&#41; &#123;
 *         return this.isDone;
 *     &#125;
 * &#125;
 *
 * class FileContinuationToken &#123;
 *     private final int nextLinkId;
 *
 *     FileContinuationToken&#40;int nextLinkId&#41; &#123;
 *         this.nextLinkId = nextLinkId;
 *     &#125;
 *
 *     public int getNextLinkId&#40;&#41; &#123;
 *         return nextLinkId;
 *     &#125;
 * &#125;
 *
 * class File &#123;
 *     private final String guid;
 *
 *     File&#40;String guid&#41; &#123;
 *         this.guid = guid;
 *     &#125;
 *
 *     public String getGuid&#40;&#41; &#123;
 *         return guid;
 *     &#125;
 * &#125;
 *
 * class FilePage implements ContinuablePage&lt;FileContinuationToken, File&gt; &#123;
 *     private final IterableStream&lt;File&gt; elements;
 *     private final FileContinuationToken fileContinuationToken;
 *
 *     FilePage&#40;List&lt;File&gt; elements, FileContinuationToken fileContinuationToken&#41; &#123;
 *         this.elements = IterableStream.of&#40;elements&#41;;
 *         this.fileContinuationToken = fileContinuationToken;
 *     &#125;
 *
 *     &#64;Override
 *     public IterableStream&lt;File&gt; getElements&#40;&#41; &#123;
 *         return elements;
 *     &#125;
 *
 *     &#64;Override
 *     public FileContinuationToken getContinuationToken&#40;&#41; &#123;
 *         return fileContinuationToken;
 *     &#125;
 * &#125;
 *
 * class FileShareServiceClient &#123;
 *     Flux&lt;FilePage&gt; getFilePages&#40;FileContinuationToken token&#41; &#123;
 *         List&lt;File&gt; files = Collections.singletonList&#40;new File&#40;UUID.randomUUID&#40;&#41;.toString&#40;&#41;&#41;&#41;;
 *         if &#40;token.getNextLinkId&#40;&#41; &lt; 10&#41; &#123;
 *             return Flux.just&#40;new FilePage&#40;files, null&#41;&#41;;
 *         &#125; else &#123;
 *             return Flux.just&#40;new FilePage&#40;files,
 *                 new FileContinuationToken&#40;&#40;int&#41; Math.floor&#40;Math.random&#40;&#41; * 20&#41;&#41;&#41;&#41;;
 *         &#125;
 *     &#125;
 * &#125;
 *
 * FileShareServiceClient client = new FileShareServiceClient&#40;&#41;;
 *
 * Supplier&lt;PageRetriever&lt;FileContinuationToken, FilePage&gt;&gt; pageRetrieverProvider = &#40;&#41; -&gt;
 *     &#40;continuationToken, pageSize&#41; -&gt; client.getFilePages&#40;continuationToken&#41;;
 *
 * class FilePagedFlux extends ContinuablePagedFluxCore&lt;FileContinuationToken, File, FilePage&gt; &#123;
 *     FilePagedFlux&#40;Supplier&lt;PageRetriever&lt;FileContinuationToken, FilePage&gt;&gt;
 *         pageRetrieverProvider&#41; &#123;
 *         super&#40;pageRetrieverProvider&#41;;
 *     &#125;
 * &#125;
 *
 * FilePagedFlux filePagedFlux = new FilePagedFlux&#40;pageRetrieverProvider&#41;;
 *
 * </pre>
 * <!-- end com.azure.core.util.paging.pagedfluxcore.continuationtoken -->
 *
 * @param <C> the type of the continuation token
 * @param <T> The type of elements in a {@link ContinuablePage}
 * @param <P> The {@link ContinuablePage} holding items of type {@code T}.
 * @see ContinuablePagedFlux
 * @see ContinuablePage
 */
public abstract class ContinuablePagedFluxCore<C, T, P extends ContinuablePage<C, T>>
    extends ContinuablePagedFlux<C, T, P> {
    private final ClientLogger logger = new ClientLogger(ContinuablePagedFluxCore.class);

    final Supplier<PageRetriever<C, P>> pageRetrieverProvider;
    final Integer defaultPageSize;

    /**
     * Creates an instance of {@link ContinuablePagedFluxCore}.
     *
     * @param pageRetrieverProvider a provider that returns {@link PageRetriever}.
     * @throws NullPointerException If {@code pageRetrieverProvider} is null.
     */
    protected ContinuablePagedFluxCore(Supplier<PageRetriever<C, P>> pageRetrieverProvider) {
        this(pageRetrieverProvider, null, null);
    }

    /**
     * Creates an instance of {@link ContinuablePagedFluxCore}.
     *
     * @param pageRetrieverProvider a provider that returns {@link PageRetriever}.
     * @param pageSize the preferred page size
     * @throws NullPointerException If {@code pageRetrieverProvider} is null.
     * @throws IllegalArgumentException If {@code pageSize} is less than or equal to zero.
     */
    protected ContinuablePagedFluxCore(Supplier<PageRetriever<C, P>> pageRetrieverProvider, int pageSize) {
        this(pageRetrieverProvider, pageSize, null);
    }

    /**
     * Creates an instance of {@link ContinuablePagedFluxCore}.
     *
     * @param pageRetrieverProvider A provider that returns {@link PageRetriever}.
     * @param pageSize The preferred page size.
     * @param continuationPredicate A predicate which determines if paging should continue.
     * @throws NullPointerException If {@code pageRetrieverProvider} is null.
     * @throws IllegalArgumentException If {@code pageSize} is not null and is less than or equal to zero.
     */
    protected ContinuablePagedFluxCore(Supplier<PageRetriever<C, P>> pageRetrieverProvider, Integer pageSize,
        Predicate<C> continuationPredicate) {
        super(continuationPredicate);
        this.pageRetrieverProvider = Objects.requireNonNull(pageRetrieverProvider,
            "'pageRetrieverProvider' function cannot be null.");
        if (pageSize != null && pageSize <= 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'pageSize' must be greater than 0 required but provided: " + pageSize));
        }
        this.defaultPageSize = pageSize;
    }

    /**
     * Get the page size configured this {@link ContinuablePagedFluxCore}.
     *
     * @return the page size configured, {@code null} if unspecified.
     */
    public Integer getPageSize() {
        return this.defaultPageSize;
    }

    @Override
    public Flux<P> byPage() {
        return byPage(this.pageRetrieverProvider, null, this.defaultPageSize);
    }

    @Override
    public Flux<P> byPage(C continuationToken) {
        if (continuationToken == null) {
            return Flux.empty();
        }
        return byPage(this.pageRetrieverProvider, continuationToken, this.defaultPageSize);
    }

    @Override
    public Flux<P> byPage(int preferredPageSize) {
        if (preferredPageSize <= 0) {
            return Flux.error(new IllegalArgumentException("preferredPageSize > 0 required but provided: "
                + preferredPageSize));
        }
        return byPage(this.pageRetrieverProvider, null, preferredPageSize);
    }

    @Override
    public Flux<P> byPage(C continuationToken, int preferredPageSize) {
        if (preferredPageSize <= 0) {
            return Flux.error(new IllegalArgumentException("preferredPageSize > 0 required but provided: "
                + preferredPageSize));
        }
        if (continuationToken == null) {
            return Flux.empty();
        }
        return byPage(this.pageRetrieverProvider, continuationToken, preferredPageSize);
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively. This is recommended for most
     * common scenarios. This will seamlessly fetch next page when required and provide with a {@link Flux} of items.
     *
     * @param coreSubscriber The subscriber for this {@link ContinuablePagedFluxCore}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        byPage(this.pageRetrieverProvider, null, this.defaultPageSize)
            .flatMap(page -> {
                IterableStream<T> iterableStream = page.getElements();
                return iterableStream == null
                    ? Flux.empty()
                    : Flux.fromIterable(page.getElements());
            })
            .subscribe(coreSubscriber);
    }

    /**
     * Get a Flux of {@link ContinuablePage} created by concat-ing Flux instances returned Page Retriever Function
     * calls.
     *
     * @param provider the provider that when called returns Page Retriever Function
     * @param continuationToken the token to identify the pages to be retrieved
     * @param pageSize the preferred page size
     * @return a Flux of {@link ContinuablePage} identified by the given continuation token
     */
    private Flux<P> byPage(Supplier<PageRetriever<C, P>> provider, C continuationToken, Integer pageSize) {
        return Flux.defer(() -> {
            final PageRetriever<C, P> pageRetriever = provider.get();
            final ContinuationState<C> state = new ContinuationState<>(continuationToken);
            return retrievePages(state, pageRetriever, pageSize);
        });
    }

    /**
     * Get a Flux of {@link ContinuablePage} created by concat-ing child Flux instances returned Page Retriever Function
     * calls. The first child Flux of {@link ContinuablePage} is identified by the continuation-token in the state.
     *
     * @param state the state to be used across multiple Page Retriever Function calls
     * @param pageRetriever the Page Retriever Function
     * @param pageSize the preferred page size
     * @return a Flux of {@link ContinuablePage}
     */
    private Flux<P> retrievePages(ContinuationState<C> state, PageRetriever<C, P> pageRetriever, Integer pageSize) {
        /*
         * The second argument for 'expand' is an initial capacity hint to the expand subscriber to indicate what size
         * buffer it should instantiate. 4 is used as PageRetriever's 'get' returns a Flux so an implementation may
         * return multiple pages, but in the case only one page is retrieved the buffer won't need to be resized or
         * request additional pages from the service.
         */
        return retrievePage(state, pageRetriever, pageSize)
            .expand(page -> {
                state.setLastContinuationToken(page.getContinuationToken(), t -> !getContinuationPredicate().test(t));
                return Flux.defer(() -> retrievePage(state, pageRetriever, pageSize));
            }, 4);
    }

    private Flux<P> retrievePage(ContinuationState<C> state, PageRetriever<C, P> pageRetriever, Integer pageSize) {
        if (state.isDone()) {
            return Flux.empty();
        } else {
            return pageRetriever.get(state.getLastContinuationToken(), pageSize)
                .switchIfEmpty(Flux.defer(() -> {
                    state.setLastContinuationToken(null);
                    return Mono.empty();
                }));
        }
    }
}
