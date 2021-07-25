package com.egorblagochinnov.validators.viewbinders

import android.view.View
import com.egorblagochinnov.validators.Validator
import java.lang.ref.WeakReference

/**
 * [ConditionViewBinder] to validate any data [D] in any view [V] through [validator]
 *
 * @param viewRef - A weak reference to a view of any type (TextView, ImageView, FrameLayout and others)
 * @param validator - [Validator] the validator against which the view is validated
 * **/
abstract class ValidatorViewBinder<V : View, D, VL : Validator<D?>>(
    viewRef: WeakReference<V>,
    protected val validator: VL
) : ConditionViewBinder<V, D>(viewRef, validator),
    Validator.OnConditionsChangedListener<D?>,
    Validator.OnOperatorChangedListener {

    override fun attach() {
        super.attach()
        activate()
    }

    private fun activate() {
        validator.addConditionsChangedListener(this)
        validator.addOperatorChangedListener(this)
    }

    override fun detach() {
        super.detach()
        deactivate()
    }

    private fun deactivate() {
        validator.removeConditionsChangedListener(this)
        validator.removeOperatorChangedListener(this)
    }
}