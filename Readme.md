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


- Create <a href="basic-nginx-cfg.md">
basic Nginx config file chatjn.barryonweb.com
</a>
 
- Copy, Enable Nginx site and check sites enabled

    ```bash
    scp chatjn.barryonweb.com barry75@barryonweb.com:/var/www/chatapp/nginx/
    sudo cp /var/www/chatapp/nginx/chatjn.barryonweb.com /etc/nginx/sites-available/
    sudo ln -s /etc/nginx/sites-available/chatjn.barryonweb.com /etc/nginx/sites-enabled/
    ls -l /etc/nginx/sites-enabled/
    ```

- Issue SSL certificate for the subdomain

  ```bash
  sudo certbot --nginx -d chatjn.barryonweb.com
  ```

  - SSL manager will <a href="ssl-nginx-cfg.md">
update Nginx config file </a>

- Check Nginx syntax and reload

  ```bash
  sudo nginx -t
  sudo systemctl reload nginx
  ```


- Build backend, copy and run manually 

  ```bash
  mvn clean package
  scp target/chatappjn-0.0.1-SNAPSHOT.jar barry75@barryonweb.com:/var/www/chatapp/backend/chatjn/
  java -jar chatappjn-0.0.1-SNAPSHOT.jar
  ```

- Test 

  - Test locally
    ```bash
    curl http://localhost:8081/api/ping
    curl http://localhost:8081/api/pingdb
    ```

  - Test via Nginx + SSL
    ```bash
    curl -k https://chatjn.barryonweb.com/api/ping
    curl -k https://chatjn.barryonweb.com/api/pingdb
    ```


## 4. Register backend as service

- Create <a href = "Service.md"> chatappjn.service file
</a> and copy to /etc/systemd/system

  ```bash
  scp chatappjn.service barry75@barryonweb.com:/etc/systemd/system/ 
  ```

- Reload systemd to register the service

      sudo systemctl daemon-reload

- Start/stop the service, check status, enable auto start on boot 

      sudo systemctl start chatappjn
      sudo systemctl stop chatappjn
      sudo systemctl status chatappjn
      sudo systemctl enable chatappjn

- Follow logs in realtime

      sudo journalctl -u chatappjn -f

- Enable no password to restart service in /etc/sudoers and verify

      sudo visudo 
      barry75 ALL=(ALL) NOPASSWD: /bin/systemctl restart chatappjn.service
      sudo -l -U barry75



## 5. CI/CD pipeline
<img src = "src/main/resources/static/images/yaml.png" style="height:25px; margin-right: 15px;" /> <img src = "src/main/resources/static/images/cicd.png" style="height:25px; margin-right: 15px;" /> 


  - Create folder .github\workflows 
  - Add <a href="initial-yaml.md">yaml file</a> for deployment, reload Nginx and restart backend service
  - Github
    - Repo → Settings → Secrets and variables → Actions
      - Create SSH_PRIVATE_KEY in Repository Secrets
      - Copy content of ~\\.ssh\github_ci private key




