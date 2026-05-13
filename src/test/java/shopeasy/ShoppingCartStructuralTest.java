package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Task 2 - Structural Testing and Code Coverage (Chapter 3)
 * Tests for ShoppingCart, written to reach >= 80% branch coverage.
 * Coverage was checked using JaCoCo after each test addition.
 */
class ShoppingCartStructuralTest {

    private static final double EPS = 1e-9;

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80,  50);
    }

    // Empty cart has zero items and zero total
    @Test
    void newCartIsEmpty() {
        assertThat(cart.itemCount()).isZero();
        assertThat(cart.total()).isEqualTo(0.0);
        assertThat(cart.getItems()).isEmpty();
    }

    // Branch: total() on empty cart returns 0
    @Test
    void totalOfEmptyCartIsZero() {
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // Branch: addItem with a new product adds a new line
    @Test
    void addItem_newProduct_addsNewLine() {
        cart.addItem(apple, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(4.50, within(EPS));
    }

    // Branch: addItem with same product merges quantity into existing line
    @Test
    void addItem_existingProduct_mergesQuantity() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 5);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(7);
        assertThat(cart.total()).isCloseTo(7 * 1.50, within(EPS));
    }

    // Branch: adding two different products creates two separate lines
    @Test
    void addItem_distinctProducts_addsSeparateLines() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 4);
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.total()).isCloseTo(2 * 1.50 + 4 * 0.80, within(EPS));
    }

    // Branch: removeItem when product exists
    @Test
    void removeItem_present_removesLine() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 1);
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo("P002");
    }

    // Branch: removeItem when product is not in cart, nothing changes
    @Test
    void removeItem_absent_isNoOp() {
        cart.addItem(apple, 2);
        cart.removeItem("P999");
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(3.0, within(EPS));
    }

    // Branch: updateQuantity with invalid (zero or negative) quantity throws
    @Test
    void updateQuantity_invalidQuantity_throws() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cart.updateQuantity("P001", -3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Branch: updateQuantity when product exists updates the quantity
    @Test
    void updateQuantity_existing_replacesQuantity() {
        cart.addItem(apple, 2);
        cart.updateQuantity("P001", 10);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(10);
        assertThat(cart.total()).isCloseTo(15.0, within(EPS));
    }

    // Branch: updateQuantity when product not found throws
    @Test
    void updateQuantity_unknownProduct_throws() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P999", 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("P999");
    }

    // Branch: applyDiscount with 0% returns the raw total
    @Test
    void applyDiscount_zeroRate_returnsRawTotal() {
        cart.addItem(apple, 4);
        assertThat(cart.applyDiscount(0.0)).isCloseTo(6.0, within(EPS));
    }

    // Partition: applyDiscount with a positive rate reduces the total
    @Test
    void applyDiscount_positiveRate_reducesTotal() {
        cart.addItem(apple, 10);
        double after = cart.applyDiscount(20.0);
        assertThat(after).isCloseTo(12.0, within(EPS));
    }

    // Boundary: applyDiscount at 100% makes total zero
    @Test
    void applyDiscount_hundredPercent_zerosTotal() {
        cart.addItem(apple, 2);
        assertThat(cart.applyDiscount(100.0)).isEqualTo(0.0);
    }

    // applyDiscount does not change the stored cart total
    @Test
    void applyDiscount_doesNotMutateCart() {
        cart.addItem(apple, 2);
        double before = cart.total();
        cart.applyDiscount(50.0);
        assertThat(cart.total()).isCloseTo(before, within(EPS));
    }

    // Branch: total() sums all items correctly
    @Test
    void totalSumsAllSubtotals() {
        cart.addItem(apple, 3);
        cart.addItem(banana, 5);
        assertThat(cart.total()).isCloseTo(8.50, within(EPS));
    }

    // getItems() returns an unmodifiable list
    @Test
    void getItemsReturnsUnmodifiableView() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // clear() removes all items from the cart
    @Test
    void clearEmptiesTheCart() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 1);
        cart.clear();
        assertThat(cart.itemCount()).isZero();
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // toString() works and includes item count
    @Test
    void toStringContainsItemCountAndTotal() {
        cart.addItem(apple, 2);
        String s = cart.toString();
        assertThat(s).contains("items=1").contains("3");
    }
}
