FROM ubuntu:18.04
RUN apt-get update && apt-get install -y openjdk-8-jdk net-tools vim
COPY ./azure-cosmos-benchmark*-jar-with-dependencies.jar /usr/app/azure-cosmos-benchmark-jar-with-dependencies.jar
COPY ./run_benchmark.sh /usr/app/
WORKDIR /usr/app
RUN chmod +x run_benchmark.sh
CMD /bin/sh ./run_benchmark.sh