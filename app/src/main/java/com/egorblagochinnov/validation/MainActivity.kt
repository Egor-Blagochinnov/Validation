package com.egorblagochinnov.validation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.egorblagochinnov.validation.ui.main.MainFragment
import com.egorblagochinnov.validators.*
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val editText = findViewById<TextInputEditText>(R.id.edit_text)
        val state = findViewById<TextView>(R.id.state_text)
        val state2 = findViewById<TextView>(R.id.state_text2)
        val switch = findViewById<SwitchCompat>(R.id.switch1)



        /*
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                validation.validator2.addCondition(lengthCondition)
            } else {
                validation.validator2.removeCondition(lengthCondition)
            }
        }
        */

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }
}