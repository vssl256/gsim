@echo on
rd /s /q jre GSim-lin
jlink --module-path "jdk11\jmods" --add-modules java.base,javafx.controls,javafx.fxml --output jre --strip-debug --compress=2 --no-header-files --no-man-pages && java -jar packr-all-4.0.0.jar --platform linux64 --jdk jre --executable GSim --classpath gsim.jar --mainclass App --output GSim-lin