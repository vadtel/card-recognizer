if "%1"=="" goto exit

java -jar artifacts/test.jar %1
goto exit

:exit
pause