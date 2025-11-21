# Java backend with MongoDb, CI/CD yaml and Nging deployment



<div style="margin-bottom: 12px;">
<img src = "src/main/resources/static/images/java.png" style="height:25px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/spring.png" style="height:25px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/mongodb.png" style="height:25px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/cicd.png" style="height:25px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/yaml.png" style="height:25px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/nginx.jpg" style="height:25px; margin-right: 15px;" /> 
</div>


## Table of Contents

0. [Prerequisites](#0-prerequisites)
1. [Java Spring backend](#1-java-spring-backend)
2. [MongoDB](#2-mongodb)
3. [Nginx configuration](#3-nginx-configuration)
4. [Register backend as service](#4-register-backend-as-service)
5. [CI/CD pipeline](#5-cicd-pipeline)

## 0. Prerequisites 

1. Created subdomain on VPS (production server)
2. Installed Nginx
3. Installed Java runtime
4. Installed MongoDB
5. Established SSH connections Dev-Github-Prod
6. Installed certbot for issuing SSL certificate

<a href="Prerequisites.md">
View Details of prerequisite steps
</a>


## 1. Java Spring backend

- Generate Spring Boot Project on  https://start.spring.io

    - Select latest stable Spring Boot (no RC2, SNAPSHOT), Java 21
    - Add Dependencies:

      - Spring Web (for REST API)
      - WebSocket (for WebSocket support)
      - Spring Boot DevTools
    
    - Download and Extract

- Define Port (default is 8080) in application.yaml

    ```yaml
    server:
      port: 8081
    ```

- Build, Run

    ```bash
    mvn clean package -DskipTests
    mvn spring-boot:run
    ```

- Git init, commit, push

  - Create Repo on GitHub
  - Run
      ```bash
      git init
      git add .
      git commit -m "Initial commit"
      ```
  - Get Remote Repo SSH link and run
      ```bash
      git remote add origin git@github.com:berislav-vidakovic/ChatAppJn.git
      ```

- Add ping endpoint

  - Create Controllers/PingController.java


## 2. MongoDB

<a href="MongoDb.md">

- Create user, database, collection and document
</a>

- Connect backend to DB
  - Update application.yaml
  - Add MongoDB dependency to pom.xml
  - Add Model
  - Add Repository - subclass of MongoRepository
  - Add Controller Controllers/PingDbController.java

## 3. Nginx configuration

  - Create basic Nginx config file
  - Enable nginx on startup
  - Issue SSL certificate for the subdomain


## 4. Register backend as service


## 5. CI/CD pipeline

  - Create yaml file for deployment, reload Nginx and restart backend service

