package de.ruderphilipp.variance;

/**
 * A <em>variance expression</em> allows to configure based on values or a combination of multiple values.
 * <p>
 * It is necessary for describing a configurable bill of material (BoM).
 * <p>
 * Let's take a pizza as an example. A "Salami pizza" is the result of the choice of
 * <tt>base = tomato ketchup AND topping = salami and cheese = mozarella</tt>. This string is a
 * <em>variance expression</em> and describes criteria on which a result is choosen.
 * <p>
 * Another example - let's stay with the pizza case - is if someone orders a pizza but dislikes one ingredient. In this
 * case, the chef can look into the shelves and offer alternatives. Let's say the customer does not like
 * <tt>tomato ketchup</tt>, but there might be also the options <tt>nothing</tt> or <tt>sauce hollandaise</tt> to choose
 * from. This would lead to a different <em>variance expression</em> and thus might lead to a different name of the
 * pizza.
 */
public interface Expression {
    String getExpressionAsString();
}
