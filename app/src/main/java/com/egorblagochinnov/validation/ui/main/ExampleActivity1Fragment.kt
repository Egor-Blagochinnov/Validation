package com.egorblagochinnov.validation.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.egorblagochinnov.validation.R
import com.egorblagochinnov.validation.databinding.MainFragment2Binding
import com.egorblagochinnov.validators.Condition
import com.egorblagochinnov.validators.ValidationResult
import com.egorblagochinnov.validators.viewbinders.LiveDataValidatorViewBinder
import com.egorblagochinnov.validators.validateBy
import java.lang.ref.WeakReference

class ExampleActivity1Fragment : Fragment() {

    private lateinit var binding: MainFragment2Binding


    companion object {
        fun newInstance() = ExampleActivity1Fragment()
    }

    private lateinit var viewModel: ExampleViewModel1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment2, container, false)
        binding = MainFragment2Binding.bind(view)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ExampleViewModel1::class.java)
        binding.viewModel = viewModel

        with(binding.editText1) {
            /*bind(viewLifecycleOwner, viewModel.textField1)*/
            validateBy(viewLifecycleOwner, viewModel.textField1Validator)
        }

        with(binding.editText2) {
            /*bind(viewLifecycleOwner, viewModel.textField2)*/
            validateBy(viewLifecycleOwner, viewModel.textField2Validator)
        }

        object : LiveDataValidatorViewBinder<TextView, String?>(WeakReference(binding.editText1), viewModel.textField1Validator) {
            override fun onValidationResult(view: TextView?, result: ValidationResult?) {

            }

            override fun onConditionsChanged(conditions: Set<Condition<String?>>) {

            }

            override fun onOperatorChanged() {

            }
        }

        viewModel.muxValidator.observe(viewLifecycleOwner) {
            if (it.isValid) {
                binding.state.text = "Correct!"
                binding.state.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
            } else {
                binding.state.text = it.errorMessage ?: "Error message is null"
                binding.state.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
            }

        }
    }

}