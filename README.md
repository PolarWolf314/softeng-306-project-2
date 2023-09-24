## Development

### Command line interface

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

### Running the application

```bash
.\gradlew run --args="--help" # Run the application and display the help message
```

### Building executable jar

By default, this jar will be located at `build/libs/scheduler.jar`

```bash
# Build the jar with all the dependencies included
.\gradlew shadowJar

# Execute the jar
java -jar .\build\libs\scheduler.jar --help
```

## Notes

* We're using the Gradle [Shadow](https://imperceptiblethoughts.com/shadow/) plugin in order to
  bundle all our dependencies into a single executable jar file (Known as a *fat-jar*).