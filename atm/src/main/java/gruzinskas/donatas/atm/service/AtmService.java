package gruzinskas.donatas.atm.service;

import gruzinskas.donatas.atm.bean.CashDispenser;
import gruzinskas.donatas.atm.common.InsufficientFundsException;
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
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityNotFoundException;

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
        try {
            CardResponse cardResponse = bankCoreDemoApiApi.getAccountNumberByCard(moneyRequestDTO.getCardNumber());
            BalanceResponse balanceResponse = bankCoreDemoApiApi.getAccountBalance(cardResponse.getAccountNumber());
            Integer amount = moneyRequestDTO.getAmount();

            if (balanceResponse.getBalance() < amount) {
                throw new InsufficientFundsException("Not enough funds");
            }
            ReservationRequest reservationRequest = new ReservationRequest();
            reservationRequest.setAccountNumber(cardResponse.getAccountNumber());
            reservationRequest.setReservedAmount(amount);
            ReservationResponse reservationResponse = null;
            boolean cashDispensed = false;

            try {
                reservationResponse = bankCoreDemoApiApi.reserveMoneyOnAccount(reservationRequest);
                cashDispensed = cashDispenser.issueMoney(amount);
                if (cashDispensed) {
                    String result = bankCoreDemoApiApi.commitReservationOnAccount(reservationResponse.getReservationId());
                    logger.info(result);
                    return true;
                } else {
                    bankCoreDemoApiApi.reserveMoneyOnAccount1(reservationResponse.getReservationId());
                    return false;
                }
            } catch (HttpClientErrorException.NotFound e) {
                throw new EntityNotFoundException("Failed to reserve funds");
            }
            catch (HttpClientErrorException.Conflict e) {
                throw new InsufficientFundsException("Not enough funds");
            }
            catch (Exception e) {
                if (reservationResponse != null && !cashDispensed) {
                    bankCoreDemoApiApi.reserveMoneyOnAccount1(reservationResponse.getReservationId());
                    logger.info(reservationResponse.getReservationId() + " reverted.");
                }
                e.printStackTrace();
                throw new IllegalStateException("ATM has encountered difficulties");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Please enter a valid card number");
        }
    }
}
