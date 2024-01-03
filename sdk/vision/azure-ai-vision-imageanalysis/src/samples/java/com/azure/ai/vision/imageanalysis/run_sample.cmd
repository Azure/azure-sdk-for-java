REM
REM A Windows script to run a single sample. It takes one argument: the name of
REM the sample file, without the ".java" extension. For example:
REM
REM run_sample.cmd SampleAnalyzeAllImageFile
REM

REM Optional cleanup
rmdir /s /q target & del *.class

REM Copy dependencies
call mvn clean dependency:copy-dependencies

REM Compile sample
javac %1.java -cp ".;target\dependency\*"

REM Run sample 
java -cp ".;target\dependency\*" %1