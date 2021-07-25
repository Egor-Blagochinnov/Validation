package com.egorblagochinnov.validation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.egorblagochinnov.validation.ui.main.ExampleActivity1Fragment

class ExampleActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.example_activity1_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ExampleActivity1Fragment.newInstance())
                .commitNow()
        }
    }
}