package com.jefferson.wallet.exceptions;

import java.time.Instant;

public record ErrorResponse(String errorTitle, String cause, Instant timestamp) {
}
