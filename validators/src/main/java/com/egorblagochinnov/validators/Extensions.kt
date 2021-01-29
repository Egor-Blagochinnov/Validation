package com.egorblagochinnov.validators

import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


fun <T> Condition<T>.isValid(value: T): Boolean {
    return this.validate(value).isValid
}

fun TextView.textChanges(): LiveData<String?> {
    val result = MutableLiveData<String?>()

    this.doAfterTextChanged {
        result.value = it?.toString()
    }

    return result
}