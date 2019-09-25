import argparse
import os
import time

from azure.storage.blob import BlobServiceClient

parser = argparse.ArgumentParser()
parser.add_argument("-c", "--count", help="Number of uploads and downloads", default=5, type=int)
parser.add_argument("-l", "--maximumTransferLength", help="Size of upload and download chunks in bytes", default=4*1024*1024, type=int)
parser.add_argument("-s", "--size", help="Size of blob in bytes", default=10*1024, type=int)
parser.add_argument("-t", "--maximumThreadCount", help="Number of threads or HTTP connections", default=1, type=int)

args = parser.parse_args()

print(f"Uploading and downloading blob of size {args.size} with {args.maximumThreadCount} threads...")

CONTAINER_NAME = 'testcontainer'
BLOB_NAME = 'testblobupdown'

CONN_STR = os.getenv('STORAGE_CONNECTION_STRING')

payload = os.urandom(args.size)

client = BlobServiceClient.from_connection_string(CONN_STR, max_block_size=args.maximumTransferLength).get_blob_client(CONTAINER_NAME, BLOB_NAME)

for i in range(args.count):
    start = time.time()
    client.upload_blob(payload, overwrite=True, max_connections=args.maximumThreadCount)
    end = time.time()
    elapsedSeconds = end - start
    megabytesPerSecond = (args.size / (1024 * 1024)) / elapsedSeconds
    print(f"Uploaded {args.size} bytes in {elapsedSeconds:.2f} seconds ({megabytesPerSecond:.2f} MB/s)")

    start = time.time()
    client.download_blob().content_as_bytes(max_connections=args.maximumThreadCount)
    end = time.time()
    elapsedSeconds = end - start
    megabytesPerSecond = (args.size / (1024 * 1024)) / elapsedSeconds
    print(f"Downloaded {args.size} bytes in {elapsedSeconds:.2f} seconds ({megabytesPerSecond:.2f} MB/s)")
