FROM mcr.microsoft.com/openjdk/jdk:21-mariner

WORKDIR /app

# Copy the jar files built by New-StressTestRun.ps1 to the container.
COPY ./target .

RUN ls /app/azure-messaging-eventhubs-stress* >/dev/null 2>&1 || (echo "ERROR: no file starting with azure-messaging-eventhubs-stress was copied to /app." && exit 1)

# This is never executed (since job yaml overrides it)
ENTRYPOINT ["bash"]
