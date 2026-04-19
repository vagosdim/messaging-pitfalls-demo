package com.agileactors.validator.dto;

public record OddsValidationResponse(boolean valid, String reason, Double margin) {

}
