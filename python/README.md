= Execution =

    To run: (And display help string)
        python PerfStressProgram.py -h


    To run a test: (and display help string)
        python PerfStressProgram.py <testname> -h


= Requirements =

    No packages/requirements are needed for the core scaffold.

    Per-Test requirements:
    - Storage: (e.g. GetBlobsTest)
        azure-storage-blob


= Advanced Use =

    To consume as library, observe syntax in PerfStressProgram.py

    To specify local/custom test location, simply provide the new location to the PerfStressRunner constructor.
