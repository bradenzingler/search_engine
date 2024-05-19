.PHONY: all clean run

all: compile run

compile:
	@echo Compiling Java sources...
	"C:/Program Files/Java/jdk-17.0.4.1/bin/javac.exe" -d bin src/main/java/com/java/*.java
	@echo Compilation complete.

run:
	@echo Running the Java application...
	cd 'c:/Users/Braden Zingler/Desktop/Summer projects/search_engine'; \
	java '@C:/Users/BRADEN~1/AppData/Local/Temp/cp_19pazl4gzltwckm6s9jg4awrb.argfile' 'com.java.Server'
	@echo Application finished.

clean:
	@echo Cleaning up...
	rm -rf bin/*.class
	@echo Clean complete.
