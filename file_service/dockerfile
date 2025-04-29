# Use the official SBT image with Scala 3.2.0 and Java 17
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0

RUN apt-get update && apt-get install -y cl-sql-sqlite3 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build.sbt ./
COPY project/ project/

COPY src/ src/

RUN sbt compile

CMD ["sbt", "run"]