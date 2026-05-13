package shopeasy;

/**
 * Gateway for processing payments.
 *
 * <p>In production this would integrate with a payment provider (e.g., Stripe).
 * In tests (Task 5) it is replaced by a Mockito mock so that
 * {@link OrderProcessor} can be tested without real money moving.
 */
public interface PaymentGateway {

    /**
     * Attempts to charge a customer the given amount.
     *
     * @param customerId the unique identifier of the customer being charged (non-null)
     * @param amount     the amount to charge in the store's currency (> 0)
     * @return {@code true} if the charge was successful, {@code false} otherwise
     */
    boolean charge(String customerId, double amount);
}
