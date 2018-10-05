package com.boloorian.android.intlphonewidget.events

import android.view.View

/**
 * Created by khosrov on 09/05/2018.
 *
 * validation listener
 * To notify user if current number is valid
 */
interface IntlPhoneInputListener {
    fun done(view: View, isValid: Boolean)
}