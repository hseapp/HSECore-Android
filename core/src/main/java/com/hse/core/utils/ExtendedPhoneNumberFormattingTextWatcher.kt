package com.hse.core.utils

import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable

class ExtendedPhoneNumberFormattingTextWatcher : PhoneNumberFormattingTextWatcher() {
    override fun afterTextChanged(s: Editable?) {
        if (s != null && !s.isBlank() && !s.startsWith("+")) {
            s.replace(0, s.length, "+$s")
        }
        super.afterTextChanged(s)
    }
}