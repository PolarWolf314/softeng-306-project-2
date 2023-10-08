# Development

> **Note**
> Only JDK 17 is officially supported.

## ⌨️ Command line interface

```
usage: scheduler.jar [-h] [-a ALGORITHM] [-p N] [-v] [-o OUTPUT] [-s] INPUT.dot P
```

```
positional arguments:
  INPUT.dot              a task graph in DOT format, with non-negative integer
                         weights
  P                      the number of processors on which to schedule the INPUT
                         graph

named arguments:
  -h, --help             show this help message and exit
  -a ALGORITHM, --algorithm ALGORITHM
                         the algorithm with which to find the optimal schedule
                         (default is dfs); options are astar (A*) and dfs
                         (depth-first search)
  -p N, --parallel N     use N cores for execution in parallel (default is 1,
                         sequential execution)
  -v, --visualise        visualise the search
  -o OUTPUT, --output OUTPUT
                         write the resultant DOT file to path OUTPUT (default is
                         INPUT-output.dot); has no effect if -s is also set
  -s, --stdout           write the schedule to stdout instead of a file;
                         nullifies the effect of -o
```

By default, the output DOT file is saved to the same folder as the input DOT file.

## ☕ Building and running

By default, the executable JAR file will be located at `/build/libs/scheduler.jar`.

### 🐧 Unix-like OSs

```sh
# Build JAR (with all dependencies included)
./gradlew shadowJar

# Execute JAR and show help message
java -jar ./build/libs/scheduler.jar --help
```

### 🪟 Windows

```sh
# Build JAR (with all dependencies included)
.\gradlew shadowJar

# Execute JAR and show help message
java -jar .\build\libs\scheduler.jar --help
```

## 🐘 …Or use Gradle’s `run` task

### 🐧 Unix-like OSs

```sh
# Run application and display help message
./gradlew run --args="--help"
```

### 🪟 Windows

```sh
# Run application and display help message
.\gradlew run --args="--help"
```

# Notes

- We’re using the Gradle [Shadow](https://imperceptiblethoughts.com/shadow) plugin in order to bundle all our dependencies into a single executable JAR file (known as a *fat-JAR*).
