This folder contains dockerfiles for private container images used in Azure SDKs for Java for testing purposes.

# How to push new JDK with maven image

1. Make desired changes (e.g. update default versions).
2. Log in to container registry instance: `az acr login -n {registry}` (e.g. `azsdkengsys.azurecr.io`)

3. Build and tag the new image

   * `docker build . -t {registry}/java-tools/mvn:{new tag} -f jdk-mariner-mvn-dockerfile --build-arg MAVEN_VERSION={new version}`
     * Supported parameters:
       * `OPEN_JDK_MARINER_TAG`: one of the [JDK Mariner image tags](https://learn.microsoft.com/java/openjdk/containers#linux-based-images)
       * `MAVEN_VERSION`: specifies version to download Maven from `https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz`
       * `MAVEN_URL`: override full URL to download `maven*.tar.gz` from.
   * `{new tag}` should contain JDK and maven versions. Follow the `jdk11-mvn3.9.5` naming pattern.

4. Push the image to the registry: `docker push {registry}/java-tools/mvn:{new tag}`

5. When updating maven version also add `jdk{version}-latest` tag:

   * be careful as it might break someone
   * `docker tag {registry}/java-tools/mvn:{new tag} {registry}/java-tools/mvn:jdk{version}-latest`
   * `docker push {registry}/java-tools/mvn:jdk{version}-latest`
