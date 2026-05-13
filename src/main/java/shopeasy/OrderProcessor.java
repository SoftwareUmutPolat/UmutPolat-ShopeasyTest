package shopeasy;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the checkout flow for a {@link ShoppingCart}.
 *
 * <p>The processor depends on two external services:
 * <ul>
 *   <li>{@link InventoryService} — to verify that all requested items are in stock</li>
 *   <li>{@link PaymentGateway}   — to charge the customer</li>
 * </ul>
 *
 * <p>In tests (Task 5) both dependencies are replaced by Mockito mocks so the
 * processor's logic can be verified in complete isolation.
 *
 * <h3>Processing rules</h3>
 * <ol>
 *   <li>The cart must not be empty.</li>
 *   <li>Every cart item must pass the inventory check. If <em>any</em> item is
 *       unavailable, processing stops immediately and {@code null} is returned
 *       (no payment is attempted).</li>
 *   <li>The cart total is charged via the payment gateway. If the charge fails,
 *       {@code null} is returned.</li>
 *   <li>On success an {@link Order} is created and returned.</li>
 * </ol>
 */
public class OrderProcessor {

    private final InventoryService inventoryService;
    private final PaymentGateway   paymentGateway;

    /**
     * Constructs an OrderProcessor with the given dependencies.
     *
     * @param inventoryService the inventory checker (non-null)
     * @param paymentGateway   the payment processor (non-null)
     */
    public OrderProcessor(InventoryService inventoryService, PaymentGateway paymentGateway) {
        if (inventoryService == null) throw new IllegalArgumentException("inventoryService must not be null");
        if (paymentGateway   == null) throw new IllegalArgumentException("paymentGateway must not be null");
        this.inventoryService = inventoryService;
        this.paymentGateway   = paymentGateway;
    }

    /**
     * Processes a checkout for the given customer and cart.
     *
     * @param customerId the identifier of the customer placing the order (non-null, non-blank)
     * @param cart       the shopping cart to check out (non-null, must not be empty)
     * @return an {@link Order} on success, or {@code null} if inventory is
     *         insufficient or payment fails
     * @throws IllegalArgumentException if {@code customerId} is null/blank or
     *                                  {@code cart} is null or empty
     */
    public Order process(String customerId, ShoppingCart cart) {
        if (customerId == null || customerId.isBlank())
            throw new IllegalArgumentException("customerId must not be null or blank");
        if (cart == null)
            throw new IllegalArgumentException("cart must not be null");
        if (cart.itemCount() == 0)
            throw new IllegalArgumentException("cart must not be empty");

        // Step 1: check inventory for every cart line
        for (CartItem item : cart.getItems()) {
            if (!inventoryService.isAvailable(item.getProduct(), item.getQuantity())) {
                return null;   // one or more items unavailable — abort
            }
        }

        // Step 2: charge the customer
        double total = cart.total();
        boolean charged = paymentGateway.charge(customerId, total);
        if (!charged) {
            return null;       // payment declined — abort
        }

        // Step 3: create and return the order
        List<CartItem> snapshot = new ArrayList<>(cart.getItems());
        return new Order(customerId, snapshot, total);
    }
}
