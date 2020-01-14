FROM mcr.microsoft.com/dotnet/core/sdk AS build

# Copy csproj and restore
WORKDIR /app
COPY *.csproj ./
RUN dotnet restore

# Copy code and publish
COPY . ./
RUN dotnet publish --no-restore -c Release -o out


# Copy build artifacts into runtime image
FROM mcr.microsoft.com/dotnet/core/aspnet AS runtime
WORKDIR /app
COPY --from=build /app/out ./
ENTRYPOINT ["./HttpMock"]
