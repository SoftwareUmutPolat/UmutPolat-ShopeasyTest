package shopeasy;

/**
 * Stateless helper that computes a final price from a base price,
 * a discount rate, and a tax rate.
 *
 * <p>Formula:
 * <pre>
 *   discounted = basePrice * (1 - discountRate / 100)
 *   final      = discounted * (1 + taxRate / 100)
 * </pre>
 *
 * <p><strong>Contracts (Task 3 – Design by Contract):</strong>
 * Students are required to add {@code assert} statements for pre- and
 * post-conditions as part of Task 3. See the Javadoc on
 * {@link #calculate(double, double, double)} for the expected contracts.
 */
public class PriceCalculator {

    /**
     * Computes the final price after applying a discount and then tax.
     *
     * <p><em>Pre-conditions (Task 3):</em>
     * <ul>
     *   <li>{@code basePrice >= 0}</li>
     *   <li>{@code 0 <= discountRate <= 100}</li>
     *   <li>{@code 0 <= taxRate <= 100}</li>
     * </ul>
     *
     * <p><em>Post-condition (Task 3):</em> result >= 0
     *
     * @param basePrice    the original price before any adjustments (>= 0)
     * @param discountRate the discount percentage to apply, in [0, 100]
     * @param taxRate      the tax percentage to apply after discount, in [0, 100]
     * @return             the final price
     */
    public double calculate(double basePrice, double discountRate, double taxRate) {
        // Pre-conditions (Task 3)
        assert basePrice >= 0
                : "calculate pre: basePrice must be >= 0, got " + basePrice;
        assert discountRate >= 0 && discountRate <= 100
                : "calculate pre: 0 <= discountRate <= 100, got " + discountRate;
        assert taxRate >= 0 && taxRate <= 100
                : "calculate pre: 0 <= taxRate <= 100, got " + taxRate;

        double discounted = basePrice * (1.0 - discountRate / 100.0);
        double withTax    = discounted + (discounted * taxRate / 100.0);

        // Post-condition (Task 3)
        assert withTax >= 0 : "calculate post: result must be >= 0";
        return withTax;
    }

    /**
     * Convenience method: applies only a discount, no tax.
     *
     * @param basePrice    the original price (>= 0)
     * @param discountRate the discount percentage [0, 100]
     * @return             the price after discount
     */
    public double applyDiscountOnly(double basePrice, double discountRate) {
        return calculate(basePrice, discountRate, 0);
    }

    /**
     * Convenience method: applies only tax, no discount.
     *
     * @param basePrice the original price (>= 0)
     * @param taxRate   the tax percentage [0, 100]
     * @return          the price after tax
     */
    public double applyTaxOnly(double basePrice, double taxRate) {
        return calculate(basePrice, 0, taxRate);
    }
}
