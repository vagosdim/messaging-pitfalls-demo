package com.agileactors.pitfalls.model;

public record OddsValidationResponse(boolean valid, String reason, Double margin) {
}
