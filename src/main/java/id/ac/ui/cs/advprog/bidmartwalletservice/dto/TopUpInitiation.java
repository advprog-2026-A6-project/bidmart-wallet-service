package id.ac.ui.cs.advprog.bidmartwalletservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopUpInitiation {
    private String status;
    private String virtualAccountNumber;
    private String virtualAccount;
    private String paymentReference;
    private Long amountToPay;
}
