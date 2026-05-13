package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Task 5 - Mocks and Stubs (Chapter 6)
 * Tests for OrderProcessor using Mockito to mock InventoryService and PaymentGateway.
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        gadget = new Product("P002", "Gadget", 40.0,  10);
    }

    // Happy path: inventory available and payment succeeds, returns an Order
    @Test
    void process_inventoryOkAndPaymentOk_returnsOrder() {
        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getOrderId()).isNotBlank();

        verify(inventoryService, times(1)).isAvailable(widget, 2);
        verify(paymentGateway, times(1)).charge("customer-1", 50.0);
    }

    // Inventory failure: isAvailable returns false, payment must never be called
    @Test
    void process_inventoryUnavailable_returnsNullAndDoesNotCharge() {
        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(inventoryService).isAvailable(widget, 2);
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // Payment failure: inventory ok but charge returns false, returns null
    @Test
    void process_paymentDeclined_returnsNull() {
        cart.addItem(widget, 1);

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 25.0)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(paymentGateway).charge("customer-1", 25.0);
    }

    // Partial quantity: second item not available, whole order rejected, no charge
    @Test
    void process_secondLineUnavailable_abortsBeforeCharging() {
        cart.addItem(widget, 2);
        cart.addItem(gadget, 5);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 5)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(inventoryService).isAvailable(widget, 2);
        verify(inventoryService).isAvailable(gadget, 5);
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // Partial quantity variant: first item unavailable, second item is never checked
    @Test
    void process_firstLineUnavailable_shortCircuits() {
        cart.addItem(widget, 2);
        cart.addItem(gadget, 1);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(inventoryService).isAvailable(widget, 2);
        verify(inventoryService, never()).isAvailable(eq(gadget), anyInt());
        verifyNoInteractions(paymentGateway);
    }

    // Validation: null customerId throws before touching any collaborator
    @Test
    void process_nullCustomerId_throwsAndDoesNothing() {
        cart.addItem(widget, 1);
        assertThatThrownBy(() -> orderProcessor.process(null, cart))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }

    // Validation: blank customerId throws before touching any collaborator
    @Test
    void process_blankCustomerId_throwsAndDoesNothing() {
        cart.addItem(widget, 1);
        assertThatThrownBy(() -> orderProcessor.process("  ", cart))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }

    // Validation: empty cart throws before touching any collaborator
    @Test
    void process_emptyCart_throwsAndDoesNothing() {
        assertThatThrownBy(() -> orderProcessor.process("customer-1", cart))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }

    // Validation: null cart throws
    @Test
    void process_nullCart_throws() {
        assertThatThrownBy(() -> orderProcessor.process("customer-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(inventoryService);
        verifyNoInteractions(paymentGateway);
    }

    // Charge is called with the exact sum of all cart items
    @Test
    void process_chargesExactCartTotal_acrossMultipleLines() {
        cart.addItem(widget, 3);
        cart.addItem(gadget, 2);

        when(inventoryService.isAvailable(any(Product.class), anyInt())).thenReturn(true);
        when(paymentGateway.charge(eq("customer-9"), eq(155.0))).thenReturn(true);

        Order order = orderProcessor.process("customer-9", cart);

        assertThat(order).isNotNull();
        assertThat(order.getTotal()).isEqualTo(155.0);
        verify(paymentGateway).charge("customer-9", 155.0);
    }
}
