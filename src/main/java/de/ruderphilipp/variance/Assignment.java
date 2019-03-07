package de.ruderphilipp.variance;

/**
 * An assignment is something like "AAA = BBB".
 *
 * It is the leaf node of a tree and cannot have sub-elements.
 */
class Assignment implements Expression {
    private final String family;
    private final String value;

    Assignment(final String family, final String value) {
        if (null == family || family.trim().isEmpty()) {
            throw new IllegalArgumentException("Option family must not be empty!");
        }

        if (null == value || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Option value must not be empty!");
        }

        this.family = family.trim();
        this.value = value.trim();
    }

    public String getFamily() {
        return family;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getExpressionAsString() {
        return getFamily() + " = " + getValue();
    }


    @Override
    public int hashCode() {
        return getExpressionAsString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Assignment) {
            Assignment other = (Assignment) obj;
            if (other.getFamily().equals(this.getFamily()) ||
                    other.getValue().equals(this.getValue())) {
                return true;
            }
        }
        return false;
    }
}
