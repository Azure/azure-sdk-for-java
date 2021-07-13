# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.145.0/containers/codespaces-linux/.devcontainer/base.Dockerfile

FROM mcr.microsoft.com/vscode/devcontainers/universal:0-linux

USER root

# [Option] Install Docker CLI
ARG INSTALL_DOCKER="false"
# [Option] Install the specified JDK version
ARG JAVA_VERSION="11"
COPY library-scripts/docker-debian.sh library-scripts/java-debian.sh /tmp/library-scripts/
RUN if [ "${INSTALL_DOCKER}" = "true" ]; then \
        rm -f /usr/local/share/docker-init.sh \
        && rm -f /var/run/docker.sock \
        && bash /tmp/library-scripts/docker-debian.sh "true" "/var/run/docker-host.sock" "/var/run/docker.sock" "codespace"; \
    fi \
    && bash /tmp/library-scripts/java-debian.sh "none" \
    && jdkIdentifier="$(sdk ls java | grep -o '${JAVA_VERSION}.*.hs-adpt' | awk '{print $NF}' | awk 'NR==1{print $1}')" \
    && bash /tmp/library-scripts/java-debian.sh "${jdkIdentifier}" \
    && rm -rf /var/lib/apt/lists/* /tmp/library-scripts/

USER codespace

# ** [Optional] Uncomment this section to install additional packages. **
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

