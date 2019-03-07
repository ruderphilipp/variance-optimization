package de.ruderphilipp.variance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An operation combines multiple expressions.
 */
class Operation implements Expression {
    enum Type {
        OR, AND;

    }

    private final Type type;
    private final List<Expression> elements;

    private Operation(final Type type, final Collection<Expression> elements) {
        this.type = type;

        final List<Expression> sortedElements = new LinkedList<>(elements);
        // reduce tree depth if same operation type
        List<Expression> sameOperationType = new LinkedList<>();
        for (Expression e : sortedElements) {
            if (e instanceof Operation) {
                Operation op = (Operation)e;
                if (op.getType().equals(type)) {
                    sameOperationType.add(e);
                }
            }
        }
        for (Expression e : sameOperationType) {
            sortedElements.remove(e);
        }
        for (Expression e : sameOperationType) {
            Operation o = (Operation)e;
            sortedElements.addAll(o.getElements());
        }
        // remove duplicates
        Set<Expression> mySet = new HashSet<>(sortedElements);
        sortedElements.clear();
        sortedElements.addAll(mySet);
        // do sorting (void operation!)
        sortedElements.sort(Comparator.comparing(Expression::getExpressionAsString));
        // now assign the sorted result
        this.elements = sortedElements;
    }

    public static Expression create(final Type type, final Collection<Expression> elements) {
        Operation op = new Operation(type, elements);
        // if only "... OR" / "... AND" is left after removing duplicates etc.
        if (op.getElements().size() == 1) {
            return op.getElements().get(0);
        } else {
            return op;
        }
    }

    public Type getType() {
        return type;
    }

    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public String getExpressionAsString() {
        return String.join(" " + this.getType().toString() + " ",
                elements.stream()
                        .map(e -> {
                            String txt = e.getExpressionAsString();
                            if (e instanceof Operation) {
                                return "(" + txt + ")";
                            } else {
                                return txt;
                            }
                        })
                        .collect(Collectors.toList())
        );
    }

    @Override
    public int hashCode() {
        return getExpressionAsString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Operation) {
            Operation other = (Operation) obj;
            if (other.getType().equals(this.getType()) ||
                    other.getElements().equals(this.getElements())) {
                return true;
            }
        }
        return false;
    }
}
