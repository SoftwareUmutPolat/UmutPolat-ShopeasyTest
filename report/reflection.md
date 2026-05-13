# ShopEasy — Reflection Report

**Course:** SE3004 Software Testing — Term Project
**Source under test:** `shopeasy` (Product, ShoppingCart, PriceCalculator, OrderProcessor, …)
**Test count:** 79 JUnit 5 tests + 6 jqwik properties (≈ 6 000 generated checks per run)
**Final coverage (JaCoCo):** ShoppingCart **88 % branch / 93 % instruction**, PriceCalculator **91 % branch / 94 % instruction**, OrderProcessor **88 % branch / 89 % instruction**.

---

## 1. Which bugs did each technique reveal?

The starter library is small and reasonably correct, so most "bugs" found are
better described as **missing or unenforced contracts**. Concrete observations:

* **Specification-based testing (Task 1).** Building the partition table for
  `PriceCalculator.calculate` immediately exposed the fact that the method
  silently accepts illegal inputs: a negative `basePrice`, a `discountRate`
  above 100, or a negative tax produce nonsensical (negative) results without
  any exception. The Javadoc claimed pre-conditions, but the code never
  enforced them. This is a latent bug — any caller that forwards bad user
  input (e.g., a malformed coupon code) would propagate silently broken
  prices.
* **Structural testing (Task 2).** The first JaCoCo run highlighted two
  branches in `updateQuantity` that are hard to spot from the spec alone:
  the `quantity <= 0` early-throw, and the "loop completes without finding
  the product" path. Without explicit tests the second one is only ever
  reached on a real-world typo. Adding tests for them did not surface a
  bug, but writing them is what made me notice that `removeItem` has the
  exact same "absent product" case yet handles it as a silent no-op rather
  than throwing — an inconsistency in the API surface that is worth raising
  in code review.
* **Design by Contract (Task 3).** Adding `assert` statements made the
  Task 1 observations executable: a `calculate(-50, 0, 0)` call now fails
  loudly with `AssertionError` instead of returning `-50`. Also revealed
  by the post-condition `discounted >= 0`: if anyone in the future flips a
  sign or compounds discount + tax incorrectly, the assertion catches it
  inside the method instead of surfacing as wrong UI prices.
* **Property-based testing (Task 4).** None of the six properties found a
  failing case (jqwik ran ~6 000 examples). The properties did, however,
  validate non-trivial invariants the unit tests only checked at single
  points — e.g., `discountIsMonotonicallyNonIncreasing` randomizes 4
  parameters and asserts ordering for every pair, which a `@CsvSource`
  cannot. Had the author written `1 + d/100` by accident, this would have
  been the test that caught it.
* **Mocks & stubs (Task 5).** The big behavioural insight is that
  `OrderProcessor.process` returns `null` — not throws — when inventory or
  payment fails. Mockito's `verify(paymentGateway, never()).charge(...)`
  documents the safety-critical guarantee that an unavailable item cannot
  result in a charged customer, and the partial-quantity test confirms the
  short-circuit. Without mocks I could not have asserted "charge was never
  called" — an integration test only sees that no money moved, which is a
  weaker claim.

## 2. Most effective technique per unit of effort

Of the five techniques the **Mocks & Stubs** approach gave the best
defect-prevention-per-line-of-test for this codebase. `OrderProcessor` is
where the business risk concentrates (a misplaced `if` could lead to
charging without delivery), and writing eight Mockito-driven scenarios
took roughly the same time as writing fifteen `PriceCalculator` parameter
rows, yet the consequences of an undetected bug are dramatically larger.

A close second is **property-based testing**: 30 minutes of work plus a
custom `@Provide` produced ~6 000 randomized checks per build. The cost
is dominated by *thinking* about the invariant, not typing — and once the
invariant is named, jqwik does the rest forever.

Specification-based testing was the most thorough but also the most
labour-intensive: writing partition tables and tabulating boundary points
takes time that scales with the number of input dimensions.

## 3. Where this suite sits on the testing pyramid

By Aniche's chapter 1 taxonomy this suite is **almost entirely the unit
layer**. Nothing exercises a real database, a real network, or even
multiple processes running together. The pyramid's missing layers, in
order of priority:

* **Integration tests** — wire `OrderProcessor` to a real (in-memory)
  inventory store and a fake payment HTTP server, to verify that the
  contract assumed by the mocks holds for at least one real implementation.
* **End-to-end / system tests** — drive the library through a thin web
  facade and assert externally observable behaviour (idempotency of
  retries, transactional rollback if the payment provider returns 500
  after a debit, etc.).
* **Concurrency tests** — `ShoppingCart` uses an `ArrayList` and is not
  thread-safe; a stress test would document that quickly.

## 4. If I had one more week

I would invest the time in three places:

1. **PIT mutation testing.** I configured the plugin in `pom.xml` but did
   not run the bonus analysis end-to-end. Mutation score is the single
   most honest measurement of "are the tests actually checking
   anything?" — surviving mutants in `applyDiscount` and the
   `OrderProcessor.process` short-circuit are the most likely first finds.
2. **Concurrency / state tests for `ShoppingCart`.** Two threads adding
   the same product simultaneously will currently produce a duplicated
   line because the find-and-merge loop is not atomic. A `jqwik`-driven
   randomized concurrency property (or hand-rolled `CountDownLatch`
   harness) would expose this.
3. **A first integration test for `OrderProcessor`** against real
   in-memory implementations of `InventoryService` and `PaymentGateway`.
   The mocks tell me how the orchestrator *ought* to call its
   collaborators; the integration test verifies that at least one
   collaborator implementation actually behaves the way the mock pretends
   it does. That gap — between "what we mocked" and "what the real
   service returns" — is exactly where production bugs hide.

## Mocking trade-off (Task 5 reflection question)

Mocks let me make assertions a real implementation cannot: *that a
specific collaborator call did or did not happen*. They let `OrderProcessor`
be tested in isolation, in milliseconds, with deterministic inputs.

What they cost is fidelity. A mock that says
`when(paymentGateway.charge(...)).thenReturn(true)` is only as accurate
as my mental model of the real gateway. If the real gateway can throw
`PaymentTimeoutException`, my mock-based suite will never see it. The
defensible position is a layered strategy: mocks for the orchestrator's
control flow, real (or fake) implementations behind an integration test
for end-to-end faithfulness.
