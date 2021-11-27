package edu.gwu.androidtweetsfall2021

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testButtonsEnableWithValidInput() {
        val username = onView(withHint("Username"))
        val password = onView(withHint("Password"))
        val login = onView(withId(R.id.login))
        val signUp = onView(withId(R.id.signUp))

        username.perform(clearText())
        password.perform(clearText())

        login.check(matches(isNotEnabled()))
        signUp.check(matches(isNotEnabled()))

        username.perform(typeText("nick@gwu.edu"))
        password.perform(typeText("abcd12345"))

        login.check(matches(isEnabled()))
        signUp.check(matches(isEnabled()))

        // We can also click the button and test the next screen, but this
        // would be better as a separate test
        login.perform(click())

        val welcomeText = onView(withText("Welcome, nick@gwu.edu"))
        welcomeText.check(matches(isDisplayed()))
    }

    // There are more scenarios we could test (e.g. login failure, sign up, etc.)
}