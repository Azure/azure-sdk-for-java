This tool generates a BOM from a given set of input dependencies.

This tool has 2 modes
1. Generate mode - When run in this mode the tool generates the BOM. This is also the default mode.
Arguments to run the tool in this mode.
        -inputFile=<location_of_versionClient.txt> -outputFile=<location_of_new_bomFile> -pomFile=<location_of_current_bomFile>
2. Analyze mode - When run in this mode the tool validates the BOM. 
Arguments to run the tool in this mode.
        -mode=analyze -pomFile=<location_of_bomFile>
