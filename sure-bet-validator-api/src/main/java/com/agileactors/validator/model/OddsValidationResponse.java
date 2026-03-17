package com.agileactors.validator.model;

public record OddsValidationResponse(boolean valid, String reason, Double margin) {

}
