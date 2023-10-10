# stops all running docker containers
docker stop $(docker ps -aq)

# removes all docker containers
docker rm $(docker ps -aq)

# deletes all docker images
docker rmi $(docker images -q)

# creates a new docker image
docker build -t se306 .

# runs the new docker image in a container with 50% cpu utilisation
docker run --cpus 6 -v $(pwd)/build/test-results:/app/build/test-results se306