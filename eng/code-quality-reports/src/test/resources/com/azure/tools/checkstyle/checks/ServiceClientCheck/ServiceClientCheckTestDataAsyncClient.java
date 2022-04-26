import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@ServiceClient(builder = ServiceClientCheckTestDataAsyncClient.class, isAsync = true)
public class ServiceClientCheckTestDataAsyncClient {

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> returnTypeFlux() {
        byte[] singleRead = new byte[4096];
        Flux<ByteBuffer> conversionFlux = FluxUtil.toFluxByteBuffer(inputStream);
        return conversionFlux;
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public FilePagedFlux returnTypeExtendsFlux() {
        return new FilePagedFlux(pageRetrieverProvider);
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Integer> returnTypePagedFlux() {
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetriever = () -> getFirstPage();
        Function<String, Mono<PagedResponse<Integer>>> nextPageRetriever =
            continuationToken -> getNextPage(continuationToken);

        return new PagedFlux<>(firstPageRetriever, nextPageRetriever);
    }

    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Integer> errorCollectionReturnType() {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TrainMultivariateModelResponse> returnTypeExtendsResponseBaseWithResponse() {
        return null;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TestData<T>> errorResponseTypeWithResponse() {
        return T;
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

    final FileShareServiceClient client = new FileShareServiceClient();

    Supplier<PageRetriever<FileContinuationToken, FilePage>> pageRetrieverProvider = () ->
        (continuationToken, pageSize) -> client.getFilePages(continuationToken);

    class FilePagedFlux extends ContinuablePagedFluxCore<FileContinuationToken, File, FilePage> {
        FilePagedFlux(Supplier<PageRetriever<FileContinuationToken, FilePage>>
                          pageRetrieverProvider) {
            super(pageRetrieverProvider);
        }
    }
}
