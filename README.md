# Optimal Parallel Scheduler

A program for finding the optimal solution to the parallel scheduling problem. Given a (small) set
of tasks and their dependence relations, this program finds the optimal schedule with which to run
them on a homogenous system.

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
                         (depth-first search branch and bound)
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

# Notes

- We’re using the Gradle [Shadow](https://imperceptiblethoughts.com/shadow) plugin in order to
  bundle all our dependencies into a single executable JAR file (known as a *fat-JAR*).

# Testing using Docker

We have decided to use docker to help us limit the resource use from our testing suite, and to be
able to deploy the testing of the application on a remote machine in an easy way.

In order to take advantage of docker, ensure that docker is installed and running, and then run the
following commands:

### Building the docker image

```
docker build -t {name-of-docker-image} .
```

An example would look like:

```
docker build -t se306 .
```

This will build a docker image.

### Running the docker image

Use the following

```
docker run --cpus {number-of-cores} {name-of-docker-image}
```

An example is:

```
docker run --cpus 5 se306
```

### Terminating the docker image

If the docker image is currently running, then you need to open a new instance of the terminal and
run:

```
docker kill $(docker ps -q)
```

### Deleting docker images

List all the docker images currently created:

```
docker images -a
```

Then remove the image:

```
docker rmi {your-image-id}
```

---

Note: In the event you get an error message such as the following:

```
Error response from daemon: conflict: unable to remove repository reference "se306" (must force) - container 1198400b4fcb is using its referenced image 404fb12e3abc
```

Then run the following commands:

```
docker stop 1198400b4fcb //replace this with whatever the terminal says is the container ID
docker rm 1198400b4fcb
```

### Note

There is a `dockerscript.sh` script provided that automates the cleaning and creating of the docker
image. However, **please be careful running this on your local machine as it will delete every
docker image and container you have on your system**.
