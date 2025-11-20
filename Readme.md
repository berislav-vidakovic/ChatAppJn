# MongoDB

## Install MongoDB

1. Import MongoDB public GPG key

    ```bash
    curl -fsSL https://pgp.mongodb.com/server-7.0.asc | sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg --dearmor
    ```

2. Add MongoDB repository

    ```bash
    echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] \
    https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | \
    sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
    ```

3. Install MongoDB

    ```bash
    sudo apt update
    sudo apt install -y mongodb-org
    ```

## Create users

4. Add security block to /etc/mongod.conf

    ```yaml
    security:
      authorization: enabled
    ```

5. Set permissions recursively

    ```bash
    sudo chown -R mongodb:mongodb /var/lib/mongodb
    sudo chown -R mongodb:mongodb /var/log/mongodb
    ```

6. Start with no access control 

    ```bash
    sudo mongod --dbpath /var/lib/mongodb --noauth --logpath /var/log/mongodb/noauth.log
    ```

7. Create user dbadmin in DB admin and verify

    ```bash
    mongosh
    use admin
    db.createUser({ user: "dbadmin", pwd: "abc123", roles: [ { role: "root", db: "admin" } ] })
    db.getUsers()
    exit
    ```

8. Stop no-auth mode, restart normally, login to DB admin with user dbadmin

    ```bash
    sudo pkill mongod
    sudo systemctl start mongod
    mongosh -u dbadmin -p --authenticationDatabase admin
    ```

9. Change password

    ```bash
    db.updateUser( "dbadmin", { pwd: "abc123" } )
    ```


10. Create DB,  create user within, Show DBs

    ```bash
    use chatappdb
    db.createUser({ user: "barry75", pwd: "abc123", roles: [ { role: "readWrite", db: "chatappdb" } ] })
    db.updateUser( "barry75", { roles: [ { role: "dbOwner", db: "chatappdb" } ] }  )
    show dbs
    ```

11. Login to DB with user defined within

    ```bash
    mongosh -u barry75 -p --authenticationDatabase chatappdb
    ```

12. Insert document into test collection

    ```bash
    db.test.insertOne({ pingdb: 'Hello world from MongoDB' })
    ```


13. Show all collections 

    ```bash
    show collections
    db.getCollectionNames()
    ```


14. Fetch content of Collection

    ```bash
    db.test.find()
    ```

15. Fetch document with particular field

    ```bash
    db.test.find({ pingdb: { $exists: true } })
    ```

16. Fetch only particular document, no id and print value

    ```bash
    db.test.find({ pingdb: { $exists: true } }, { pingdb: 1, _id: 0 } )
    let doc =db.test.findOne( { pingdb: { $exists: true } }, { pingdb: 1, _id: 0 } ); 
    print(doc.pingdb)
    ```

17. Make MongoDB available externally /etc/mongod.conf

    ```yaml
    net:
      port: 27017
      bindIp: 0.0.0.0
    ```

  - Download MongoDB Shell from https://www.mongodb.com/try/download/shell
  - Access from PowerShell

    ```powershell
    .\mongosh "mongodb://barry75@barryonweb.com:27017/chatappdb"
    ``` 

18. Connect Backend to MongoDB

 - Update application.yaml
 - Add MongoDB dependency to pom.xml
 - Add Model
 - Add Repository - subclass of MongoRepository
 - Add Controller

