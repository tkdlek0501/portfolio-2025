FROM openjdk:21-jdk

WORKDIR /app

# wait-for-it.sh 복사
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# JAR 복사
COPY build/libs/board-0.0.1-SNAPSHOT.jar app.jar

# wait-for-it으로 DB, Redis 대기 후 Spring 실행
CMD ["./wait-for-it.sh", "my-board-db:3306", "--", "./wait-for-it.sh", "my-cache-server:6379", "--", "java", "-jar", "app.jar"]