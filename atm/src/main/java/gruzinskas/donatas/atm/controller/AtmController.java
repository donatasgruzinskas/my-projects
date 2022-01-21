package gruzinskas.donatas.atm.controller;

import gruzinskas.donatas.atm.model.MoneyRequestDTO;
import gruzinskas.donatas.atm.service.AtmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Validated
public class AtmController {

    private AtmService atmService;

    @Autowired
    public AtmController(AtmService atmService) {
        this.atmService = atmService;
    }

    @PostMapping(value = "/issue/money", consumes = "application/json")
    public ResponseEntity<Object> getMoney(@Valid @RequestBody MoneyRequestDTO moneyRequestDTO) {
        boolean result = atmService.getMoney(moneyRequestDTO);
        if (result){
            return ResponseEntity.ok("OK");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("errors", "Not enough funds");
//        return ResponseEntity.badRequest().body("errors", "Not enough funds");
        return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
