# A PowerShell script to run a single sample. It takes one optional argument "-file": the name of
# the sample file, without the ".java" extension (defaults to SampleAnalyzeAllImageFile).
# For example:
#
# PS> .\run_sample.ps1 -file SampleCaptionImageFile
#
param (
    [string]$file = "SampleAnalyzeAllImageFile"
)

# Optional cleanup
Remove-Item -ErrorAction SilentlyContinue -Recurse -Force target
Remove-Item -ErrorAction SilentlyContinue -Force *.class

# Copy dependencies
mvn clean dependency:copy-dependencies

# Compile sample
javac "$file.java" -cp ".;target/dependency/*"

# Run sample 
java -cp ".;target/dependency/*" $file
