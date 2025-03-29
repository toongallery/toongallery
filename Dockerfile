## 1️⃣ JDK 17이 포함된 Gradle 공식 이미지 사용
#FROM gradle:7.6.2-jdk17 AS builder
#
## 2️⃣ 작업 디렉토리 설정
#WORKDIR /app
#
## 3️⃣ 프로젝트 파일 복사 및 빌드 실행
#COPY --chown=gradle:gradle . .
#
## 4️⃣ Gradle을 사용하여 JAR 파일 생성
#RUN gradle clean build -x test
#
## 5️⃣ JDK 17을 사용하여 최종 컨테이너 구성
#FROM openjdk:17-jdk-slim
#
## 6️⃣ 작업 디렉토리 설정
#WORKDIR /app
#
## 7️⃣ 빌드된 JAR 파일 복사
#COPY --from=builder /app/build/libs/*.jar app.jar
#
## 8️
#ENV SPRING_PROFILES_ACTIVE=dev
#
## 8️⃣ 컨테이너에서 실행될 명령어 설정
#CMD ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean build



FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "app.jar"]