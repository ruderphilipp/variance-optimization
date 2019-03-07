package de.ruderphilipp.variance;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionBuilderTest {

    @Test
    void doesNotWorkIfNoAssignment() {
        assertThrows(IllegalArgumentException.class, () -> ExpressionBuilder.build(null), "NULL is possible");
        assertThrows(IllegalArgumentException.class, () -> ExpressionBuilder.build("AAA"), "Text is possible");

        try {
            String in = RandomStringUtils.randomAlphanumeric(1, 8);
            ExpressionBuilder.build(in);
            fail("Random string is possible: " + in);
        } catch (IllegalArgumentException e) {
        }

        assertThrows(IllegalArgumentException.class, () -> ExpressionBuilder.build("AAA=BBB"), "Text without spaces at assignment is possible");
    }

    @Test
    void shouldReturnBlankForBlankInput() {
        // "normal" blank
        Expression e = ExpressionBuilder.build("");
        assertEquals("", e.getExpressionAsString());
        assertTrue(e instanceof BlankExpression);

        // multiple spaces
        Expression e2 = ExpressionBuilder.build("   ");
        assertEquals("", e2.getExpressionAsString());
        assertTrue(e2 instanceof BlankExpression);
    }

    @Test
    void shouldReturnErrorIfParenthesisNotClosed() {
        assertThrows(InvalidExpressionException.class, () -> ExpressionBuilder.build("(AAA = BBB"));
    }

    @Test
    void shouldReturnErrorIfParenthesisNotClosed2() {
        assertThrows(InvalidExpressionException.class, () -> ExpressionBuilder.build("AAA = BBB)"));
    }

    @Test
    void shouldReturnErrorIfParenthesisInWrongOrder() {
        assertThrows(InvalidExpressionException.class, () -> ExpressionBuilder.build(")AAA = BBB("));
    }

    @Test
    void shouldReturnErrorIfTooManyParenthesis() {
        assertThrows(InvalidExpressionException.class, () -> ExpressionBuilder.build("((AAA = BBB)))"));
    }

    @Test
    void shouldReturnErrorIfAssignmentIncomplete() {
        assertThrows(IncompleteExpressionException.class, () -> ExpressionBuilder.build("AAA = "));
    }

    @Test
    void shouldParseSingleAssignment() {
        Expression e = ExpressionBuilder.build("AAA = BBB");
        assertTrue(e instanceof Assignment, "got wrong type: " + e.getClass().getName());
        Assignment a = (Assignment) e;
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());
        assertEquals("AAA = BBB", a.getExpressionAsString());
    }

    @Test
    void shouldParseSingleAssignmentWithRandomValues() {
        String option = RandomStringUtils.randomAlphanumeric(1, 20);
        String value = RandomStringUtils.randomAlphanumeric(1, 20);
        String in = option + " = " + value;

        Expression e = ExpressionBuilder.build(in);
        assertTrue(e instanceof Assignment, "got wrong type: " + e.getClass().getName());
        Assignment a = (Assignment) e;
        assertEquals(option, a.getFamily());
        assertEquals(value, a.getValue());
        assertEquals(in, a.getExpressionAsString());
    }

    @Test
    void shouldParseSingleAssignmentWithArbitraryManySpaces() {
        Expression e = ExpressionBuilder.build("      AAA    =  BBB    ");
        assertTrue(e instanceof Assignment, "got wrong type: " + e.getClass().getName());
        Assignment a = (Assignment) e;
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());
        assertEquals("AAA = BBB", a.getExpressionAsString());
    }

    @Test
    void shouldParseSimpleOrStatement() {
        Expression e = ExpressionBuilder.build("AAA = BBB OR CCC = DDD");
        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.OR, o.getType());
        List<Expression> children = o.getElements();
        assertEquals(2, children.size());

        Assignment a = (Assignment) children.get(0);
        assertEquals("AAA = BBB", a.getExpressionAsString());
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());

        Assignment b = (Assignment) children.get(1);
        assertEquals("CCC = DDD", b.getExpressionAsString());
        assertEquals("CCC", b.getFamily());
        assertEquals("DDD", b.getValue());
    }

    @Test
    void shouldParseSimpleAndStatement() {
        Expression e = ExpressionBuilder.build("AAA = BBB AND CCC = DDD");
        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.AND, o.getType());
        List<Expression> children = o.getElements();
        assertEquals(2, children.size());

        Assignment a = (Assignment) children.get(0);
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());
        assertEquals("AAA = BBB", a.getExpressionAsString());

        Assignment b = (Assignment) children.get(1);
        assertEquals("CCC", b.getFamily());
        assertEquals("DDD", b.getValue());
        assertEquals("CCC = DDD", b.getExpressionAsString());
    }

    @Test
    void shouldReturnSortedOr() {
        String in = "AA = EEE OR AA = DDD OR AA = CCC OR AA = BBB OR AA = AAA";
        String expected = "AA = AAA OR AA = BBB OR AA = CCC OR AA = DDD OR AA = EEE";
        Expression e = ExpressionBuilder.build(in);
        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.OR, o.getType());
        assertEquals(expected, e.getExpressionAsString());
    }

    @Test
    void shouldRemoveParenthesisForSingleAssignments() {
        String in = "(AA = EEE) OR (AA = DDD) OR (AA = CCC) OR (AA = BBB) OR (AA = AAA)";
        String expected = "AA = AAA OR AA = BBB OR AA = CCC OR AA = DDD OR AA = EEE";
        assertEquals(expected, ExpressionBuilder.build(in).getExpressionAsString());
    }

    @Test
    void shouldParseSimpleCombination1() {
        String in = "AAA = BBB AND (CCC = DDD OR EEE = FFF)";
        Expression e = ExpressionBuilder.build(in);
        assertEquals(in, e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o1 = (Operation) e;
        assertEquals(Operation.Type.AND, o1.getType());
        List<Expression> children = o1.getElements();
        assertEquals(2, children.size());

        Assignment a = (Assignment) children.get(0);
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());
        assertEquals("AAA = BBB", a.getExpressionAsString());

        Expression o2 = children.get(1);
        assertTrue(o2 instanceof Operation);
        Operation o2_ = (Operation) o2;
        assertEquals(Operation.Type.OR, o2_.getType());
        List<Expression> children2 = o2_.getElements();
        assertEquals(2, children2.size());

        Assignment b = (Assignment) children2.get(0);
        assertEquals("CCC", b.getFamily());
        assertEquals("DDD", b.getValue());
        assertEquals("CCC = DDD", b.getExpressionAsString());

        Assignment c = (Assignment) children2.get(1);
        assertEquals("EEE", c.getFamily());
        assertEquals("FFF", c.getValue());
        assertEquals("EEE = FFF", c.getExpressionAsString());
    }

    @Test
    void shouldParseSimpleCombination2() {
        String in = "(AAA = BBB AND CCC = DDD) OR EEE = FFF";
        Expression e = ExpressionBuilder.build(in);
        assertEquals(in, e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o1 = (Operation) e;
        assertEquals(Operation.Type.OR, o1.getType());
        List<Expression> children = o1.getElements();
        assertEquals(2, children.size());

        Expression o2 = children.get(0);
        assertTrue(o2 instanceof Operation);
        Operation o2_ = (Operation) o2;
        assertEquals(Operation.Type.AND, o2_.getType());
        List<Expression> children2 = o2_.getElements();
        assertEquals(2, children2.size());

        Assignment a = (Assignment) children2.get(0);
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());
        assertEquals("AAA = BBB", a.getExpressionAsString());

        Assignment b = (Assignment) children2.get(1);
        assertEquals("CCC", b.getFamily());
        assertEquals("DDD", b.getValue());
        assertEquals("CCC = DDD", b.getExpressionAsString());

        Assignment c = (Assignment) children.get(1);
        assertEquals("EEE", c.getFamily());
        assertEquals("FFF", c.getValue());
        assertEquals("EEE = FFF", c.getExpressionAsString());
    }

    @Test
    void shouldParseSimpleCombination2a() {
        String in = "(AAA = BBB AND CCC = DDD) OR EEE = FFF";
        Expression e = ExpressionBuilder.build("(" + in + ")");
        assertEquals(in, e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o1 = (Operation) e;
        assertEquals(Operation.Type.OR, o1.getType());
        List<Expression> children = o1.getElements();
        assertEquals(2, children.size());

        Expression o2 = children.get(0);
        assertTrue(o2 instanceof Operation);
        Operation o2_ = (Operation) o2;
        assertEquals(Operation.Type.AND, o2_.getType());
        List<Expression> children2 = o2_.getElements();
        assertEquals(2, children2.size());

        Assignment a = (Assignment) children2.get(0);
        assertEquals("AAA", a.getFamily());
        assertEquals("BBB", a.getValue());
        assertEquals("AAA = BBB", a.getExpressionAsString());

        Assignment b = (Assignment) children2.get(1);
        assertEquals("CCC", b.getFamily());
        assertEquals("DDD", b.getValue());
        assertEquals("CCC = DDD", b.getExpressionAsString());

        Assignment c = (Assignment) children.get(1);
        assertEquals("EEE", c.getFamily());
        assertEquals("FFF", c.getValue());
        assertEquals("EEE = FFF", c.getExpressionAsString());
    }

    @Test
    void shouldReturnErrorIfBothOperatorsOnSameLevel1() {
        assertThrows(InvalidExpressionException.class, () -> ExpressionBuilder.build("AAA = BBB AND CCC = DDD OR EEE = FFF"));
    }

    @Test
    void shouldMoveSameOperatorChildIntoParentOr() {
        String in = "(AAA = CCC OR AAA = BBB) OR AAA = AAA";
        Expression e = ExpressionBuilder.build(in);
        assertEquals("AAA = AAA OR AAA = BBB OR AAA = CCC", e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.OR, o.getType());
        List<Expression> children = o.getElements();
        assertEquals(3, children.size());
    }

    @Test
    void shouldMoveSameOperatorChildIntoParentAnd() {
        String in = "(AAA = CCC AND AAA = BBB) AND AAA = AAA";
        Expression e = ExpressionBuilder.build(in);
        assertEquals("AAA = AAA AND AAA = BBB AND AAA = CCC", e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.AND, o.getType());
        List<Expression> children = o.getElements();
        assertEquals(3, children.size());
    }

    @Test
    void shouldMoveSameOperatorChildIntoParent1() {
        String in = "(AAA = CCC OR AAA = BBB) AND (XXX = ZZZ AND YYY = YYY)";
        Expression e = ExpressionBuilder.build(in);
        assertEquals("(AAA = BBB OR AAA = CCC) AND XXX = ZZZ AND YYY = YYY", e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.AND, o.getType());
        List<Expression> children = o.getElements();
        assertEquals(3, children.size());
    }

    @Test
    void shouldMoveSameOperatorChildIntoParent2() {
        String in = "((A = B OR A = C) AND B = X) OR ((B = Y OR B = Z) AND C = D) OR (E = F OR E = G)";
        Expression e = ExpressionBuilder.build(in);
        assertEquals("((A = B OR A = C) AND B = X) OR ((B = Y OR B = Z) AND C = D) OR E = F OR E = G", e.getExpressionAsString());

        assertTrue(e instanceof Operation, "got wrong type: " + e.getClass().getName());
        Operation o = (Operation) e;
        assertEquals(Operation.Type.OR, o.getType());
        List<Expression> children = o.getElements();
        assertEquals(4, children.size());
    }

    @Test
    void shouldReduceSingleAssigments() {
        String in = "((AAA = XXX) OR (AAA = XXX AND (AAA = XXX OR AAA = XXX))) AND AAA = XXX";
        Expression e = ExpressionBuilder.build(in);
        assertEquals("AAA = XXX", e.getExpressionAsString());
    }

    @Test
    void shouldReduceSameExpressions() {
        String in = "((A = B OR A = C) AND (A = B OR A = C)) OR ((A = B OR A = C) AND (A = B OR A = C))";
        Expression e = ExpressionBuilder.build(in);
        assertEquals("A = B OR A = C", e.getExpressionAsString());
    }
}
