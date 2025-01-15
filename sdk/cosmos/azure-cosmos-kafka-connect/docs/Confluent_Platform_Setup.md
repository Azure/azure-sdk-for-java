# Confluent Platform Setup

This guide walks through setting up Confluent Platform using Docker containers.

## Prerequisites

- Bash shell
  - Will not work in Cloud Shell or WSL1
- Java 11+ ([download](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html))
- Maven ([download](https://maven.apache.org/download.cgi))
- Docker ([download](https://www.docker.com/products/docker-desktop))
- Powershell (optional) ([download](https://learn.microsoft.com/powershell/scripting/install/installing-powershell))

### Startup

> Running either script for the first time may take several minutes to run in order to download docker images for the Confluent platform components.

```bash

cd $REPO_ROOT/src/docker

# Option 1: Use the bash script to setup
./startup.sh

# Option 2: Use the powershell script to setup
pwsh startup.ps1

# verify the services are up and running
docker-compose ps

```

> You may need to increase the memory allocation for Docker to 3 GB or more.
>
> Rerun the startup script to reinitialize the docker containers.

Your Confluent Platform setup is now ready to use!

### Running Kafka Connect standalone mode

The Kafka Connect container that is included with the Confluent Platform setup runs as Kafka connect as `distributed mode`. Using Kafka Connect as `distributed mode` is *recommended* since you can interact with connectors using the Control Center UI.

If you instead would like to run Kafka Connect as `standalone mode`, which is useful for quick testing, continue through this section. For more information on Kafka Connect standalone and distributed modes, refer to these [Confluent docs](https://docs.confluent.io/home/connect/userguide.html#standalone-vs-distributed-mode).

### Access Confluent Platform components

| Name | Address                 | Description |
| --- |-------------------------| --- |
| Control Center | <http://localhost:9021> | The main webpage for all Confluent services where you can create topics, configure connectors, interact with the Connect cluster (only for distributed mode) and more. |
| Kafka Topics UI | <http://localhost:9000> | Useful to viewing Kafka topics and the messages within them. |
| Schema Registry UI | <http://localhost:9001> | Can view and create new schemas, ideal for interacting with Avro data.  |
| ZooNavigator | <http://localhost:9004> | Web interface for Zookeeper. Refer to the [docs](https://zoonavigator.elkozmon.com/en/stable/) for more information. |

### Cleanup

Tear down the Confluent Platform setup and cleanup any unneeded resources

```bash

cd $REPO_ROOT/src/docker

# bring down all docker containers
docker-compose down

# remove dangling volumes and networks
docker system prune -f --volumes --filter "label=io.confluent.docker"

```
