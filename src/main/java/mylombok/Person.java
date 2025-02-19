package main.java.mylombok;

public class Person {
    @MyGetter
    private String name;

    public Person(String name) {
        this.name = name;
    }
}
