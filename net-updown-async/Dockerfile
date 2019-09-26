FROM mcr.microsoft.com/dotnet/core/sdk:3.0 AS build
WORKDIR /app

# Copy csproj and restore
COPY *.csproj ./app.csproj
RUN dotnet restore

# Copy code and publish
COPY . ./
RUN dotnet publish --no-restore -c Release -o out app.csproj


# Copy build artifacts into runtime image
FROM mcr.microsoft.com/dotnet/core/runtime:3.0 AS runtime
WORKDIR /app
COPY --from=build /app/out ./
ENTRYPOINT ["dotnet", "app.dll"]
