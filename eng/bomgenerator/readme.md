This tool generates a BOM from a given set of input dependencies.

This tool has 2 modes
1. Generate mode - When run in this mode the tool generates the BOM. This is also the default mode.
Arguments to run the tool in this mode.
        -inputDir=<location_of_input_directory>
		 This includes the following files.
		 1. Version_client.txt file 
		 2. The BOM file which is the initial POM file.
		 3. InputDependencies file to override the inputs from the version_client.txt. This is an optional file.

        -outputDir=<location_of_outputput_directory>
        This generates the followibg files in the location.
		1. Report log.
		2. Newly created BOM file

2. Analyze mode - When run in this mode the tool validates the BOM. 
Arguments to run the tool in this mode.
        -mode=analyze -inputDir=<location_to_inputDir)>
		This location includes the pom file for the BOM or any library.
