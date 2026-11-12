package br.com.emprestimo.msemprestimo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsEmprestimoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsEmprestimoApplication.class, args);
    }
}
