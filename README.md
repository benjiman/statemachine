# State machines

State machines in Java.

Enables both type checked and runtime checked transitions

Typechecked, if we try to transition straight from green to red it will fail to compile

```java
sealed interface TrafficLight
        extends State<TrafficLight>
        permits Green, SolidAmber, FlashingAmber, Red {}
static final class Green implements TrafficLight, TransitionTo<SolidAmber> {}
static final class SolidAmber implements TrafficLight, TransitionTo<Red> {}
static final class Red implements TrafficLight, TransitionTo<FlashingAmber> {}
static final class FlashingAmber implements TrafficLight, TransitionTo<Green> {}

@Test
public void traffic_light_typechecked_example() {
    Green signal = new Green();
    // Comment out a transition and it will fail to compile.
    signal = signal
        .transition(SolidAmber::new)
        .transition(Red::new)
        .transition(FlashingAmber::new)
        .transition(Green::new);
}
```

We can still have typechecked transitions even where multiple state transitions are possible

```java
static final class Pending implements OrderStatus, BiTransitionTo<CheckingOut, Cancelled> {}

pending.transition(CheckingOut::new); // fine
pending.transition(Cancelled::new);   // fine
pending.transition(Refunded::new);   // Compile Error

```

When using the runtime checked transitions we'd throw an exception if we can't transition

```java
@Test
public void runtime_transitions_possible() {
   TrafficLight light = new Green();
   light = light
       .tryTransition(SolidAmber::new)
       .unchecked();

   assertTrue(light instanceof SolidAmber);
}
@Test(expected = State.InvalidStateTransitionException.class)
public void runtime_transitions_throw_exception_when_not_possible() {
    TrafficLight light = new Green();
    light = light
            .tryTransition(Red::new)
            .unchecked();
}

```

We can add behaviour on states so that we can perform an appropriate action for the state we're in

```java
@Test
public void behaviour_on_states() throws OhNoes {
    Customer customer = customer("spam@example.com");

    OrderStatus state = new Pending();
    state.notifyProgress(customer, emailSender);

    verifyZeroInteractions(emailSender);

    state = state
            .tryTransition(CheckingOut::new)
            .orElseThrow(OhNoes::new)
            .transition(Purchased::new);

    state.notifyProgress(customer, emailSender);

    verify(emailSender).sendEmail(customer.email(), "Your order is on its way");
    verify(emailSender).sendEmail("fulfillment@mycompany.com", "Customer order pending");
}

sealed interface OrderStatus
        extends State<OrderStatus>
        permits Pending, CheckingOut, Purchased, Shipped, Cancelled, Failed, Refunded {
    default void notifyProgress(Customer customer, EmailSender sender) {}
}
static final class Purchased implements OrderStatus, BiTransitionTo<Shipped, Failed> {
    public void notifyProgress(Customer customer, EmailSender emailSender) {
        emailSender.sendEmail("fulfillment@mycompany.com", "Customer order pending");
        emailSender.sendEmail(customer.email(), "Your order is on its way");
    }
}
static final class Cancelled implements OrderStatus {
    public void notifyProgress(Customer customer, EmailSender emailSender) {
        emailSender.sendEmail("fulfillment@mycompany.com", "Customer order cancelled");
        emailSender.sendEmail(customer.email(), "Your order has been cancelled");
    }
}

```

We can also have guards before and after transitions. Our states can be non-static classes if we want to access state in the enclosing class, like this logger.

```java
Logger failureLog = Logger.getLogger("failures");
final class Failed implements OrderStatus {
    @Override
    public void afterTransition(OrderStatus from) {
        failureLog.warning("Oh bother! failed from " + from.getClass().getSimpleName());
    }
}
```
