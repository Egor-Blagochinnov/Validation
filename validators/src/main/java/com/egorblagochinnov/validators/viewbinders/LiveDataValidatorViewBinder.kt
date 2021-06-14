package com.egorblagochinnov.validators.viewbinders

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.egorblagochinnov.validators.Condition
import com.egorblagochinnov.validators.LiveDataValidator
import com.egorblagochinnov.validators.ValidationResult
import java.lang.ref.WeakReference

/**
 * Слушает валидатор с данными любого типа <D>
 * Когда срабатывает валидатор - сообщает о результате проверки
 *
 * @param viewRef слабая ссылка на view любого типа (TextView, ImageView, FrameLayout и другие)
 * @param validator LiveDataValidator валидатор, по которому проверяется view
 * **/
abstract class LiveDataValidatorViewBinder<V : View, D>(
    viewRef: WeakReference<V>,
    validator: LiveDataValidator<D?>
) : ValidatorViewBinder<V, D, LiveDataValidator<D?>>(viewRef, validator),
    Observer<ValidationResult> {
    constructor(
        lifecycleOwner: LifecycleOwner,
        viewRef: WeakReference<V>,
        validator: LiveDataValidator<D?>
    ) : this(viewRef, validator) {
        attach(lifecycleOwner)
    }

    fun attach(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
        validator.observe(lifecycleOwner, this)
    }

    override fun attach() {
        super.attach()
        validator.observeForever(this)
    }

    override fun detach() {
        super.detach()
        validator.removeObserver(this)
    }

    /**
     * При изменении валидатора приводит поле в соответствующее состояние
     * **/
    override fun onChanged(t: ValidationResult?) {
        onValidationResult(t)
    }

    override fun getValidationData(view: V?): D? {
        return validator.source.value
    }

    companion object {
        fun <V : View, D> create(
            view: V,
            validator: LiveDataValidator<D?>,
            onResult: (view: V?, result: ValidationResult?) -> Unit
        ): LiveDataValidatorViewBinder<V, D> {
            return object : LiveDataValidatorViewBinder<V, D>(WeakReference(view), validator) {
                override fun onValidationResult(view: V?, result: ValidationResult?) {
                    onResult(view, result)
                }

                override fun onOperatorChanged() {

                }

                override fun onConditionsChanged(conditions: Set<Condition<D?>>) {

                }
            }
        }
    }
}