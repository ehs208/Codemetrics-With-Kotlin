package com.example

/**
 * Test class covering various Kotlin complexity scenarios
 */
class ComplexKotlinClass {

    // Simple function
    fun simpleFunction(x: Int): Int = x * 2

    // Low complexity
    fun lowComplexity(value: Int): String {
        return when {                      // +1
            value > 0 -> "positive"        // +1
            value < 0 -> "negative"        // +1
            else -> "zero"
        }
    }

    // When expression test
    fun whenExpressionTest(input: Any): String {
        return when (input) {              // +1
            is String -> {                 // +1
                if (input.length > 5) {    // +1
                    "long string"
                } else {
                    "short string"
                }
            }
            is Int -> "integer"            // +1
            is Double -> "double"          // +1
            else -> "unknown"
        }
    }

    // If expression test
    fun ifExpressionTest(x: Int, y: Int): Int {
        val max = if (x > y) {             // +1
            if (x > 100) {                 // +1
                x * 2
            } else {
                x
            }
        } else {
            if (y > 100) {                 // +1
                y * 2
            } else {
                y
            }
        }
        return max
    }

    // For loop test
    fun forLoopTest(items: List<Int>): Int {
        var sum = 0
        for (item in items) {              // +1
            if (item > 0) {                // +1
                sum += item
            } else if (item < 0) {         // +1
                sum -= item
            }
        }

        for (i in 0..10) {                 // +1
            if (i % 2 == 0) {              // +1
                sum += i
            }
        }

        return sum
    }

    // While loop test
    fun whileLoopTest(n: Int): Int {
        var count = 0
        var i = 0
        while (i < n) {                    // +1
            if (i % 2 == 0) {              // +1
                count++
            } else if (i % 3 == 0) {       // +1
                count += 2
            }
            i++
        }
        return count
    }

    // Try-catch test
    fun tryCatchTest(input: String): Int? {
        return try {                       // +1
            val value = input.toInt()
            if (value > 0) {               // +1
                value
            } else {
                null
            }
        } catch (e: NumberFormatException) { // +1
            if (input.isEmpty()) {         // +1
                0
            } else {
                null
            }
        }
    }

    // Elvis operator test (depends on settings)
    fun elvisOperatorTest(input: String?): String {
        val result = input ?: "default"    // +1 if Elvis counting is enabled
        return if (result.isNotEmpty()) {  // +1
            result
        } else {
            "empty"
        }
    }

    // Property with getter - Complexity varies based on settings
    var complexProperty: Int = 0
        get() {
            return if (field > 0) {        // +1 if property metrics enabled
                field * 2
            } else {
                0
            }
        }
        set(value) {
            field = if (value > 0) {       // +1 if property metrics enabled
                value
            } else {
                0
            }
        }

    // Lambda expression test
    fun createLambda(threshold: Int): (Int) -> Boolean {
        // Lambda complexity depends on settings
        return { x -> x > threshold && x < threshold * 2 }  // May count if enabled
    }

    // High complexity
    fun highComplexity(items: List<String>?, mode: Int, strict: Boolean): Any? {
        if (items.isNullOrEmpty()) {       // +1
            return null
        }

        var result = 0
        for (item in items) {              // +1
            when (mode) {                  // +1
                1 -> {                     // +1
                    if (item.length > 5) { // +1
                        result++
                    } else if (item.length > 3) { // +1
                        result += 2
                    }
                }
                2 -> {                     // +1
                    for (c in item) {      // +1
                        if (c.isDigit()) { // +1
                            result += c.digitToInt()
                        }
                    }
                }
                3 -> {                     // +1
                    try {                  // +1
                        val value = item.toInt()
                        when {             // +1
                            value > 100 -> result += value         // +1
                            value > 50 -> result += value / 2      // +1
                            value > 0 -> result += value / 4       // +1
                        }
                    } catch (e: NumberFormatException) {  // +1
                        if (strict) {      // +1
                            throw RuntimeException("Invalid number", e)
                        }
                    }
                }
            }
        }

        return if (result > 0) result else null  // +1 (if expression)
    }

    // Extreme complexity with nested control flow
    fun extremeComplexity(
        data: Map<String, List<Int>>?,
        threshold: Int,
        mode: String
    ): Map<String, Int> {
        val result = mutableMapOf<String, Int>()

        if (data == null) {                           // +1
            return emptyMap()
        }

        for ((key, values) in data) {                 // +1
            if (key.isEmpty()) {                      // +1
                continue
            }

            when (mode) {                             // +1
                "sum" -> {                            // +1
                    var sum = 0
                    for (value in values) {           // +1
                        if (value > threshold) {      // +1
                            sum += value
                        } else if (value < 0) {       // +1
                            sum -= value
                        }
                    }
                    result[key] = sum
                }
                "max" -> {                            // +1
                    val max = values.maxOrNull()
                    if (max != null) {                // +1
                        if (max > threshold) {        // +1
                            result[key] = max
                        } else if (max > threshold / 2) { // +1
                            result[key] = max / 2
                        }
                    }
                }
                "filter" -> {                         // +1
                    val filtered = mutableListOf<Int>()
                    for (value in values) {           // +1
                        when {                        // +1
                            value > threshold * 2 -> { // +1
                                if (value % 2 == 0) { // +1
                                    filtered.add(value)
                                }
                            }
                            value > threshold -> {    // +1
                                if (value % 3 == 0) { // +1
                                    filtered.add(value)
                                }
                            }
                            value > 0 -> {            // +1
                                filtered.add(value)
                            }
                        }
                    }
                    result[key] = filtered.size
                }
                "complex" -> {                        // +1
                    var count = 0
                    for (i in values.indices) {       // +1
                        val value = values[i]
                        if (value > threshold) {      // +1
                            for (j in i + 1 until values.size) { // +1
                                if (values[j] > threshold) {     // +1
                                    count++
                                } else if (values[j] < 0) {      // +1
                                    count--
                                }
                            }
                        } else if (value < 0) {       // +1
                            count -= 10
                        }
                    }
                    result[key] = count
                }
            }
        }

        return result
    }

    // Sealed class with when - good complexity test
    sealed class Result {
        data class Success(val value: Int) : Result()
        data class Error(val message: String) : Result()
        object Loading : Result()
    }

    fun processResult(result: Result): String {
        return when (result) {                        // +1
            is Result.Success -> {                    // +1
                if (result.value > 100) {             // +1
                    "High value: ${result.value}"
                } else if (result.value > 0) {        // +1
                    "Low value: ${result.value}"
                } else {
                    "Zero or negative"
                }
            }
            is Result.Error -> {                      // +1
                if (result.message.isEmpty()) {       // +1
                    "Unknown error"
                } else {
                    "Error: ${result.message}"
                }
            }
            Result.Loading -> "Loading..."
        }
    }

    // Scope functions test (let, also, apply, run, with)
    fun scopeFunctionsTest(input: String?): String {
        return input?.let {                           // +1 (?.let)
            if (it.length > 5) {                      // +1
                it.uppercase()
            } else {
                it
            }
        } ?: "default"                                // +1 (Elvis)
    }

    // Range and progression test
    fun rangeTest(n: Int): Int {
        var sum = 0
        for (i in 1..n step 2) {                      // +1
            if (i % 3 == 0) {                         // +1
                sum += i
            } else if (i % 5 == 0) {                  // +1
                sum -= i
            }
        }

        for (i in n downTo 1) {                       // +1
            if (i % 2 == 0) {                         // +1
                sum += i
            }
        }

        return sum
    }
}
