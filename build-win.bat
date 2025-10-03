@echo on
rd /s /q jre-win GSim-win
jlink --module-path "%JAVA_HOME%\jmods;C:\Java\zulu11\bin\javafx" --add-modules java.base,javafx.controls,javafx.fxml --output jre-win --strip-debug --compress=2 --no-header-files --no-man-pages && java -jar packr-all-4.0.0.jar --platform windows64 --jdk jre-win --executable GSim --classpath gsim.jar --mainclass App --output GSim-win