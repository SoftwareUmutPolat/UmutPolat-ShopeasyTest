package shopeasy;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task 4 - Property-Based Testing (Chapter 5)
 * Tests for PriceCalculator and ShoppingCart using jqwik.
 * All inputs stay within valid ranges (pre-conditions from Task 3).
 */
class ShopEasyPropertyTest {

    private static final double PRICE_EPS = 1e-6;

    // Custom provider: creates valid Product objects with random values
    @Provide
    Arbitrary<Product> validProducts() {
        Arbitrary<String> ids    = Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(6).map(s -> "P-" + s);
        Arbitrary<String> names  = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        Arbitrary<Double> prices = Arbitraries.doubles().between(0.01, 10_000.0);
        Arbitrary<Integer> stock = Arbitraries.integers().between(0, 1_000);
        return Combinators.combine(ids, names, prices, stock).as(Product::new);
    }

    /*
     * Property 1: Monotonicity
     * Meaning: A higher discount should never give a higher price.
     * Bugs caught: wrong sign in the discount formula (e.g. 1 + d/100 instead of 1 - d/100).
     */
    @Property
    void discountIsMonotonicallyNonIncreasing(
            @ForAll @DoubleRange(min = 0.0, max = 1_000_000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double tax,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double discountA,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double discountB) {

        double low  = Math.min(discountA, discountB);
        double high = Math.max(discountA, discountB);

        PriceCalculator calc = new PriceCalculator();
        double priceWithLow  = calc.calculate(base, low,  tax);
        double priceWithHigh = calc.calculate(base, high, tax);

        assertThat(priceWithHigh).isLessThanOrEqualTo(priceWithLow + PRICE_EPS);
    }

    /*
     * Property 2: Identity
     * Meaning: 0% discount and 0% tax should return exactly the base price.
     * Bugs caught: accidental rounding or extra offsets applied in the no-op case.
     */
    @Property
    void zeroDiscountAndZeroTaxIsIdentity(
            @ForAll @DoubleRange(min = 0.0, max = 1_000_000.0) double base) {

        double result = new PriceCalculator().calculate(base, 0.0, 0.0);
        assertThat(result).isCloseTo(base, org.assertj.core.api.Assertions.within(PRICE_EPS));
    }

    /*
     * Property 3: Boundedness
     * Meaning: result is always between 0 and base*(1+maxTax/100).
     * Bugs caught: negative output from a sign error, or tax applied twice.
     */
    @Property
    void finalPriceIsBounded(
            @ForAll @DoubleRange(min = 0.0, max = 1_000_000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double discount,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double tax) {

        double result     = new PriceCalculator().calculate(base, discount, tax);
        double upperBound = base * (1.0 + tax / 100.0);

        assertThat(result).isGreaterThanOrEqualTo(0.0);
        assertThat(result).isLessThanOrEqualTo(upperBound + PRICE_EPS);
    }

    /*
     * Property 4: Cart commutativity
     * Meaning: adding product A then B gives the same total as adding B then A.
     * Bugs caught: insertion order affecting subtotals, or addItem updating wrong item.
     */
    @Property
    void cartIsCommutativeWhenAddingTwoProducts(
            @ForAll("validProducts") Product a,
            @ForAll("validProducts") Product b,
            @ForAll @IntRange(min = 1, max = 100) int qtyA,
            @ForAll @IntRange(min = 1, max = 100) int qtyB) {

        if (a.getId().equals(b.getId())) return;

        ShoppingCart cart1 = new ShoppingCart();
        cart1.addItem(a, qtyA);
        cart1.addItem(b, qtyB);

        ShoppingCart cart2 = new ShoppingCart();
        cart2.addItem(b, qtyB);
        cart2.addItem(a, qtyA);

        assertThat(cart2.total()).isCloseTo(cart1.total(), org.assertj.core.api.Assertions.within(PRICE_EPS));
        assertThat(cart2.itemCount()).isEqualTo(cart1.itemCount());
    }

    /*
     * Property 5: Convenience methods agree with calculate()
     * Meaning: applyDiscountOnly and applyTaxOnly should give same result as calculate().
     * Bugs caught: refactoring one path but forgetting the other.
     */
    @Property
    void convenienceMethodsAgreeWithCalculate(
            @ForAll @DoubleRange(min = 0.0, max = 1_000_000.0) double base,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double discount,
            @ForAll @DoubleRange(min = 0.0, max = 100.0)       double tax) {

        PriceCalculator calc = new PriceCalculator();
        assertThat(calc.applyDiscountOnly(base, discount))
                .isCloseTo(calc.calculate(base, discount, 0.0),
                        org.assertj.core.api.Assertions.within(PRICE_EPS));
        assertThat(calc.applyTaxOnly(base, tax))
                .isCloseTo(calc.calculate(base, 0.0, tax),
                        org.assertj.core.api.Assertions.within(PRICE_EPS));
    }

    /*
     * Property 6: Cart total equals sum of subtotals
     * Meaning: total() should always equal the sum of each item's subtotal.
     * Bugs caught: double-counting or skipping an item during iteration.
     */
    @Property
    void cartTotalEqualsSumOfSubtotals(
            @ForAll("validProducts") Product a,
            @ForAll("validProducts") Product b,
            @ForAll @IntRange(min = 1, max = 50) int qtyA,
            @ForAll @IntRange(min = 1, max = 50) int qtyB) {

        if (a.getId().equals(b.getId())) return;

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(a, qtyA);
        cart.addItem(b, qtyB);

        List<CartItem> items = cart.getItems();
        double expected = items.stream().mapToDouble(CartItem::subtotal).sum();
        assertThat(cart.total()).isCloseTo(expected, org.assertj.core.api.Assertions.within(PRICE_EPS));
    }
}
