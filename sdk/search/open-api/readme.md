# Code generation via AutoRest
 
The Azure Search SDK uses [AutoRest](https://github.com/Azure/autorest) to generate low-level REST implementation code. To generate code, you will need to perform some one-time setup tasks, then invoke AutoRest with a given configuration. The repository contains a helper script to perform regeneration tasks. 

## One-Time Setup

1. Install [Node.js](https://nodejs.org/en/download/) `v10.x`
2. Install the [.NET Core SDK](https://dotnet.microsoft.com/download) `2.2` 
3. Install [AutoRest](https://github.com/Azure/autorest/blob/master/README.md#installing-autorest) via npm
4. Install [gulp.js](https://gulpjs.com/) via npm
5. Clone and build [AutoRest.java](https://github.com/Azure/autorest.java/tree/v3) using the `v3` branch, which supports [Project Reactor](https://projectreactor.io/)

### Sample commands

```shell
npm install -g autorest
git clone https://github.com/Azure/autorest.java --recursive 
cd autorest.java
git checkout v3
npm install
```

## Generation

run the following bash script:

```bash
./getSpecsAndGenerate.sh <autorest.java directory path>
```

It will create/update two directories: `azure-search-service` and `azure-search-data` with the newly generated REST implementations
