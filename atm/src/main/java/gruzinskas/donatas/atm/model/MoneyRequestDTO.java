package gruzinskas.donatas.atm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoneyRequestDTO {

    @NotBlank(message = "Card number cannot be blank")
    private String cardNumber;
    @Min(value = 0,message = "No negative values for withdrawal amount")
    @Max(value = 999999991, message = "ATM is not that rich")
    private Integer amount;


//    @NotNull
//    @Size(min = 1)
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

//    @Min(0)
//    @Max(999999999)
    public Integer getAmount() {
        return amount;

    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
