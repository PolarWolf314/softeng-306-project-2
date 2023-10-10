# kills all running docker containers
docker kill "$(docker ps -q)"

# removes all docker containers
docker rm "$(docker ps -a -q)"

# deletes all docker images
docker rmi "$(docker image -q)"

# creates a new docker image
docker build -t se306 .

# runs the new docker image in a container with 50% cpu utilisation
docker run --cpus 6 se306