package com.egorblagochinnov.validators.viewbinders

import android.view.View
import com.egorblagochinnov.validators.Validator
import java.lang.ref.WeakReference

/**
 * Для валидации любых данных <D> в любых view <V>
 *
 * @param viewRef слабая ссылка на view любого типа (TextView, ImageView, FrameLayout и другие)
 * @param validator [Validator] валидатор, по которому проверяется view
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

    /**
     * Подписывается на валидатор и на условия валидатора
     * **/
    private fun activate() {
        validator.addConditionsChangedListener(this)
        validator.addOperatorChangedListener(this)
    }

    override fun detach() {
        super.detach()
        deactivate()
    }

    /**
     * Отписывается от валидатора и от условий валидатора
     * **/
    private fun deactivate() {
        validator.removeConditionsChangedListener(this)
        validator.removeOperatorChangedListener(this)
    }
}