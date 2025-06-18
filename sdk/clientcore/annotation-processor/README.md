# Client Core Compile-Time Annotation Processor

The client-core annotation processor for introducing compile-time code generation for libraries based on client core
>Note: This project is for experimentation and exploring new ideas that may or may not make it into a supported GA release.

## Usage

1. Add the below plugin configuration to your `pom.xml`:
   ```xml
   <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version> <!-- {x-version-update;org.apache.maven.plugins:maven-compiler-plugin;external_dependency} -->
        <executions>
          <execution>
            <id>run-annotation-processing</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>compile</goal>
            </goals>

            <configuration>
              <source>1.8</source>
              <target>1.8</target>
              <release>8</release>
              <proc>only</proc>
              <generatedSourcesDirectory>${project.build.directory}/generated-sources/</generatedSourcesDirectory>
              <annotationProcessorPaths>
                <annotationProcessorPath>
                  <groupId>io.clientcore</groupId>
                  <artifactId>annotation-processor</artifactId>
                  <version>1.0.0-beta.1</version> <!-- {x-version-update;io.clientcore:annotation-processor;current} -->
                </annotationProcessorPath>
              </annotationProcessorPaths>
              <annotationProcessors>
                <annotationProcessor>io.clientcore.annotation.processor.AnnotationProcessor</annotationProcessor>
              </annotationProcessors>

              <compilerArgs>
                <arg>-Xlint:-options</arg>
              </compilerArgs>
            </configuration>
          </execution>
        </executions>

        <dependencies>
          <dependency>
            <groupId>io.clientcore</groupId>
            <artifactId>annotation-processor</artifactId>
            <version>1.0.0-beta.1</version> <!-- {x-version-update;io.clientcore:annotation-processor;current} -->
          </dependency>
        </dependencies>
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

3. `mvn clean install annotation-processor/pom.xml` followed by `mvn clean compile` your project and the plugin
   will generate an implementation of the annotated interface in the `target/generated-sources` directory.
   ```java
   public class ExampleServiceImpl implements ExampleService {
      private static final ClientLogger LOGGER = new ClientLogger(TestInterfaceClientService.class);

      private final HttpPipeline defaultPipeline;

      private final ObjectSerializer serializer;

      public ExampleServiceImpl(HttpPipeline defaultPipeline, ObjectSerializer serializer) {
          this.defaultPipeline = defaultPipeline;
          this.serializer = serializer == null ? new JsonSerializer() : serializer;
      }

      public static ExampleService getNewInstance(HttpPipeline pipeline, ObjectSerializer serializer) {
          return new ExampleServiceImpl(pipeline, serializer);
      }

      public HttpPipeline getPipeline() {
          return defaultPipeline;
      }

      public Response<BinaryData> getUser(String userId, Context context) {
          return getUser(endpoint, apiVersion, userId, context);
      }

      @Override
      private Response<BinaryData> getUser(String endpoint, String apiVersion, String userId, RequestContext requestContext) {
          HttpPipeline pipeline = this.getPipeline();
          String host = endpoint + "/example/users/" + userId + "?api-version=" + apiVersion;

          // create the request
          HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, host);

          // set the headers
          HttpHeaders headers = new HttpHeaders();
          httpRequest.setHeaders(headers);

          // add RequestContext to the request
          httpRequest.setContext(requestContext);

          // set the body content if present

          // send the request through the pipeline
          Response<BinaryData> networkResponse = pipeline.send(httpRequest);

          final int responseCode = networkResponse.getStatusCode();
          boolean expectedResponse = responseCode == 200;
          if (!expectedResponse) {
              throw new RuntimeException("Unexpected response code: " + responseCode);
          }

          networkResponse.close();
          return networkResponse;
      }
   }
   ```
This implementation eliminates reflection and integrates directly with your HTTP client infrastructure.

