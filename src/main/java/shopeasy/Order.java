package shopeasy;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Immutable record of a successfully placed order.
 *
 * <p>Orders are created exclusively by {@link OrderProcessor}; client code
 * should never construct {@code Order} objects directly.
 */
public class Order {

    private final String orderId;
    private final String customerId;
    private final List<CartItem> items;
    private final double total;

    /**
     * Creates a new Order (package-private — only OrderProcessor should call this).
     */
    Order(String customerId, List<CartItem> items, double total) {
        this.orderId    = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items      = Collections.unmodifiableList(items);
        this.total      = total;
    }

    public String getOrderId()       { return orderId; }
    public String getCustomerId()    { return customerId; }
    public List<CartItem> getItems() { return items; }
    public double getTotal()         { return total; }

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', items=%d, total=%.2f}",
                orderId, customerId, items.size(), total);
    }
}
