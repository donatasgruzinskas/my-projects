package gruzinskas.donatas.atm;

import gruzinskas.donatas.atm.bean.CashDispenser;
import gruzinskas.donatas.atm.common.InsufficientFundsException;
import gruzinskas.donatas.atm.model.MoneyRequestDTO;
import gruzinskas.donatas.atm.service.AtmService;
import io.swagger.client.api.BankCoreDemoApiApi;
import io.swagger.client.model.BalanceResponse;
import io.swagger.client.model.CardResponse;
import io.swagger.client.model.ReservationResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityNotFoundException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class AtmServiceTest {

    @MockBean
    public CashDispenser cashDispenser;

    @MockBean
    public BankCoreDemoApiApi bankCoreDemoApiApi;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    public AtmService atmService;

    @Before
    public void setup() {
        atmService = new AtmService(cashDispenser, bankCoreDemoApiApi);
    }

    @Test
    public void ShouldPassHappyPath() {
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("123");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("asdf");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("123");

        when(cashDispenser.issueMoney(anyInt())).thenReturn(true);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.commitReservationOnAccount(any())).thenReturn("OK");

        boolean result = atmService.getMoney(moneyRequestDTO);

        assertTrue(result);
    }

    @Test
    public void ShouldFailWithLowBalance() {
        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("Not enough funds");
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("123");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(10000);
        moneyRequestDTO.setCardNumber("123");

        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);

        atmService.getMoney(moneyRequestDTO);
    }

    @Test
    public void ShouldFailGracefullyWithWhenFailingToIssueMoney() {
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("123");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("asdf");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("123");

        when(cashDispenser.issueMoney(anyInt())).thenReturn(false);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.commitReservationOnAccount(any())).thenReturn("OK");
        when(bankCoreDemoApiApi.reserveMoneyOnAccount1(any())).thenReturn("OK");

        boolean result = atmService.getMoney(moneyRequestDTO);

        assertFalse(result);
    }

    @Test
    public void ShouldFailWhenReservationIsNotFound() {
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Failed to reserve funds");
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("123");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("asdf");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("123");

        when(cashDispenser.issueMoney(anyInt())).thenReturn(true);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.commitReservationOnAccount(any())).thenThrow(HttpClientErrorException.NotFound.class);

        atmService.getMoney(moneyRequestDTO);
    }

    @Test
    public void ShouldFailWhenBankApiReturnsConflict() {
        exceptionRule.expect(InsufficientFundsException.class);
        exceptionRule.expectMessage("Not enough funds");
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("123");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("123");

        when(cashDispenser.issueMoney(anyInt())).thenReturn(true);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenThrow(HttpClientErrorException.Conflict.class);

        atmService.getMoney(moneyRequestDTO);
    }

    @Test
    public void ShouldCatchUnexpectedException() {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("ATM has encountered difficulties");
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("123");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("asdf");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("123");

        when(cashDispenser.issueMoney(anyInt())).thenThrow(NullPointerException.class);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);

        atmService.getMoney(moneyRequestDTO);
    }

    @Test
    public void ShouldFailWhenCardIsNotFound() {
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Please enter a valid card number");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("123");

        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenThrow(HttpClientErrorException.NotFound.class);

        atmService.getMoney(moneyRequestDTO);
    }

}
