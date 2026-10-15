package com.example.fendly

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageActivityTest {
    @Test
    fun languageSearchFiltersPickerChoices() {
        ActivityScenario.launch(LanguageActivity::class.java).use {
            onView(withText("Choose your language")).check(matches(isDisplayed()))
            onView(withHint("Search language"))
                .perform(typeText("Gujarati"), closeSoftKeyboard())
            onView(withText("ગુજરાતી")).check(matches(isDisplayed()))
            onView(withText("हिन्दी")).check(doesNotExist())
        }
    }
}
