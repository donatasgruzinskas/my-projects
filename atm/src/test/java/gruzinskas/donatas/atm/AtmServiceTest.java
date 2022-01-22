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
import static org.mockito.Mockito.*;

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
        cardResponse.setAccountNumber("ACCOUNT_NUMBER");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("RESERVATION_ID");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");

        when(cashDispenser.issueMoney(anyInt())).thenReturn(true);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.commitReservationOnAccount(any())).thenReturn("OK");

        boolean result = atmService.getMoney(moneyRequestDTO);

        assertTrue(result);
        verify(bankCoreDemoApiApi, times(1)).reserveMoneyOnAccount(any());
        verify(cashDispenser, times(1)).issueMoney(anyInt());
        verify(bankCoreDemoApiApi, times(1)).commitReservationOnAccount(any());
        verify(bankCoreDemoApiApi, times(0)).reserveMoneyOnAccount1(any());
    }

    @Test
    public void ShouldFailGracefullyWithWhenFailingToIssueMoney() {
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("ACCOUNT_NUMBER");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("RESERVATION_ID");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");

        when(cashDispenser.issueMoney(anyInt())).thenReturn(false);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.commitReservationOnAccount(any())).thenReturn("OK");
        when(bankCoreDemoApiApi.reserveMoneyOnAccount1(any())).thenReturn("OK");

        boolean result = atmService.getMoney(moneyRequestDTO);

        assertFalse(result);
        verify(bankCoreDemoApiApi, times(1)).reserveMoneyOnAccount(any());
        verify(cashDispenser, times(1)).issueMoney(anyInt());
        verify(bankCoreDemoApiApi, times(1)).reserveMoneyOnAccount1(any());
    }

    @Test
    public void ShouldRollbackReservationUponFailureToIssueMoneyWithAnUnexpectedError() {
        exceptionRule.expect(IllegalStateException.class);
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("ACCOUNT_NUMBER");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("RESERVATION_ID");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");

        when(cashDispenser.issueMoney(anyInt())).thenThrow(IllegalStateException.class);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);

        atmService.getMoney(moneyRequestDTO);

        verify(bankCoreDemoApiApi, times(0)).reserveMoneyOnAccount(any());
        verify(bankCoreDemoApiApi, times(1)).reserveMoneyOnAccount1(any());
    }

}
