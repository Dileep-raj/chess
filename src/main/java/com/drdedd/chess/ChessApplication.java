package com.drdedd.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ChessApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ChessApplication.class, args);
            String decor = "~";
            String message = "Chess application started!";
            int total = 148, div = (total - message.length()) / 2;
            String pre = decor.repeat(div), repeat = decor.repeat(total);
            System.out.printf("%s\n%s%s%s%n%s%n", repeat, pre, message, pre, repeat);
        } catch (Exception e) {
            System.err.println("Error while launching application!");
            e.printStackTrace(System.err);
        }
    }

    @GetMapping(value = "/")
    public String helloWorld() {
        return "Hello World!<br/>Welcome to Chess";
    }

}
