@echo off

set /p mode=Mode? (manual/auto)
java -jar JAFileMinifier/JAFileMinifier.jar %mode%

echo.
echo Press any key to exit

pause > nul
exit