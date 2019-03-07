package de.ruderphilipp.variance;

import java.util.*;
import java.util.stream.Collectors;

class ExpressionBuilder {

    private ExpressionBuilder() {
    }

    private final static String AND = " AND ";
    private final static String OR = " OR ";
    private static final int STEP_SIZE = 4; // either " OR " or " AND" (space gets trimmed away)

    public static Expression build(final String varianceExpression) {
        // error checking
        if (null == varianceExpression) {
            throw new IllegalArgumentException("Input value of NULL not valid!");
        }
        if (varianceExpression.trim().isEmpty()) {
            return new BlankExpression();
        }
        if (!varianceExpression.contains(" = ")) {
            throw new IllegalArgumentException("Input needs to have at least one assignment!");
        }

        String varExp = varianceExpression.trim();
        sanityCheckForParenthesis(varExp);

        Expression result;

        if (varExp.contains(AND) || varExp.contains(OR)) {
            // both types in the string?
            int countAnd = countMatches(AND, varExp);
            int countOr = countMatches(OR, varExp);
            if (countAnd > 0 && countOr > 0) {
                result = splitWithMultipleTypes(varExp);
            } else {
                // only one type
                String type = (countAnd > 0) ? AND : OR;
                result = splitWithOneType(type, varExp);
            }
        } else {
            // single assignment
            if (varExp.startsWith("(") && varExp.endsWith(")")) {
                varExp = varExp.substring(1, varExp.length() - 1);
            }
            String[] parts = varExp.split(" = ");
            if (parts.length != 2) {
                throw new IncompleteExpressionException("Incomplete assignment!");
            }
            result = new Assignment(parts[0], parts[1]);
        }

        return result;
    }

    private static void sanityCheckForParenthesis(String varExp) {
        if (varExp.contains("(") || varExp.contains(")")) {
            // sanity check
            {
                int countOpening = countMatches("\\(", varExp); // escaped because of RegExp
                int countClosing = countMatches("\\)", varExp);

                if (countOpening > countClosing) {
                    throw new InvalidExpressionException("More \"(\" than \")\"!");
                } else if (countOpening < countClosing) {
                    throw new InvalidExpressionException("More \")\" than \"(\"!");
                }
            }

            // find opening
            int openingPosition = varExp.indexOf("(");
            if (0 > openingPosition) {
                throw new IncompleteExpressionException("No opening parenthesis, but a closing one");
            }
            // find closing
            int closingPosition = varExp.lastIndexOf(")");
            if (0 > closingPosition) {
                throw new IncompleteExpressionException("No closing parenthesis!");
            }
            // sanity check
            if (openingPosition > closingPosition) {
                throw new InvalidExpressionException("Closing parenthesis before opening!");
            }
        }
    }

    private static int countMatches(final String needle, final String haystack) {
        if (null == needle || null == haystack) {
            throw new IllegalArgumentException("What should I do with NULL?");
        }
        if (needle.trim().isEmpty() || haystack.trim().isEmpty()) {
            throw new IllegalArgumentException("What should I do with empty values?");
        }

        // The suggested solution does not work if it is split by last character - in this case there is only one array
        // entry - thus, add a blank space to the end.
        String tmp = haystack + " ";
        // see https://stackoverflow.com/a/38620259
        return tmp.split(needle).length - 1;
    }

    private static Expression splitWithOneType(final String t, final String varianceExpression) {
        String varExp = varianceExpression.replace("(", "").replace(")", "");
        String[] parts = varExp.split(t);
        Set<String> uniqueParts = new HashSet<>(Arrays.asList(parts));
        List<Expression> children = new ArrayList<>();
        for (String p : uniqueParts) {
            children.add(build(p));
        }
        return Operation.create(getType(t), children);
    }

