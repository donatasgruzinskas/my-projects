package gruzinskas.donatas.atm;

import com.fasterxml.jackson.databind.ObjectMapper;
import gruzinskas.donatas.atm.controller.AtmController;
import gruzinskas.donatas.atm.model.MoneyRequestDTO;
import gruzinskas.donatas.atm.service.AtmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtmController.class)
@RunWith(SpringRunner.class)
public class AtmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private AtmService atmService;

    @Test
    public void HappyPath() throws Exception {
        when(atmService.getMoney(any())).thenReturn(true);
        MoneyRequestDTO moneyRequestDTO = MoneyRequestDTO.builder()
                .amount(100)
                .cardNumber("123")
                .build();

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void ShouldReturn400WhenServiceBlocks() throws Exception {
        when(atmService.getMoney(any())).thenReturn(false);
        MoneyRequestDTO moneyRequestDTO = MoneyRequestDTO.builder()
                .amount(100)
                .cardNumber("123")
                .build();

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("Not enough funds"));
    }

    @Test
    public void ShouldReturn400WhenCardNumberIsNull() throws Exception {
        MoneyRequestDTO moneyRequestDTO = MoneyRequestDTO.builder()
                .amount(50)
                .cardNumber(null)
                .build();

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("Card number cannot be blank"));
    }

    @Test
    public void ShouldReturn400WhenCardNumberIsEmpty() throws Exception {
        MoneyRequestDTO moneyRequestDTO = MoneyRequestDTO.builder()
                .amount(50)
                .cardNumber("")
                .build();

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("Card number cannot be blank"));
    }

    @Test
    public void ShouldReturn400WhenAmountIsNegative() throws Exception {
        MoneyRequestDTO moneyRequestDTO = MoneyRequestDTO.builder()
                .amount(-1)
                .cardNumber("123")
                .build();

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("No negative values for withdrawal amount"));
    }

    @Test
    public void ShouldReturn400WhenAmountIsTooLarge() throws Exception {
        MoneyRequestDTO moneyRequestDTO = MoneyRequestDTO.builder()
                .amount(999999992)
                .cardNumber("123")
                .build();

        this.mockMvc.perform(post("/issue/money")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").value("ATM is not that rich"));
    }


}
