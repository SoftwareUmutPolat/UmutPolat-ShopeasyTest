package shopeasy;

/**
 * A single line in a {@link ShoppingCart}: a product and the quantity requested.
 */
public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        if (product == null)  throw new IllegalArgumentException("Product must not be null");
        if (quantity <= 0)    throw new IllegalArgumentException("Quantity must be > 0");
        this.product  = product;
        this.quantity = quantity;
    }

    public Product getProduct()  { return product; }
    public int getQuantity()     { return quantity; }

    /** Replace the quantity for this cart line. */
    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        this.quantity = quantity;
    }

    /** Subtotal for this cart line (unit price × quantity). */
    public double subtotal() {
        return product.getUnitPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("CartItem{product=%s, qty=%d, subtotal=%.2f}",
                product.getName(), quantity, subtotal());
    }
}
