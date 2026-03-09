package com.example;

import java.util.List;
import java.util.function.Predicate;

/**
 * Test class covering various Java complexity scenarios
 */
public class ComplexJavaClass {

    // Simple method
    public int simpleMethod(int x) {
        return x * 2;
    }

    // Low complexity
    public String lowComplexity(int value) {
        // Use signum-based indexing to eliminate conditional branching
        String[] labels = {"negative", "zero", "positive"};
        return labels[Integer.signum(value) + 1];
    }

    // Normal complexity
    public int normalComplexity(int x, int y) {
        if (x > 10) {                    // +1
            for (int i = 0; i < x; i++) { // +1
                if (i % 2 == 0) {         // +1
                    y += i;
                }
            }
        } else if (x < 0) {              // +1
            while (y > 0) {               // +1
                y--;
            }
        }
        return x + y;
    }

    // High complexity
    public boolean highComplexity(String input, int threshold) {
        if (input == null) {                           // +1
            return false;
        }

        for (char c : input.toCharArray()) {           // +1
            if (Character.isDigit(c)) {                // +1
                int digit = Character.getNumericValue(c);
                if (digit > threshold) {               // +1
                    return true;
                } else if (digit == threshold) {       // +1
                    threshold--;
                }
            } else if (Character.isLetter(c)) {        // +1
                if (Character.isUpperCase(c)) {        // +1
                    threshold++;
                } else {                               // +1
                    threshold--;
                }
            }
        }

        try {                                          // +1
            if (threshold < 0) {                       // +1
                throw new IllegalStateException("Invalid threshold");
            }
        } catch (Exception e) {                        // +1
            return false;
        }

        return true;
    }

    // Extreme complexity
    public Object extremeComplexity(List<String> items, int mode, boolean strict) {
        if (items == null || items.isEmpty()) {        // +1 (|| counts as 1)
            return null;
        }

        int result = 0;
        for (String item : items) {                    // +1
            if (item == null) {                        // +1
                continue;
            }

            switch (mode) {                            // +1 (each case is +1)
                case 1:
                    if (item.length() > 5) {           // +1
                        result++;
                    } else if (item.length() > 3) {    // +1
                        result += 2;
                    }
                    break;
                case 2:                                // +1
                    for (int i = 0; i < item.length(); i++) { // +1
                        if (Character.isDigit(item.charAt(i))) {  // +1
                            result += i;
                        }
                    }
                    break;
                case 3:                                // +1
                    try {                              // +1
                        int val = Integer.parseInt(item);
                        if (val > 100) {               // +1
                            result += val;
                        } else if (val > 50) {         // +1
                            result += val / 2;
                        } else if (val > 0) {          // +1
                            result += val / 4;
                        }
                    } catch (NumberFormatException e) { // +1
                        if (strict) {                  // +1
                            throw new RuntimeException("Invalid number", e);
                        }
                    }
                    break;
                default:                               // +1
                    break;
            }

            if (strict && result > 1000) {             // +1 (&& counts as +1)
                for (int i = 0; i < result; i++) {     // +1
                    if (i % 10 == 0) {                 // +1
                        result--;
                    } else if (i % 5 == 0) {           // +1
                        result++;
                    }
                }
            }
        }

        return result > 0 ? result : null;             // +1 (ternary)
    }

    // Lambda expressions test
    public Predicate<Integer> createLambda(int threshold) {
        // Lambda complexity depends on settings
        return x -> x > threshold && x < threshold * 2;  // May count as +1 if enabled
    }

    // Anonymous class test
    public Runnable createAnonymousClass(String message) {
        return new Runnable() {                        // Anonymous class
            @Override
            public void run() {
                if (message != null) {                 // +1
                    System.out.println(message);
                }
            }
        };
    }

    // Nested conditions
    public void nestedConditions(int a, int b, int c) {
        if (a > 0) {                                   // +1
            if (b > 0) {                               // +1
                if (c > 0) {                           // +1
                    System.out.println("All positive");
                } else if (c < 0) {                    // +1
                    System.out.println("c is negative");
                }
            } else if (b < 0) {                        // +1
                if (c > 0) {                           // +1
                    System.out.println("b negative, c positive");
                }
            }
        } else if (a < 0) {                            // +1
            if (b > 0) {                               // +1
                if (c > 0) {                           // +1
                    System.out.println("a negative, b and c positive");
                } else if (c < 0) {                    // +1
                    System.out.println("a and c negative");
                }
            } else if (b < 0) {                        // +1
                if (c > 0) {                           // +1
                    System.out.println("a and b negative");
                } else if (c < 0) {                    // +1
                    System.out.println("All negative");
                } else {                               // +1
                    System.out.println("c is zero");
                }
            }
        }
    }

    // Do-while loop test
    public int doWhileTest(int n) {
        int sum = 0;
        int i = 0;
        do {                                           // +1
            if (i % 2 == 0) {                          // +1
                sum += i;
            } else if (i % 3 == 0) {                   // +1
                sum -= i;
            }
            i++;
        } while (i < n);                               // Already counted

        return sum;
    }
}
