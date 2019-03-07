package de.ruderphilipp.variance;

public class InvalidExpressionException extends IllegalArgumentException {
    InvalidExpressionException(final String message) {
        super(message);
    }
}
