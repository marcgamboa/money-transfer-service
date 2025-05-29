@echo off
echo Using Java from: %JAVA_HOME%
java -version

echo Running tests...
call gradlew clean test

if exist build\reports\tests\test\index.html (
    echo Opening test report...
    start build\reports\tests\test\index.html
) else (
    echo Test report not found.
)

pause