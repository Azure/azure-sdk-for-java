import os

from core.PerfStressTest import PerfStressTest
from core.Helpers import NewGuid

from azure.storage.blob import ContainerClient as SyncContainerClient
from azure.storage.blob.aio import ContainerClient as AsyncContainerClient

class GetBlobsTest(PerfStressTest):
    '''This test evaluates the perf of enumerating blobs'''
    def __init__(self):
        connection_string = os.environ.get("STORAGE_CONNECTION_STRING")
        if not connection_string:
            raise Exception("Undefined environment variable STORAGE_CONNECTION_STRING")

        container_name = NewGuid()
        self.container_client = SyncContainerClient.from_connection_string(conn_str=connection_string, container_name=container_name)

        #TODO: I really hate this.
        self.async_container_client = AsyncContainerClient.from_connection_string(conn_str=connection_string, container_name=container_name)


    async def SetupAsync(self):
        await self.async_container_client.__aenter__()


    async def CleanupAsync(self):
        await self.async_container_client.__aexit__()


    async def GlobalSetupAsync(self):
        self.container_client.create_container()
        for _ in range(0, self.Arguments.count): #pylint: disable=no-member
            self.container_client.upload_blob(NewGuid(), '')


    async def GlobalCleanupAsync(self):
        self.container_client.delete_container()


    def Run(self):
        for _ in self.container_client.list_blobs():
            pass


    async def RunAsync(self):
        async for _ in self.async_container_client.list_blobs(): #pylint: disable=not-an-iterable
            pass


    @staticmethod
    def AddArguments(parser):
        parser.add_argument('-c', '--count', nargs='?', help='Number of blobs to populate.  Default is 1.', default=1)
