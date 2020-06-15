FROM ubuntu:18.04
RUN apt-get update && apt-get install -y openjdk-8-jdk net-tools vim
COPY ./run_benchmark.sh /usr/app/
COPY ./azure-cosmos-benchmark-jar-with-dependencies.jar /usr/app/
WORKDIR /usr/app
RUN chmod +x run.sh
CMD /bin/sh ./run.sh