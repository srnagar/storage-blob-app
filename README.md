# storage-blob-app

### Compile:
```shell
mvn clean install
```

### Create a file of 100MB size

Run this from `./target` directory
```bash
head -c 100M </dev/urandom >file-to-upload
```

### Execute
Run this from `./target` directory

```shell
java -jar storage-blob-app-1.0-SNAPSHOT.jar
```

