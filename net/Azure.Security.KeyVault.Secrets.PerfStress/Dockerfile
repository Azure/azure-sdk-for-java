FROM mcr.microsoft.com/dotnet/core/sdk AS build

# Copy csproj and restore
WORKDIR /app
COPY Azure.Test.PerfStress/*.csproj Azure.Test.PerfStress/
COPY Azure.Security.KeyVault.Secrets.PerfStress/*.csproj Azure.Security.KeyVault.Secrets.PerfStress/

WORKDIR /app/Azure.Security.KeyVault.Secrets.PerfStress
RUN dotnet restore

# Copy code and publish
WORKDIR /app
COPY Azure.Test.PerfStress Azure.Test.PerfStress/
COPY Azure.Security.KeyVault.Secrets.PerfStress Azure.Security.KeyVault.Secrets.PerfStress/

WORKDIR /app/Azure.Security.KeyVault.Secrets.PerfStress
RUN dotnet publish --no-restore -c Release -o out


# Copy build artifacts into runtime image
FROM mcr.microsoft.com/dotnet/core/runtime AS runtime
WORKDIR /app
COPY --from=build /app/Azure.Security.KeyVault.Secrets.PerfStress/out ./
ENTRYPOINT ["./Azure.Security.KeyVault.Secrets.PerfStress"]
