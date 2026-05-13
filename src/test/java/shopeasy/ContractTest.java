package shopeasy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Task 3 - Design by Contract (Chapter 4)
 * Tests that assert statements in ShoppingCart and PriceCalculator
 * work correctly for both valid and invalid inputs.
 * Run with -ea flag (already configured in pom.xml).
 */
class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeAll
    static void assertionsAreEnabled() {
        boolean enabled = false;
        assert enabled = true;
        if (!enabled) {
            throw new IllegalStateException("Java assertions must be enabled (-ea) to run ContractTest.");
        }
    }

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // addItem: valid input should not throw
    @Test
    void addItem_validInput_passes() {
        assertThatCode(() -> cart.addItem(product, 3)).doesNotThrowAnyException();
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    // addItem pre-condition: null product should throw AssertionError
    @Test
    void addItem_nullProduct_throwsAssertionError() {
        assertThatThrownBy(() -> cart.addItem(null, 1))
                .isInstanceOf(AssertionError.class);
    }

    // addItem pre-condition: zero quantity should throw AssertionError
    @Test
    void addItem_zeroQuantity_throwsAssertionError() {
        assertThatThrownBy(() -> cart.addItem(product, 0))
                .isInstanceOf(AssertionError.class);
    }

    // addItem pre-condition: negative quantity should throw AssertionError
    @Test
    void addItem_negativeQuantity_throwsAssertionError() {
        assertThatThrownBy(() -> cart.addItem(product, -2))
                .isInstanceOf(AssertionError.class);
    }

    // addItem post-condition: size increases by one for a new product
    @Test
    void addItem_postCondition_sizeGrowsByOne() {
        cart.addItem(product, 1);
        assertThat(cart.itemCount()).isEqualTo(1);
        Product other = new Product("P002", "Other", 5.0, 10);
        cart.addItem(other, 1);
        assertThat(cart.itemCount()).isEqualTo(2);
    }

    // applyDiscount: valid rates (0 and 100) should not throw
    @Test
    void applyDiscount_validBounds_passes() {
        cart.addItem(product, 1);
        assertThatCode(() -> cart.applyDiscount(0.0)).doesNotThrowAnyException();
        assertThatCode(() -> cart.applyDiscount(100.0)).doesNotThrowAnyException();
        assertThatCode(() -> cart.applyDiscount(50.0)).doesNotThrowAnyException();
    }

    // applyDiscount pre-condition: negative rate should throw AssertionError
    @Test
    void applyDiscount_negativeRate_throwsAssertionError() {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(-0.01))
                .isInstanceOf(AssertionError.class);
    }

    // applyDiscount pre-condition: rate above 100 should throw AssertionError
    @Test
    void applyDiscount_rateAboveHundred_throwsAssertionError() {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(100.01))
                .isInstanceOf(AssertionError.class);
    }

    // applyDiscount post-condition: result is less than total when rate > 0
    @Test
    void applyDiscount_postCondition_resultLessThanTotal() {
        cart.addItem(product, 2);
        double after = cart.applyDiscount(25.0);
        assertThat(after).isLessThan(cart.total());
    }

    // Invariant: total() is always >= 0 after any operation
    @Test
    void cartInvariant_totalAlwaysNonNegative() {
        assertThat(cart.total()).isGreaterThanOrEqualTo(0.0);
        cart.addItem(product, 5);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0.0);
        cart.removeItem("P001");
        assertThat(cart.total()).isGreaterThanOrEqualTo(0.0);
        cart.clear();
        assertThat(cart.total()).isGreaterThanOrEqualTo(0.0);
    }

    // calculate: valid inputs should not throw
    @Test
    void calculate_validZeroes_passes() {
        assertThatCode(() -> calculator.calculate(0.0, 0.0, 0.0)).doesNotThrowAnyException();
    }

    // calculate: typical valid input should not throw
    @Test
    void calculate_typicalInput_passes() {
        assertThatCode(() -> calculator.calculate(100.0, 20.0, 18.0)).doesNotThrowAnyException();
    }

    // calculate pre-condition: negative base price should throw AssertionError
    @Test
    void calculate_negativeBase_throwsAssertionError() {
        assertThatThrownBy(() -> calculator.calculate(-1.0, 0.0, 0.0))
                .isInstanceOf(AssertionError.class);
    }

    // calculate pre-condition: discount above 100 should throw AssertionError
    @Test
    void calculate_discountAboveHundred_throwsAssertionError() {
        assertThatThrownBy(() -> calculator.calculate(100.0, 101.0, 0.0))
                .isInstanceOf(AssertionError.class);
    }

    // calculate pre-condition: negative discount should throw AssertionError
    @Test
    void calculate_negativeDiscount_throwsAssertionError() {
        assertThatThrownBy(() -> calculator.calculate(100.0, -5.0, 0.0))
                .isInstanceOf(AssertionError.class);
    }

    // calculate pre-condition: tax above 100 should throw AssertionError
    @Test
    void calculate_taxAboveHundred_throwsAssertionError() {
        assertThatThrownBy(() -> calculator.calculate(100.0, 0.0, 250.0))
                .isInstanceOf(AssertionError.class);
    }

    // calculate pre-condition: negative tax should throw AssertionError
    @Test
    void calculate_negativeTax_throwsAssertionError() {
        assertThatThrownBy(() -> calculator.calculate(100.0, 0.0, -1.0))
                .isInstanceOf(AssertionError.class);
    }

    // calculate post-condition: result is always >= 0 for valid inputs
    @Test
    void calculate_postCondition_resultNonNegative() {
        assertThat(calculator.calculate(50.0, 50.0, 50.0)).isGreaterThanOrEqualTo(0.0);
        assertThat(calculator.calculate(0.0, 100.0, 100.0)).isGreaterThanOrEqualTo(0.0);
    }
}
