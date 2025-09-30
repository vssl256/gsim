@echo on
rmdir /s GSim
java -jar packr-all-4.0.0.jar --platform linux64 --jdk jre1.8.0_441 --executable GSim --classpath gsim.jar --mainclass App --output GSim