package de.ruderphilipp.variance;

/**
 * Represents an expression without content.
 */
class BlankExpression implements Expression {
    @Override
    public String getExpressionAsString() {
        return "";
    }
}
