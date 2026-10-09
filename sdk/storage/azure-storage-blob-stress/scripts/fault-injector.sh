#!/bin/sh
set -ex;
dotnet dev-certs https --export-path /mnt/outputs/dev-cert.crt --format PEM --no-password;
/root/.dotnet/tools/http-fault-injector;