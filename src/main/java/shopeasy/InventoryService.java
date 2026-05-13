package shopeasy;

/**
 * Service responsible for checking product availability in the warehouse.
 *
 * <p>In production this would call a real inventory database.
 * In tests (Task 5) it is replaced by a Mockito mock so that
 * {@link OrderProcessor} can be tested in complete isolation.
 */
public interface InventoryService {

    /**
     * Checks whether the requested quantity of a product is currently available.
     *
     * @param product  the product to check (non-null)
     * @param quantity the number of units needed (> 0)
     * @return {@code true} if the warehouse has at least {@code quantity} units
     */
    boolean isAvailable(Product product, int quantity);
}
