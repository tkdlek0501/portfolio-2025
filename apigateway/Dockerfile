FROM openjdk:21-jdk

WORKDIR /app

# wait-for-it.sh 복사
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# JAR 복사 (gateway 모듈 기준)
COPY build/libs/apigateway-0.0.1-SNAPSHOT.jar app.jar

# wait-for-it으로 Redis만 대기
CMD ["./wait-for-it.sh", "my-cache-server:6379", "--", "java", "-jar", "app.jar"]