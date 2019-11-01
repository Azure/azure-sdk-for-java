FROM mcr.microsoft.com/dotnet/core/sdk:3.0 AS build

# Copy csproj and restore
WORKDIR /app
COPY Azure.Test.PerfStress/*.csproj Azure.Test.PerfStress/
COPY Azure.Storage.Blobs.PerfStress/*.csproj Azure.Storage.Blobs.PerfStress/

WORKDIR /app/Azure.Storage.Blobs.PerfStress
RUN dotnet restore

# Copy code and publish
WORKDIR /app
COPY Azure.Test.PerfStress Azure.Test.PerfStress/
COPY Azure.Storage.Blobs.PerfStress Azure.Storage.Blobs.PerfStress/

WORKDIR /app/Azure.Storage.Blobs.PerfStress
RUN dotnet publish --no-restore -c Release -o out


# Copy build artifacts into runtime image
FROM mcr.microsoft.com/dotnet/core/runtime:3.0 AS runtime
WORKDIR /app
COPY --from=build /app/Azure.Storage.Blobs.PerfStress/out ./
ENTRYPOINT ["./Azure.Storage.Blobs.PerfStress"]
