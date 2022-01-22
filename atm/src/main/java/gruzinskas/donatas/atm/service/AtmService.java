package gruzinskas.donatas.atm.service;

import gruzinskas.donatas.atm.bean.CashDispenser;
import gruzinskas.donatas.atm.model.MoneyRequestDTO;
import io.swagger.client.api.BankCoreDemoApiApi;
import io.swagger.client.model.BalanceResponse;
import io.swagger.client.model.CardResponse;
import io.swagger.client.model.ReservationRequest;
import io.swagger.client.model.ReservationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AtmService {

    private Logger logger = LoggerFactory.getLogger(AtmService.class);

    private CashDispenser cashDispenser;

    private BankCoreDemoApiApi bankCoreDemoApiApi;

    @Autowired
    public AtmService(CashDispenser cashDispenser, BankCoreDemoApiApi bankCoreDemoApiApi) {
        this.cashDispenser = cashDispenser;
        this.bankCoreDemoApiApi = bankCoreDemoApiApi;
    }

    public boolean getMoney(MoneyRequestDTO moneyRequestDTO) {
        CardResponse cardResponse = bankCoreDemoApiApi.getAccountNumberByCard(moneyRequestDTO.getCardNumber());
        bankCoreDemoApiApi.getAccountBalance(cardResponse.getAccountNumber());

        Integer amount = moneyRequestDTO.getAmount();
        //Overdraft?
//        if (balanceResponse.getBalance() < amount) {
//            throw new InsufficientFundsException("Not enough funds");
//        }
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setAccountNumber(cardResponse.getAccountNumber());
        reservationRequest.setReservedAmount(amount);

        boolean cashDispensed = false;
        ReservationResponse reservationResponse = null;
        try {
            reservationResponse = bankCoreDemoApiApi.reserveMoneyOnAccount(reservationRequest);
            cashDispensed = cashDispenser.issueMoney(amount);
            if (cashDispensed) {
                bankCoreDemoApiApi.commitReservationOnAccount(reservationResponse.getReservationId());
                return true;
            } else {
                bankCoreDemoApiApi.reserveMoneyOnAccount1(reservationResponse.getReservationId());
                return false;
            }
        } catch (Exception e) {
            if (reservationResponse != null && !cashDispensed) {
                logger.info("reverting reservation " + reservationResponse.getReservationId());
                bankCoreDemoApiApi.reserveMoneyOnAccount1(reservationResponse.getReservationId());
                logger.info(reservationResponse.getReservationId() + " reverted.");
            }
            throw e;
        }
    }
}
