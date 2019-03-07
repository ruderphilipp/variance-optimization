package de.ruderphilipp.variance;

public class IncompleteExpressionException extends IllegalArgumentException {
    IncompleteExpressionException(final String message) {
        super(message);
    }
}
