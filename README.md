## Development

> **Note**
> Only JDK 17 is officially supported.

### ⌨️ Command line interface

```
usage: scheduler.jar INPUT.dot P [-h] [-p N] [-v] [-o OUTPUT] 

An algorithm for finding the optimal schedule for a given set of tasks and processors.

Positional arguments:
  INPUT.dot                  A task graph with integer weights in the dot format
  P                          The number of processes to schedule the INPUT graph on

Named arguments:
  -h, --help                 show this help message and exit
  -p N, --parallel N         Use N cores for execution in parallel (default is sequential)
  -v, --visualise            Visualise the search
  -o OUTPUT, --output OUTPUT The output file to write the schedule to (default is INPUT-output.dot)
```

#### 💾 Where files are saved

- By default, the output DOT file is saved to the same folder as the input DOT file.
- Relative pathnames are relative to the root of the project directory.

### ☕ Building and running

By default, the executable JAR file will be located at `/build/libs/scheduler.jar`

#### 🐧 Unix-like OSs

```bash
# Build JAR (with all dependencies included)
./gradlew shadowJar

# Execute JAR and show help message
java -jar ./build/libs/scheduler.jar --help
```

#### 🪟 Windows

```bash
# Build JAR (with all dependencies included)
.\gradlew shadowJar

# Execute JAR and show help message
java -jar .\build\libs\scheduler.jar --help
```

### 🐘 …Or use Gradle

#### 🐧 Unix-like OSs

```bash
# Run application and display help message
./gradlew run --args="--help"
```

#### 🪟 Windows

```bash
# Run application and display help message
.\gradlew run --args="--help"
```

## Notes

- We’re using the Gradle [Shadow](https://imperceptiblethoughts.com/shadow) plugin in order to bundle all our dependencies into a single executable JAR file (known as a *fat-JAR*).
