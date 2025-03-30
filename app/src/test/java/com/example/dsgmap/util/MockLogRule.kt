package com.example.dsgmap.util

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * JUnit rule to mock Android's Log class during unit tests.
 */
class MockLogRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                // Mock the Log class before test execution
                mockkStatic(Log::class)
                
                // Make all Log methods return dummy values
                every { Log.v(any(), any()) } returns 0
                every { Log.v(any(), any(), any()) } returns 0
                every { Log.d(any(), any()) } returns 0
                every { Log.d(any(), any(), any()) } returns 0
                every { Log.i(any(), any()) } returns 0
                every { Log.i(any(), any(), any()) } returns 0
                every { Log.w(any(), any<String>()) } returns 0
                every { Log.w(any(), any<Throwable>()) } returns 0
                every { Log.w(any(), any<String>(), any()) } returns 0
                every { Log.e(any(), any<String>()) } returns 0
                every { Log.e(any(), any<String>(), any()) } returns 0
                
                try {
                    // Run the test
                    base.evaluate()
                } finally {
                    // Clean up after test execution
                    unmockkStatic(Log::class)
                }
            }
        }
    }
}