    private static Expression splitWithMultipleTypes(final String varianceExpression) {
        List<Integer> openingParentheses = findAllPositionsOf("(", varianceExpression);
        List<Integer> closingParentheses = findAllPositionsOf(")", varianceExpression);
        List<Integer> level0Positions = findLevelZeroPositions(openingParentheses, closingParentheses, varianceExpression.length());
        if (level0Positions.isEmpty()) {
            if (!openingParentheses.isEmpty()) {
                // "(...)" around complete statement
                return build(varianceExpression.substring(1, varianceExpression.length() - 1));
            } else {
                // no parenthesis
                // cannot be "-1" because this method is only entered if both exist
                level0Positions.add(varianceExpression.indexOf(AND));
                level0Positions.add(varianceExpression.indexOf(OR));
            }
        }

        int start = 0;
        List<String> textParts = new LinkedList<>();
        String type = null;
        for (int l0 : level0Positions) {
            // add the part left of the operator
            textParts.add(varianceExpression.substring(start, l0).trim());
            start = l0 + STEP_SIZE;

            // what operator?
            String myType = varianceExpression.substring(l0, start).trim().toLowerCase();
            if (null == type) {
                type = myType;
            } else {
                // sanity check
                if (!myType.equalsIgnoreCase(type)) {
                    throw new InvalidExpressionException("AND and OR on same level are forbidden!");
                }
            }
        }
        // add the right part after the last operator
        textParts.add(varianceExpression.substring(start).trim());

        // parse the texts
        List<Expression> parts = textParts.stream().map(ExpressionBuilder::build).collect(Collectors.toList());

        return Operation.create(getType(type), parts);
    }

    private static Operation.Type getType(final String type) throws UnsupportedOperationException {
        if (null == type) {
            throw new UnsupportedOperationException("Parameter must not be NULL!");
        }

        Operation.Type result;
        if (type.trim().equalsIgnoreCase(AND.trim())) {
            result = Operation.Type.AND;
        } else if (type.trim().equalsIgnoreCase(OR.trim())) {
            result = Operation.Type.OR;
        } else {
            throw new UnsupportedOperationException("Identified unknown operator type: " + type);
        }
        return result;
    }

    private static List<Integer> findAllPositionsOf(final String needle, final String haystack) {
        List<Integer> result = new LinkedList<>();
        // error checking
        if ((null == needle || needle.trim().isEmpty()) || (null == haystack || haystack.trim().isEmpty())) {
            return result;
        }

        int step = needle.length();

        int index = -1;
        do {
            index = haystack.indexOf(needle, index);
            if (index > -1) {
                result.add(index);
                index = index + step;
            }
        } while (index > -1);

        return result;
    }

    private static List<Integer> findLevelZeroPositions(final List<Integer> open, final List<Integer> close, final int maxLength) {
        List<Integer> result = new LinkedList<>();

        if ((null == open || null == close) || (open.isEmpty() || close.isEmpty())) {
            return result;
        }

        if (open.get(0) > 0) {
            // not at the beginning
            result.add(open.get(0) - STEP_SIZE);
        }

        Iterator<Integer> openIterator = open.iterator();
        Iterator<Integer> closeIterator = close.iterator();
        int level = 0;

        // go from one position to the next
        int nextOpening = openIterator.next();
        int nextClosing = -1;

        while (closeIterator.hasNext()) {
            nextClosing = closeIterator.next();
            while (nextOpening < nextClosing) {
                level++;
                if (openIterator.hasNext()) {
                    nextOpening = openIterator.next();
                } else {
                    break;
                }
            }
            // reached a ")"
            level--;
            if (level == 0) {
                // look at the string starting one character after the parenthesis
                int position = nextClosing + 1;
                if (position < maxLength) {
                    result.add(position);
                }
            }
        }
        // if the variant expression is like "(...) AND ..."
        if (result.isEmpty()) {
            // look at the string starting one character after the parenthesis
            int position = nextClosing + 1;
            if (position < maxLength) {
                result.add(position);
            }
        }

        return result;
    }
}
