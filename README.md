# my-lombok

실행환경
- Java 17
- IntelliJ

## 어노테이션 프로세스를 이용하여 .java 파일을 생성하는 방식
```shell
    % javac -d out/ $(find src/main/java/mylombok -name "*.java")
    % javac -d out/ -sourcepath src/main/java -processorpath out/ -processor main.java.mylombok.MyGetterProcessor $(find src/main/java -name "*.java")
    % java -cp out main.java.mylombok.MyLombokMain
```