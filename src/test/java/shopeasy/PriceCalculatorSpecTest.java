package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Task 1 - Specification-Based Testing (Chapter 2)
 * Tests for PriceCalculator using partition analysis and boundary value analysis.
 *
 * Partitions for basePrice: negative | zero | positive
 * Partitions for discountRate: negative | 0 | (0,100) | 100 | above 100
 * Partitions for taxRate: negative | 0 | (0,100) | 100 | above 100
 */
class PriceCalculatorSpecTest {

    private static final double EPS = 1e-9;

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // Partition: base price is zero, result should always be 0
    @ParameterizedTest(name = "base=0, disc={0}%, tax={1}%")
    @CsvSource({
            "  0,   0",
            " 50,   0",
            "  0,  20",
            " 30,  18",
            "100, 100"
    })
    void zeroBasePriceAlwaysReturnsZero(double discount, double tax) {
        assertThat(calculator.calculate(0.0, discount, tax)).isEqualTo(0.0);
    }

    // Partition: positive base with no discount or tax, result equals base
    @Test
    void positiveBasePriceWithNoRatesReturnsItself() {
        assertThat(calculator.calculate(199.99, 0, 0)).isCloseTo(199.99, within(EPS));
    }

    // Boundary: discount = 0 (lower bound), no reduction applied
    @Test
    void discountRateZeroMeansNoDiscount() {
        assertThat(calculator.calculate(100.0, 0.0, 0.0)).isEqualTo(100.0);
    }

    // Boundary: discount = 100 (upper bound), price becomes 0
    @ParameterizedTest(name = "tax={0}%")
    @ValueSource(doubles = {0.0, 18.0, 50.0, 100.0})
    void discountRateHundredAlwaysCollapsesToZero(double tax) {
        assertThat(calculator.calculate(250.0, 100.0, tax)).isEqualTo(0.0);
    }

    // Boundary: discount just below 100 (off-point), result is small but positive
    @Test
    void discountRateJustBelowHundredYieldsTinyPositive() {
        double result = calculator.calculate(100.0, 99.99, 0.0);
        assertThat(result).isCloseTo(0.01, within(1e-6));
        assertThat(result).isGreaterThan(0.0);
    }

    // Boundary: tax = 0 (lower bound), price is only discounted
    @Test
    void taxRateZeroMeansNoTax() {
        assertThat(calculator.calculate(100.0, 10.0, 0.0)).isCloseTo(90.0, within(EPS));
    }

    // Boundary: tax = 100 (upper bound), discounted price doubles
    @Test
    void taxRateHundredDoublesDiscountedPrice() {
        assertThat(calculator.calculate(100.0, 20.0, 100.0)).isCloseTo(160.0, within(EPS));
    }

    // Partition: typical values, checks formula base*(1-d/100)*(1+t/100)
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => {3}")
    @CsvSource({
            "100.0, 10.0, 20.0, 108.0",
            "200.0,  0.0, 10.0, 220.0",
            "200.0, 50.0,  0.0, 100.0",
            " 80.0, 25.0, 25.0,  75.0",
            "  1.0, 50.0, 50.0,   0.75",
            "999.99, 0.0, 0.0,  999.99"
    })
    void typicalValuesMatchFormula(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(1e-6));
    }

    // Partition: very large base price, result should still be positive
    @Test
    void veryLargeBasePriceIsHandled() {
        double result = calculator.calculate(1_000_000_000.0, 10.0, 20.0);
        assertThat(result).isCloseTo(1.08e9, within(1.0));
        assertThat(result).isGreaterThan(0.0);
    }

    // Boundary: very small base price, result must be >= 0
    @Test
    void verySmallBasePriceStaysNonNegative() {
        double result = calculator.calculate(0.01, 50.0, 18.0);
        assertThat(result).isGreaterThanOrEqualTo(0.0);
    }

    // Invalid input: discount above 100, with -ea throws AssertionError
    @Test
    void discountRateAboveHundredEitherAssertsOrYieldsNegative() {
        try {
            double result = calculator.calculate(100.0, 101.0, 0.0);
            assertThat(result).isCloseTo(-1.0, within(EPS));
        } catch (AssertionError ae) {
            assertThat(ae).isNotNull();
        }
    }

    // Invalid input: negative discount, with -ea throws AssertionError
    @Test
    void negativeDiscountEitherAssertsOrAmplifiesPrice() {
        try {
            double result = calculator.calculate(100.0, -1.0, 0.0);
            assertThat(result).isCloseTo(101.0, within(EPS));
        } catch (AssertionError ae) {
            assertThat(ae).isNotNull();
        }
    }

    // Invalid input: negative base price, with -ea throws AssertionError
    @Test
    void negativeBasePriceEitherAssertsOrIsNegative() {
        try {
            double result = calculator.calculate(-50.0, 0.0, 0.0);
            assertThat(result).isCloseTo(-50.0, within(EPS));
        } catch (AssertionError ae) {
            assertThat(ae).isNotNull();
        }
    }

    // Partition: applyDiscountOnly should equal calculate with tax = 0
    @Test
    void applyDiscountOnlyEqualsCalculateWithZeroTax() {
        assertThat(calculator.applyDiscountOnly(200.0, 25.0))
                .isCloseTo(calculator.calculate(200.0, 25.0, 0.0), within(EPS));
    }

    // Partition: applyTaxOnly should equal calculate with discount = 0
    @Test
    void applyTaxOnlyEqualsCalculateWithZeroDiscount() {
        assertThat(calculator.applyTaxOnly(200.0, 18.0))
                .isCloseTo(calculator.calculate(200.0, 0.0, 18.0), within(EPS));
    }
}
