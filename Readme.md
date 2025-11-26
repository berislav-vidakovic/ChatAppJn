# Java backend with MongoDb, CI/CD and Nginx 



<div style="margin-bottom: 12px;">
<img src = "src/main/resources/static/images/java.png" style="height:35px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/spring.png" style="height:35px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/mongodb.png" style="height:35px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/cicd.png" style="height:35px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/yaml.png" style="height:35px; margin-right: 15px;" /> 
<img src = "src/main/resources/static/images/nginx.jpg" style="height:35px; margin-right: 15px;" /> 
</div>


## Table of Contents

0. [Prerequisites](docs/Prerequisites.md)
1. [Java Spring backend](#1-java-spring-backend)
2. [MongoDB](#2-mongodb)
3. [Nginx configuration](#3-nginx-configuration)
4. [Register backend as service](#4-register-backend-as-service)
5. [CI/CD pipeline](#5-cicd-pipeline)
6. [WebSocket](#6-websocket)
7. [Get all users from MongoDB](#7-get-all-users-from-mongodb)
8. [Register new User and hashing password](#8-register-new-user-and-hashing-password)
9. [JWT Authentication incremental build](#9-jwt-authentication-incremental-build)



   

## 1. Java Spring backend 
<img src = "src/main/resources/static/images/java.png" style="height:35px; margin-right: 15px;" /> <img src = "src/main/resources/static/images/spring.png" style="height:35px; margin-right: 15px;" /> 


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

- Git  <a href="docs/Git.md">
create remote repo, init, commit and  push
</a>

- Add ping endpoint

  - Create Controllers/PingController.java

- Add CORS policy for frontend access

  - Create <a href="docs/CORS.md">Config/CorsConfig.java</a>


## 2. MongoDB
<img src = "src/main/resources/static/images/mongodb.png" style="height:35px; margin-right: 15px;" /> 

- Access MongoDB remotely or locally

  ```bash
  mongosh "mongodb://barry75@barryonweb.com:27017/chatappdb"
  mongosh -u barry75 -p --authenticationDatabase chatappdb
  ```

- Create <a href="docs/MongoDb.md"> user, database, collection and document
</a>



- Connect backend to DB
  - Update application.yaml with DB connection details
  - Add MongoDB dependency to pom.xml
  - Add Model
  - Add Repository - subclass of MongoRepository
  - Add Controller Controllers/PingDbController.java

## 3. Nginx configuration
<img src = "src/main/resources/static/images/nginx.jpg" style="height:35px; margin-right: 15px;" /> 


- Create <a href="docs/basic-nginx-cfg.md">
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

  - SSL manager will <a href="docs/ssl-nginx-cfg.md">
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

- Create <a href = "docs/Service.md"> chatappjn.service file
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
<img src = "src/main/resources/static/images/yaml.png" style="height:35px; margin-right: 15px;" /> <img src = "src/main/resources/static/images/cicd.png" style="height:35px; margin-right: 15px;" /> 


  - Create folder .github\workflows 
  - Add <a href="docs/initial-yaml.md">yaml file</a> for deployment, reload Nginx and restart backend service
  - Github
    - Repo → Settings → Secrets and variables → Actions
      - Create SSH_PRIVATE_KEY in Repository Secrets
      - Copy content of ~\\.ssh\github_ci private key


## 6. Websocket 

- Add 4 Java files
  - Services/<a href="docs/Client.md">Client.java</a>
  - Services/<a href="docs/IdleMonitor.md">IdleMonitor.java</a>
  - WebSockets/<a href="docs/SessionMonitor.md">SessionMonitor.java</a>
  - WebSockets/<a href="docs/WebSocketHandler.md">WebSocketHandler.java</a>

- Add <a href="docs/WebSocketCfg.md">WebSocketConfig file</a>
- Add timeout parameters in application.yaml

    ```yaml
    websocket:
      timeout-mins: 1
      check-interval-sec: 45
    ```

## 7. Get all users from MongoDB

- Create and populate users collection

  ```js
  db.users.insertMany([
    { login: "shelly", full_name: "Sheldon", isonline: false },
    { login: "lenny",  full_name: "Leonard", isonline: false },
    { login: "raj",    full_name: "Rajesh",  isonline: false },
    { login: "howie",  full_name: "Howard",  isonline: false }
  ])
  db.users.find()
  ```
- Add Model, Repository, Controller
- MongoDB automatically generates string id field

## 8. Register new User and hashing password 

- Extend collection documents with new field with password (update all or only documents where password is missing)

  ```js
  db.users.updateMany(
    {},
    { $set: { password: "" } }
  )
  db.users.updateMany(
    { password: { $exists: false } },
    { $set: { password: "" } }
  )
  ```

- Add passwordEncoder dependency to pom.xml
- Add <a href="docs/SecurityConfig.md">SecurityConfig.java</a> with @Bean PasswordEncoder and SecurityFilterChain   
- Add @Autowired PasswordEncoder, ObjectMapper, WebSocketHandler to UsersController

## 9. JWT Authentication incremental build

### 1. Refresh endpoint returning dummy tokens

- AuthorizationController.java 
  - Received Request: { refreshToken } 
  - Sending Response  { dummyAccessToken, dummyRefreshToken }
  - Sending WS broadcast { type: userSessionUpdate, data } 

- Enable endpoint /api/auth/refresh in SecurityConfig.java

    ```java
    .requestMatchers("/api/auth/refresh").permitAll() 
    ```

### 2. Create Model and Repository with dummy Controller check

- Add RefreshToken class (Model) that matches MongoDB collection
  ```js
  { "_id", "userid", "token", "expires" } 
  ```
- Add RefreshTokenRepository class   
- Check in Controller if the received token exists in MongoDB collection
  - if exists
    - if expired send code 201 (Created)
    - if valid send code 200 (OK)
  - if it does not exist send code 201 (Created)
  - Response sending 
    - dummyAccessToken
    - refreshToken
      - dummy for status 201
      - received one for status 200

### 3. Controller check upgrade

- For invalid or expired refreshToken
  - Return HttpStatus.UNAUTHORIZED (401)  
- For valid refreshToken
  - Find user in users collection by userId from refreshToken collection
  - Update user status to online
  - Generate new refreshToken
  - Return HttpStatus.OK (200) with { dummyAccessToken, refreshToken, userId, isOnline: true }   
- MongoDB CRUD actions
  - **Delete** dummy users from user collection
    ```js
    db.users.deleteOne({login:'p'})
    db.users.deleteMany({login: { $in:['b','c']}})
    ```

  - **Read** userid by full name:
    ```js
    db.users.find({full_name:'Sheldon'},{_id:1})
    ```
    OUTPUT: [ { _id: ObjectId('692326918a68875daa63b113') } ]  
  - **Create** dummy token with valid userid in MongoDB:
    ```js
    db.refreshTokens.insertOne({ 
      userid: ObjectId('692326918a68875daa63b113'), 
      token:"initialRefreshToken", 
      expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) })
    ```  
  - **Update** token by userId
    ```js
    db.refreshTokens.updateOne(
      { userid: ObjectId("692326918a68875daa63b113") },  // filter
      {
        $set: {
          token: "validRefreshToken",
          expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
        }
      }
    );
    ```
- Added UserRepository reference (Autowired) to AuthController
  - Update user status to online for user in users collection found by userId from refreshToken collection

- Renewed refreshToken dummy: newRefreshToken

- Testing on Frontend  
  1. Request { refreshToken: dummyRefreshToken }  
      - Expected Response: HttpStatus.UNAUTHORIZED (401)
  2. Request { refreshToken: validRefreshToken }  
      - Token valid - renew refreshToken and Expiry date and save to MongoDB collection (update)
      - Expected Response: HttpStatus.OK (200) { dummyAccessToken, newRefreshToken, userId, isOnline: true }

### 4. Valid refresh token path - token renewal

- Added UserMonitor
- Added useridle timeout and interval into application.yaml
- Updated SessionMonitor for Autologout if user is logged in
- Added JWT dependencies into pom.xml
- Added Config/JwtBuilder.java 
  ```java
  public class JwtBuilder {
    private static final Key SECRET_KEY = 
      Keys.hmacShaKeyFor("KeyForJWTauthenticationInChatApp".getBytes());
    private static final long EXPIRATION_TIME_MS = 60*60*1000; // 1 hour
    public static String generateToken(String userId, String username) {
      return Jwts.builder()
              .setSubject(username)
              .claim("userId", userId)
              .setIssuedAt(new Date())
              .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
              .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
              .compact();
    }
  }
  ```
- AuthController
  - Call UserMonitor::updateUserActivity on user login (valid refreshToken)
  - Generate new refreshToken and save to DB 
    ```java
    String refreshToken = UUID.randomUUID().toString();
    ```
  - Generate new accessToken 
    ```java
    String newAccessToken = JwtBuilder.generateToken(user.getId(), user.getLogin());
    ```

### 5. Handling Request with accessToken in Authorization Bearer header

- Added static method getSecretKey in Config/JwtBuilder.java
- Added file Config/JwtValidator.java 
  ```java
  try { 
    Claims claims = Jwts.parserBuilder()
      .setSigningKey(JwtBuilder.getSecretKey())  
      .build()
      .parseClaimsJws(accessJWT)
      .getBody();    
    request.setAttribute("userId", claims.getSubject());
  } 
  catch (Exception e) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write("Invalid or expired token");
    return;
  }
  ```
  - Request enters JwtValidator filter first. The filter runs before any controller

- Response Status code For missing Authorization header: 400 (Bad Request) 
- Response Status code For invalid/expired accessToken: 401 (Unauthorized) 

