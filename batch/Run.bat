@echo off

for /d %%a in ("%cd%\*") do (
    echo Directory %%~nxa
	java -jar JAFileMinifier/JAFileMinifier.jar %%~nxa
)

echo.
echo Press any key to exit

pause > nul
exit