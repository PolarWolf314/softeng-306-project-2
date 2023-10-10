# Use openjdk17 as the base image
FROM openjdk:17-jdk

# Set the working directory in the image
WORKDIR /app

# Copy the Gradle project files (including build.gradle and settings.gradle)
COPY . .

RUN ./gradlew build -x test

# Run your test suite and redirect output to a file
CMD ["sh", "-c", "./gradlew test --tests nz.ac.auckland.se306.group12.optimal.OptimalSchedulerNodes10Test | tee ./build/reports/test-output.txt"]