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

0. [Prerequisites](Prerequisites.md)
1. [Java Spring backend](#1-java-spring-backend)
2. [MongoDB](#2-mongodb)
3. [Nginx configuration](#3-nginx-configuration)
4. [Register backend as service](#4-register-backend-as-service)
5. [CI/CD pipeline](#5-cicd-pipeline)


   

## 1. Java Spring backend 
<img src = "src/main/resources/static/images/java.png" style="height:25px; margin-right: 15px;" /> <img src = "src/main/resources/static/images/spring.png" style="height:25px; margin-right: 15px;" /> 


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
  **- Built jar file** target\chatappjn-0.0.1-SNAPSHOT.jar

- Git  <a href="Git.md">
create remote repo, init, commit and  push
</a>

- Add ping endpoint

  - Create Controllers/PingController.java


## 2. MongoDB
<img src = "src/main/resources/static/images/mongodb.png" style="height:25px; margin-right: 15px;" /> 


- Create <a href="MongoDb.md"> user, database, collection and document
</a>

- Connect backend to DB
  - Update application.yaml
  - Add MongoDB dependency to pom.xml
  - Add Model
  - Add Repository - subclass of MongoRepository
  - Add Controller Controllers/PingDbController.java

## 3. Nginx configuration
<img src = "src/main/resources/static/images/nginx.jpg" style="height:25px; margin-right: 15px;" /> 


  - Create basic Nginx config file
  - Enable nginx on startup
  - Issue SSL certificate for the subdomain


## 4. Register backend as service

- Create <a href = "Service.md"> chatappjn.service file
</a>

- Reload systemd to register the service

      sudo systemctl daemon-reload

- Start/stop the service, check status, enable auto start on boot 

      sudo systemctl start chatappjn
      sudo systemctl stop chatappjn
      sudo systemctl status chatappjn
      sudo systemctl enable chatappjn

- Enable no password to restart service

      sudo visudo 
      barry75 ALL=(ALL) NOPASSWD: /bin/systemctl restart chatappjn
      barry75 ALL=(ALL) NOPASSWD: /bin/systemctl reload nginx
      barry75 ALL=(ALL) NOPASSWD: /bin/cp, /bin/ln, /usr/sbin/nginx

- Check no password commands for the user

      sudo -l -U barry75


- Follow logs in realtime

      sudo journalctl -u chatappjn -f



## 5. CI/CD pipeline
<img src = "src/main/resources/static/images/yaml.png" style="height:25px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/cicd.png" style="height:25px; margin-right: 15px;" /> 


  - Create yaml file for deployment, reload Nginx and restart backend service

