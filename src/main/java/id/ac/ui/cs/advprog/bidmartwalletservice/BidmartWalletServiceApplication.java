package id.ac.ui.cs.advprog.bidmartwalletservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BidmartWalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidmartWalletServiceApplication.class, args);
    }

}
