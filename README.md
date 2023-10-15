# Optimal Parallel Scheduler

![Screenshot 2023-10-16T020945](https://github.com/UOASOFTENG306/project-2-project-2-team-12/assets/33956381/e7025ec9-1443-4263-9acf-d13ad2848bdf)

A program for finding the optimal solution to the parallel scheduling problem. Given a (small) set of tasks and their dependence relations, this program finds the optimal schedule with which to run them on a homogenous system.

> **Note**
> Only JDK 17 is officially supported.

## 💡 For best visualisation, maximise terminal window

The visualiser renders in the terminal from which you initiate this program, and will expand to fill the space it is given. Using a relatively large window size is recommended (at least 100&nbsp;×&nbsp;40), though you should probably just maximise the window. (No, really, you should.)

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
                         (default is dfs); options are astar (A* with ELS state
                         space), dfs (DFS branch-and-bound with ELS state space)
                         and ao (DFS B&B with allocation–ordering state space)
  -p N, --parallel N     use N cores for execution in parallel (default is 1,
                         sequential execution)
  -v, --visualise        visualise the search; nullifies the effect of -a,
                         forcing use of DFS B&B with ELS state space.
  -o OUTPUT, --output OUTPUT
                         write the resultant DOT file to path OUTPUT (default is
                         INPUT-output.dot); has no effect if -s is also set
  -s, --stdout           write the schedule to stdout instead of a file;
                         nullifies the effect of -o
```

By default, the output DOT file is saved to the same folder as the input DOT file.

## ☕ Building and running

By default, the executable JAR file will be located at `/build/libs/scheduler.jar`.

> **Note**
> We’re using the Gradle [Shadow](https://imperceptiblethoughts.com/shadow) plugin in order to bundle all our dependencies into a single executable JAR file (known as a *fat-JAR*).

### Unix-like OSs

```sh
# Build JAR (with all dependencies included)
./gradlew shadowJar

# Execute JAR and show help message
java -jar ./build/libs/scheduler.jar --help
```

### Windows

```sh
# Build JAR (with all dependencies included)
.\gradlew shadowJar

# Execute JAR and show help message
java -jar .\build\libs\scheduler.jar --help
```

## 🐘 …Or use Gradle’s `run` task

### Unix-like OSs

```sh
# Run application and display help message
./gradlew run --args="--help"
```

### Windows

```sh
# Run application and display help message
.\gradlew run --args="--help"
```

## 🐳 Testing with Docker

We use [Docker](https://www.docker.com) to manage the resource usage from our testing suite, and to deploy testing of the application on remote machines.

To take advantage of this, ensure you have Docker installed and running, and then run these commands:

### Building and running

```sh
# Build Docker image
docker build -t <name of docker image> .
```

For example: `docker build -t se306 .`.

```sh
# Run Docker image
docker run --cpus <number of cores> -v $(pwd)/build/test-results:/app/build/test-results <name of Docker image>
```

For example: `docker run --cpus 6 -v $(pwd)/build/test-results:/app/build/test-results se306`. This runs the Docker file with a specified number of cores, then copies the test results to the local machine.

### Stopping and removing

If the docker image is currently running, open a new terminal and run

```
# Terminate Docker image
docker kill $(docker ps -q)
```

### Deleting docker images

```
# List all existing Docker images
docker images -a

# Remove the image
docker rmi <your image ID>
```

If you get an error message like this:

```
Error response from daemon: conflict: unable to remove repository reference "se306" (must force) - container 1198400b4fcb is using its referenced image 404fb12e3abc
```

then run these commands:

```
docker stop 1198400b4fcb  # Replace with appropriate image ID
docker rm 1198400b4fcb
```


> **Important**
> A [`dockerscript.sh`](/dockerscript.sh) shell script is provided that automates the cleaning and creation of a Docker image. However, **do not run this unless you have read and understand what the script does**. It will delete **every** Docker image and container on your system.
