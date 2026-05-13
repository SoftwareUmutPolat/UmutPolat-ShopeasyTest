package shopeasy;

/**
 * Represents a product available in the ShopEasy catalogue.
 *
 * <p>A Product is immutable after creation. Stock quantity is managed
 * externally by {@link InventoryService}.
 */
public class Product {

    private final String id;
    private final String name;
    private final double unitPrice;
    private final int stockQuantity;

    /**
     * Creates a new Product.
     *
     * @param id            unique product identifier (non-null, non-empty)
     * @param name          human-readable product name (non-null, non-empty)
     * @param unitPrice     price per unit in the store's currency (>= 0)
     * @param stockQuantity number of units currently in stock (>= 0)
     */
    public Product(String id, String name, double unitPrice, int stockQuantity) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Product id must not be null or blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Product name must not be null or blank");
        if (unitPrice < 0)
            throw new IllegalArgumentException("Unit price must be >= 0");
        if (stockQuantity < 0)
            throw new IllegalArgumentException("Stock quantity must be >= 0");

        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
    }

    public String getId()            { return id; }
    public String getName()          { return name; }
    public double getUnitPrice()     { return unitPrice; }
    public int getStockQuantity()    { return stockQuantity; }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, stock=%d}",
                id, name, unitPrice, stockQuantity);
    }
}
