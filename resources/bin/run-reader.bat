@if "%READER_HOME%" == "" set READER_HOME=..
@java -Dreader.home=%READER_HOME% -Djava.library.path=%READER_HOME%\lib -jar %READER_HOME%\lib\pikton-reader-app-1.0-SNAPSHOT.jar 

