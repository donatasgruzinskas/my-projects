package gruzinskas.donatas.atm;


import com.fasterxml.jackson.databind.ObjectMapper;
import gruzinskas.donatas.atm.bean.CashDispenser;
import gruzinskas.donatas.atm.controller.AtmController;
import gruzinskas.donatas.atm.model.MoneyRequestDTO;
import gruzinskas.donatas.atm.service.AtmService;
import io.swagger.client.api.BankCoreDemoApiApi;
import io.swagger.client.model.BalanceResponse;
import io.swagger.client.model.CardResponse;
import io.swagger.client.model.ReservationResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtmController.class)
@RunWith(SpringRunner.class)
@ComponentScan
public class AtmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AtmService atmService;

    @MockBean
    public CashDispenser cashDispenser;

    @MockBean
    public BankCoreDemoApiApi bankCoreDemoApiApi;

    @Before
    public void setup() {
        this.atmService = new AtmService(cashDispenser, bankCoreDemoApiApi);
    }


    @Test
    public void ShouldCatchFailedReservationRollback() throws Exception {
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("ACCOUNT_NUMBER");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("RESERVATION_ID");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");
        String bodyString = "No such reservation";

        when(cashDispenser.issueMoney(anyInt())).thenThrow(NullPointerException.class);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount1(any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT FOUND", bodyString.getBytes(), Charset.defaultCharset()));

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value(bodyString));

        verify(bankCoreDemoApiApi, times(1)).reserveMoneyOnAccount1("RESERVATION_ID");
    }

    @Test
    public void ShouldFailWhenBankApiFailsToReserveMoney() throws Exception {
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("ACCOUNT_NUMBER");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(10);
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");
        String bodyString = "Not enough funds";

        when(cashDispenser.issueMoney(anyInt())).thenReturn(true);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "BAD REQUEST", bodyString.getBytes(), Charset.defaultCharset()));

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value(bodyString));

        verify(bankCoreDemoApiApi, times(1)).reserveMoneyOnAccount(any());
        verify(cashDispenser, times(0)).issueMoney(anyInt());
    }

    @Test
    public void ShouldFailWhenCardIsNotFound() throws Exception {
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");
        String bodyString = "Non existing card";

        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT FOUND", bodyString.getBytes(), Charset.defaultCharset()));

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value(bodyString));

        verify(bankCoreDemoApiApi, times(1)).getAccountNumberByCard(any());
        verify(bankCoreDemoApiApi, times(0)).getAccountBalance(any());
    }

    @Test
    public void ShouldFailWhenReservationIsNotFound() throws Exception {
        CardResponse cardResponse = new CardResponse();
        cardResponse.setAccountNumber("ACCOUNT_NUMBER");
        BalanceResponse balanceResponse = new BalanceResponse();
        balanceResponse.setBalance(1000);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setReservationId("RESERVATION_ID");
        MoneyRequestDTO moneyRequestDTO = new MoneyRequestDTO();
        moneyRequestDTO.setAmount(100);
        moneyRequestDTO.setCardNumber("CARD_NUMBER");
        String bodyString = "No such reservation";

        when(cashDispenser.issueMoney(anyInt())).thenReturn(true);
        when(bankCoreDemoApiApi.getAccountNumberByCard(any())).thenReturn(cardResponse);
        when(bankCoreDemoApiApi.getAccountBalance(any())).thenReturn(balanceResponse);
        when(bankCoreDemoApiApi.reserveMoneyOnAccount(any())).thenReturn(reservationResponse);
        when(bankCoreDemoApiApi.commitReservationOnAccount(any())).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT FOUND", bodyString.getBytes(), Charset.defaultCharset()));

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value(bodyString));

        verify(bankCoreDemoApiApi, times(1)).commitReservationOnAccount("RESERVATION_ID");
        verify(bankCoreDemoApiApi, times(0)).reserveMoneyOnAccount1(any());
    }

}
