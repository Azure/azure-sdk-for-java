call mvn test -Dgson.version=2.4 -Djackson.version=2.9.4
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.5 -Djackson.version=2.9.5
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.6 -Djackson.version=2.9.6
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.6.1 -Djackson.version=2.9.7
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.6.2 -Djackson.version=2.9.8
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.7 -Djackson.version=2.9.9
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.0 -Djackson.version=2.9.10
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.1 -Djackson.version=2.10.0
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.2 -Djackson.version=2.10.1
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.3 -Djackson.version=2.10.2
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.4 -Djackson.version=2.10.3
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.5 -Djackson.version=2.10.4
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.6 -Djackson.version=2.10.5
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.7 -Djackson.version=2.11.0
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.8 -Djackson.version=2.11.1
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.8.9 -Djackson.version=2.11.2
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.9.0 -Djackson.version=2.11.3
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

call mvn test -Dgson.version=2.9.1 -Djackson.version=2.11.4
IF %ERRORLEVEL% NEQ 0 (
    echo "Tests failed"
    pause
)

pause