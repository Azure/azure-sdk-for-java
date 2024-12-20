# Client Core Compile-Time Annotation Processor

The client-core annotation processor for introducing compile-time code generation for libraries based on client core
>Note: This project is for experimentation and exploring new ideas that may or may not make it into a supported GA release.

## Usage

1. Add the plugin dependency:
   ```xml
   <dependencies>
    <dependency>
        <groupId>io.clientcore.tools</groupId>
        <artifactId>annotation-processor</artifactId>
        <version>1.0.0.beta.1</version> <!-- {x-version-update;io.clientcore.tools:annotation-processor;external_dependency} -->
        <scope>provided</scope>
    </dependency>
   </dependencies>
   ```
   1.1. Add the plugin configuration to your `pom.xml`:
   ```xml
   <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version> <!-- {x-version-update;org.apache.maven.plugins:maven-compiler-plugin;external_dependency} -->
        <configuration>
          <generatedSourcesDirectory>${project.build.directory}/generated-sources/</generatedSourcesDirectory>
          <annotationProcessors>
            <annotationProcessor>io.generation.tools.codegen.AnnotationProcessor</annotationProcessor>
          </annotationProcessors>
        </configuration>
      </plugin>
    </plugins>
   ```
2. Annotate your interfaces with `@ServiceInterface`,  `@HttpRequestInformation` and 
   `@UnexpectedResponseExceptionDetail` such annotations:
   ```java 
   @ServiceInterface(name = "ExampleClient", host = "{endpoint}/example")
   public interface ExampleService {
       @HttpRequestInformation(method = HttpMethod.GET, path = "/user/{userId}", expectedStatusCodes = { 200 })
       @UnexpectedResponseExceptionDetail(exceptionTypeName = "CLIENT_AUTHENTICATION", statusCode = { 401 })
       @UnexpectedResponseExceptionDetail(exceptionTypeName = "RESOURCE_NOT_FOUND", statusCode = { 404 })
       @UnexpectedResponseExceptionDetail(exceptionTypeName = "RESOURCE_MODIFIED", statusCode = { 409 })
       User getUser(@PathParam("userId") String userId);
   }
   ```

3. Build your project and the plugin will generate an implementation of the annotated interface.
   The processor would generate an implementation:
   ```java
   public class ExampleServiceImpl implements ExampleService {
    private static final ClientLogger LOGGER = new ClientLogger(OpenAIClientServiceImpl.class);

    private final HttpPipeline defaultPipeline;

    private final ObjectSerializer serializer;

    private final String endpoint;

    private final ExampleServiceVersion serviceVersion;

    private String apiVersion;

    public ExampleServiceImpl (HttpPipeline defaultPipeline, ObjectSerializer serializer,
       String endpoint, ExampleServiceVersion serviceVersion) {
       this.defaultPipeline = defaultPipeline;
       this.serializer = serializer;
       this.endpoint = endpoint;
       this.apiVersion = serviceVersion.getVersion();
       this.serviceVersion = serviceVersion;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public HttpPipeline getPipeline() {    
        return defaultPipeline;
    }

    public ExampleServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    private final HttpPipeline pipeline;

    public ExampleServiceImpl(HttpPipeline pipeline) {
        this.pipeline = pipeline;
    }
      
    public Response<BinaryData> getUser(String userId, Context context) {
        return getUser(endpoint, apiVersion, userId, context);
    }

    @Override
    private Response<BinaryData> getUser(String endpoint, String apiVersion, String userId, Context context) {
        HttpPipeline pipeline = this.getPipeline();
        String host = endpoint + "/example/users/" + userId + "?api-version=" + apiVersion;

        // create the request
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, host);

        // set the headers
        HttpHeaders headers = new HttpHeaders();
        httpRequest.setHeaders(headers);

        // add RequestOptions to the request
        httpRequest.setRequestOptions(requestOptions);

        // set the body content if present

        // send the request through the pipeline
        Response<?> response = pipeline.send(httpRequest);

        final int responseCode = response.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        ResponseBodyMode responseBodyMode = ResponseBodyMode.IGNORE;
        if (requestOptions != null) {
            responseBodyMode = requestOptions.getResponseBodyMode();
        }
        if (responseBodyMode == ResponseBodyMode.DESERIALIZE) {
            BinaryData responseBody = response.getBody();
            HttpResponseAccessHelper.setValue((HttpResponse<?>) response, responseBody);
        } else {
            BinaryData responseBody = response.getBody();
            HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> responseBody);
        }
        return (Response<BinaryData>) response;
    }
   }
   ```
This implementation eliminates reflection and integrates directly with your HTTP client infrastructure.

