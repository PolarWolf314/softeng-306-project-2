FROM ubuntu:latest
LABEL authors="aaron"

ENTRYPOINT ["top", "-b"]