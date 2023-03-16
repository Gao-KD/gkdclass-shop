package org.example;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Main {
    private int id;
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}