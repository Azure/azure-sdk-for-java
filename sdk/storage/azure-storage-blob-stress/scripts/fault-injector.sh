#!/bin/sh
set -ex;
dotnet dev-certs https --export-path /mnt/outputs/dev-cert.pfx;
/root/.dotnet/tools/http-fault-injector;
