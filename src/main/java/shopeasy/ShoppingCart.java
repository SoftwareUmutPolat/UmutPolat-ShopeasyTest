package shopeasy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A mutable shopping cart that holds {@link CartItem}s.
 *
 * <p><strong>Contracts (Task 3 – Design by Contract):</strong>
 * <ul>
 *   <li>Students are required to add {@code assert} pre-/post-condition statements
 *       to {@link #addItem(Product, int)} and {@link #applyDiscount(double)} as
 *       part of Task 3. The Javadoc below describes the expected contracts.</li>
 * </ul>
 *
 * <p><strong>Invariant:</strong> {@link #total()} is always &gt;= 0 after any operation.
 */
public class ShoppingCart {

    private final List<CartItem> items = new ArrayList<>();

    /**
     * Adds a product to the cart. If the product is already present, the quantities
     * are combined into the existing cart line.
     *
     * <p><em>Pre-condition (Task 3):</em> {@code product != null}, {@code quantity > 0}<br>
     * <em>Post-condition (Task 3):</em> cart contains an entry for {@code product};
     * total number of distinct items in the cart >= previous count.
     *
     * @param product  the product to add (must not be null)
     * @param quantity number of units to add (must be > 0)
     */
    public void addItem(Product product, int quantity) {
        // Pre-conditions (Task 3)
        assert product != null : "addItem pre: product must not be null";
        assert quantity > 0    : "addItem pre: quantity must be > 0";

        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                assert total() >= 0 : "ShoppingCart invariant: total() must be >= 0";
                return;
            }
        }
        items.add(new CartItem(product, quantity));
        assert total() >= 0 : "ShoppingCart invariant: total() must be >= 0";
    }

    /**
     * Removes all units of the given product from the cart.
     * If the product is not in the cart, this method does nothing.
     *
     * @param productId the id of the product to remove
     */
    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    /**
     * Updates the quantity of an existing cart line.
     *
     * @param productId the id of the product whose quantity should change
     * @param quantity  the new quantity (must be > 0)
     * @throws IllegalArgumentException if the product is not in the cart
     */
    public void updateQuantity(String productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
        throw new IllegalArgumentException("Product not found in cart: " + productId);
    }

    /**
     * Applies a percentage discount to the current total and returns the discounted total.
     * The discount is applied <em>on top of</em> the raw subtotal; it does not persist
     * between calls (i.e., calling this method twice with 10% does not compound discounts).
     *
     * <p><em>Pre-condition (Task 3):</em> {@code 0 <= discountRate <= 100}<br>
     * <em>Post-condition (Task 3):</em> returned value &lt;= {@link #total()} when
     * {@code discountRate > 0}.
     *
     * @param discountRate percentage discount to apply, in [0, 100]
     * @return the total after applying the discount
     */
    public double applyDiscount(double discountRate) {
        // Pre-condition (Task 3)
        assert discountRate >= 0 && discountRate <= 100
                : "applyDiscount pre: 0 <= discountRate <= 100, got " + discountRate;

        double rawTotal = total();
        double discounted = rawTotal - (rawTotal * discountRate / 100);

        // Post-condition (Task 3)
        assert discounted >= 0 : "applyDiscount post: result must be >= 0";
        return discounted;
    }

    /**
     * Returns the sum of all cart-item subtotals, with no discounts applied.
     *
     * @return gross total (>= 0)
     */
    public double total() {
        double sum = 0;
        for (CartItem item : items) {
            sum += item.subtotal();
        }
        return sum;
    }

    /**
     * Returns the number of distinct product lines in the cart (not the total unit count).
     */
    public int itemCount() {
        return items.size();
    }

    /**
     * Returns an unmodifiable view of the cart items.
     */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Removes all items from the cart.
     */
    public void clear() {
        items.clear();
    }

    @Override
    public String toString() {
        return String.format("ShoppingCart{items=%d, total=%.2f}", items.size(), total());
    }
}
