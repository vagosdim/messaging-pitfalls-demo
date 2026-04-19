package com.agileactors.validator.controller;

import com.agileactors.validator.dto.OddsValidationRequest;
import com.agileactors.validator.dto.OddsValidationResponse;
import com.agileactors.validator.service.OddsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validator")
@RequiredArgsConstructor
public class OddsValidatorController {

    private final OddsValidator oddsValidator;

    @PostMapping("/validate-odds")
    public ResponseEntity<OddsValidationResponse> validateOdds(@RequestBody OddsValidationRequest oddsValidationRequest) throws InterruptedException {
        return ResponseEntity.ok(oddsValidator.validateOdds(oddsValidationRequest));
    }

}
