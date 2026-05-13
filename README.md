# ShopEasy – Term Project Starter

**Course:** Software Testing  
**Textbook:** *Effective Software Testing* – Aniche  
**Chapters covered:** 1–6

---

## Quick Start

```bash
# 1. Clone the repository
git clone <your-fork-url>
cd shopeasy-tests

# 2. Compile and run all tests
mvn test

# 3. Generate JaCoCo coverage report (Task 2)
#    Open: target/site/jacoco/index.html
mvn test   # JaCoCo report is generated automatically after tests

# 4. Run PIT mutation testing (Task 2 – bonus)
mvn org.pitest:pitest-maven:mutationCoverage
#    Open: target/pit-reports/<timestamp>/index.html
```

## Project Structure

```
src/
├── main/java/shopeasy/       ← Production code (DO NOT modify, except Task 3 classes)
│   ├── Product.java
│   ├── CartItem.java
│   ├── ShoppingCart.java     ← Task 3: add assert statements here
│   ├── PriceCalculator.java  ← Task 3: add assert statements here
│   ├── InventoryService.java
│   ├── PaymentGateway.java
│   ├── Order.java
│   └── OrderProcessor.java
└── test/java/shopeasy/       ← Your test code goes here
    ├── PriceCalculatorSpecTest.java    (Task 1)
    ├── ShoppingCartStructuralTest.java (Task 2)
    ├── ContractTest.java               (Task 3)
    ├── ShopEasyPropertyTest.java       (Task 4)
    └── OrderProcessorMockTest.java     (Task 5)

report/
├── reflection.pdf            ← Task 6 (add before submission)
└── jacoco-screenshot.png     ← Task 2 (add before submission)
```

## Task Overview

| Task | Technique | Chapter | Weight |
|------|-----------|---------|--------|
| 1 | Specification-Based Testing | Ch. 2 | 20 pts |
| 2 | Structural Testing & Code Coverage | Ch. 3 | 20 pts |
| 3 | Design by Contract | Ch. 4 | 15 pts |
| 4 | Property-Based Testing | Ch. 5 | 20 pts |
| 5 | Mocks & Stubs | Ch. 6 | 20 pts |
| 6 | Reflection Report | Ch. 1–6 | 5 pts  |

See the project assignment document for full task descriptions and grading criteria.

## Dependencies

| Library | Purpose | Version |
|---------|---------|---------|
| JUnit 5 | Test runner | 5.10.2 |
| jqwik | Property-based testing (Task 4) | 1.8.3 |
| Mockito | Mocking (Task 5) | 5.11.0 |
| AssertJ | Fluent assertions | 3.25.3 |
| JaCoCo | Code coverage (Task 2) | 0.8.12 |
| PIT | Mutation testing (Task 2 bonus) | 1.15.8 |

## Submission Checklist

- [ ] All 5 test files contain meaningful, runnable tests
- [ ] `mvn test` passes with no failures
- [ ] JaCoCo screenshot saved to `report/jacoco-screenshot.png`
- [ ] Reflection report saved to `report/reflection.pdf`
- [ ] `AUTHORS.md` filled in
- [ ] Git history has at least one commit per task
- [ ] No IDE files committed (`.idea/`, `*.iml`)
