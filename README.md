A toy project that demonstrates the use of JPMML-Model and JPMML-Evaluator APIs to "deep analyze" the predictions of Random Forest models.

# Prerequisites #

* Java 1.7 or newer

# Installation #

Currently, this project depends on the latest SNAPSHOT version of the JPMML-Evaluator library. This SNAPSHOT version is not distributed via Maven Central repository. It has to be built using Apache Maven from the latest GitHub source checkout as follows:

```
$ git clone https://github.com/jpmml/jpmml-evaluator.git
$ cd jpmml-evaluator
$ mvn clean install
```

In the future, this SNAPSHOT version can be replaced with any JPMML-Evaluator library version 1.2.5 or greater.

Once the latest SNAPSHOT version of the JPMML-Evaluator library has been successfully installed to the local Maven repository, it is possible to build this project using Apache Maven as usual:

```
$ mvn clean install
```

The build produces an executable uber-JAR file `target/rf-debugger-executable-1.0-SNAPSHOT.jar`, which contains an executable application class `org.jpmml.example.RfDebugger`.

# Usage #

Debugging a Random Forest PMML file that has been produced by R:

```
$ java -cp target/rf-debugger-executable-1.0-SNAPSHOT.jar org.jpmml.example.RfDebugger src/etc/R/RandomForestIris.pmml src/etc/R/Iris.csv
```

Debugging a Random Forest PMML file that has been produced by Python (Scikit-Learn):

```
$ java -cp target/rf-debugger-executable-1.0-SNAPSHOT.jar org.jpmml.example.RfDebugger src/etc/scikit-learn/RandomForestIris.pmml src/etc/scikit-learn/Iris.csv
```