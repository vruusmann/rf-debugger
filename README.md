A toy project that demonstrates the use of JPMML-Model and JPMML-Evaluator APIs to "deep analyze" the predictions of Random Forest models.

# Prerequisites #

* Java 1.7 or newer

# Installation #

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):

```
mvn clean package
```

The build produces an executable uber-JAR file `target/rf-debugger-executable-1.0-SNAPSHOT.jar`, which contains an executable application class `org.jpmml.example.RfDebugger`.

# Usage #

Debugging a Random Forest PMML file that has been produced by R:

```
java -cp target/rf-debugger-executable-1.0-SNAPSHOT.jar org.jpmml.example.RfDebugger src/etc/R/RandomForestIris.pmml src/etc/R/Iris.csv
```

Debugging a Random Forest PMML file that has been produced by Python (Scikit-Learn):

```
java -cp target/rf-debugger-executable-1.0-SNAPSHOT.jar org.jpmml.example.RfDebugger src/etc/scikit-learn/RandomForestIris.pmml src/etc/scikit-learn/Iris.csv
```