package com.agileactors.validator.controller;

import com.agileactors.validator.model.OddsChange;
import com.agileactors.validator.model.OddsValidationResponse;
import com.agileactors.validator.service.OddsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OddsApiController {

    private final OddsValidator oddsValidator;

    @PostMapping("/validate-odds")
    public OddsValidationResponse validateOdds(@RequestBody OddsChange oddsChange) throws InterruptedException {
        return oddsValidator.validateOdds(oddsChange);
    }

    @PostMapping("/broadcast")
    public void broadcast(@RequestBody OddsChange oddsChange) throws InterruptedException {
        oddsValidator.broadcastOddsChange(oddsChange);
    }
}
