# Validation


# Установка

```
allprojects {
    repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```


```
dependencies {
    implementation 'com.github.Egor-Blagochinnov:Validation:v1.0.0-alpha2'
}
```

[TOC]

# Проблематика

Часто в мобильных приложениях приходится делать различные экраны для ввода пользователем информации. Но так как пользователи не отличаются умом и сообразительностью - приходится проверять, что они там написали: запрещенные символы, максимальная длина, соответсвие RegExp и так далее.

Первое, что приходит в голову для решения проблемы валидации пользовательского ввода - прицепить регулярку прямо на `EditText` 

Например, вот так:

```kotlin
val pattern = "[a-z]+".toRegex()
editText.addTextChangedListener {
    val text = it?.toString() ?: return@addTextChangedListener
    if (text.matches(pattern)) {
        hideError(editText)
    } else {
        showError(editText, "Only a-z symbols allowed")
    }
}
```

Но а если теперь я хочу проверять поле по двум разным RegExp и выводить разные ошибки?

Ну тогда можно добавить второй слушатель:

```Kotlin
editText.addTextChangedListener {
    val text = it?.toString() ?: return@addTextChangedListener
    if (text.length <= 10) {
        hideError(editText)
    } else {
        showError(editText, "Only 10 symbols allowed")
    }
}
```

Однако, теперь если в поле ввести строку "12345" то ошибки не будет: первый слушатель выставит ошибку на поле, потому что поле содержит цифры, а вот второй слушатель скроет ошибку, потому что по его мнению - поле правильное, не более 10 символов.

И это только одна проблемка. Дальше - веселее:

- Как повесить на поле множество правил валидации, чтобы они правильно работали вместе?
- Как добавлять или удалять правила?
- Где хранить эту кучу валидаторов?
- Как проверять любые типы данных, а не только строковые?
- и многое другое...



# Решение

## Condition - основа всего

В основе всей валидации лежит условие (`Condition`) - простейший интерфейс с одним методом `validate`.

![image-20210725145014322](/Users/egorblagocinnov/Documents/image-20210725145014322.png)

### Как это работает?

На изи!
У `Condition` есть метод `validate(data)`, который проверит данные и вернёт результат `ValidationResult`. Внутри `ValidationResult` будет булевый результат проверки `isValid` и сообщение об ошибке, которое должно появляться если `isValid == false`

### Сложений и умножение 

`Condition` можно складывать и умножать. Сложение работает как аналог булевого ИЛИ, а умножение как аналог булевого И

Сложение (ИЛИ)

| ИЛИ                 | Conditon(true) | Conditon(false) |
| ------------------- | -------------- | --------------- |
| **Conditon(true)**  | Conditon(true) | Conditon(true)  |
| **Conditon(false)** | Conditon(true) | Conditon(false) |

Умножение (И)

| И                   | Conditon(true)  | Conditon(false) |
| ------------------- | --------------- | --------------- |
| **Conditon(true)**  | Conditon(true)  | Conditon(false) |
| **Conditon(false)** | Conditon(false) | Conditon(false) |



## Validator - проверка по множеству условий

А что если надо проверять значение по множеству условий?

Тут на помошь приходит `Validator`, он содержит в себе набор  условий `Set<Condition>`, проверяет значение по всем этим условия и выдаёт финальный результат `ValidationResult`

![Снимок экрана 2021-08-10 в 18.00.40](/Users/egorblagocinnov/Documents/Снимок экрана 2021-08-10 в 18.00.40.png)

### Как это работает?

`Validator` по-сути является `Condition`, только более прокаченный.

Внутри `Validator`  находится множуство условий `Set<Condition>`. В момент проверки значение проверяется по каждому из условий, формируется набор результатов валидации `Set<ValidationResult>`. Затем, этот набор с результатами передается на вход оператору (`Operator`), который и решает, какой будет финальный результат валидации. Вот, Всё.

У валидатора есть свои приколы:

### Оператор

`Operator` - это просто `Condition<Collection<ValidationResult>>`, то есть тупа проверяет коллекцию результатов валидации. Получается такой аналог логического оператора из начального курса булевой алгебры. По-умолчанию используется оператор-конъюнкция.

Но можно написать свой оператор, который, например, будет выдавать `ValidationResult(true)` если количество валидных условий достигло порогового значения.

```kotlin
class ThresholdOperator(val validThreshold: Int) : Validator.Operator {
    override fun validate(value: Collection<ValidationResult>?): ValidationResult {
        val validCount = value?.count { it.isValid } ?: 0

        return if (validCount >= validThreshold) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid("Less than $validThreshold valid conditions")
        }
    }
}
```

#### Установка оператора

```kotlin
validator.setOperator(ThresholdOperator(validator.getConditionsSet().size / 2))
```

#### Удаление оператора

Нельзя удалять оператор! `Validator` не может работать без оператора

#### Наблюдение за изменением оператора

Может быть такое, что необходимо отслеживать изменения оператора. Например, чтобы обновить view.

```kotlin
validator.addOperatorChangedListener {
    // on operator changed
}

//И удаляем слушателя
validator.removeOperatorChangedListener(operatorListener)
```

### Набор условий

#### Добавление условия

```Kotlin
validator.addCondition(Condition { string ->
    ValidationResult.obtain(string?.contains("target") == true, "String must contains target")
})
```

#### Удаление условия

```kotlin
validator.removeCondition(condition)
```

#### Наблюдение за изменением условий

Чтобы следить за списком условий - добавьте слушателя `OnConditionsChangedListener`, который будет вызываться при любом изменении условий

```Kotlin
validator.addConditionsChangedListener { newConditions ->
    //on new conditions
}
```

#### Изменение условий

Если нужно сделать много преобразований можно использовать `changeConditionsSet`, чтобы слушатель `OnConditionsChangedListener` сработал только один раз - после всех преобразований набора условий.

```
validator.changeConditionsSet {
    this.add(Conditions.RequiredField())
    this.remove(condition2)
    this.add(Conditions.NotNull())
}
```



## LiveDataValidator - реактивная валидация

Было бы удобно, если бы валидатор самостоятельно проверял данные при каждом их изменении. Так и сделаем! Сейчас модно молодежно использовать `LiveData`. Так пусть валидатор подпишется на неё и будет проверять каждое значение. 

`LiveDataValidator` работает так же как и обычный `Vlidator`, однако у него есть свои приколы:

### Состояние (`state`). 

Состояние это результат последней проверки. 
Представляет собой `LiveData<ValidationResult>`, поэтому за состоянием валидатора можно удобно следить.
`LiveDataValidator` всегда в актуальном состоянии пока он подписан на источник (`Validator.observe`; `Validator.observeForever`)

```kotlin
liveDataValidator.state
```

### Активация LiveDataValidator

LiveDataValidator начинает работать только тогда, когда хоть кто-нибудь подписан на него

```kotlin
liveDataValidator.state.observe(viewLifecycleOwner) { validationResult ->
    //apply validation result
}

//Или можно вот так, разницы нет
liveDataValidator.observe(viewLifecycleOwner) { validationResult ->
    //apply validation result
}
```

### Реакция на другие `LiveData`

`LiveDataValidator` умеет следить за другими LiveData и реагировать на их изменения

Для этого есть метод `watchOn`

```kotlin
liveDataValidator.watchOn(textMaxLength) { newTextMaxLength ->
    liveDataValidator.validate()
}
```

В примере выше `liveDataValidator` следит за полем `textMaxLength` и как только значение  `textMaxLength` меняется `liveDataValidator` принудительно валидируется

Для подобных случаев есть метод `triggerOn`, который запускает валидацию всякий раз когда изменяется дополнительный источник

```Kotlin
liveDataValidator.triggerOn(textMaxLength)
```

#### Пример

Есть 2 текстовых поля: на одном пики точены, на другом ~~х** д*****ы~~ вовсе не пики
Задача, чтобы второе поле не содержало в себе текст первого поля

```kotlin
val first: MutableLiveData<String?> = MutableLiveData<String?>() //Первое поле (с пиками)
val second: MutableLiveData<String?> = MutableLiveData<String?>() //Второе поле (с другими пиками)

val secondValidator: LiveDataValidator<String?> = LiveDataValidator(second).apply {
    addCondition(Conditions.RequiredField())
    addCondition(Conditions.RegEx("[a-z]+".toRegex(), "only a-z symbols allowed"))
    addCondition(Conditions.TextMaxLength(10))

    addCondition {
      	// Внимание сюда: для проверки используется внешнее мутабельное поле first!
        if (it?.contains(first.value ?: "") == true) {
            ValidationResult.invalid("textField2 should not contains textField1")
        } else {
            ValidationResult.valid()
        }
    }

    triggerOn(first) //Теперь при каждом изменении first будет вызываться метод validate()
}
```

Как видно, `secondValidator` проверяет поле `second`, но при этом использует исползует `first` для проверки.
Но что если `first` изменился? Тогда валидатор будет висеть в неактуальном состоянии до следующего изменения `second`.
Поэтому валидатору нужно следить за `first` и при каждом его изменении принудительно выполнять проверку
Делается это методом `triggerOn(LiveData<*>)`, который будет запускать валидатор при каждом изменении `first`

Вместо `triggerOn` можно так же использовать `watchOn` и самостоятельно прописать нужное действие

```kotlin
watchOn(textField1) {
    validate()
}
```



## MuxLiveDataValidator - объединяем валидаторы

А теперь, когда у нас есть куча полей с LiveDataValidator'ами надо каким-то образом опредилить общий результат валидации. Самый распространённый пример: если все поля на форме заполнены правильно - включаем кнопку "Далее".

Для этого есть `MuxLiveDataValidator`.  Он подписывается на множество `LiveDataValidator'ов` и как только один из них изменяется - `MuxLiveDataValidator` собирает состояния (`ValidationResult`) всех LiveDataValidator'ов и отдаёт их на проверку оператору (`Operator`). Operator выдаёт окончательный результат.

Короче, `MuxLiveDataValidator` работает типа как мультиплексор. Отсюда и название.

![Снимок экрана 2021-08-14 в 15.21.41](/Users/egorblagocinnov/Documents/Снимок экрана 2021-08-14 в 15.21.41.png)

### Состояние (state)

Аналогично `LiveDataValidator` у `MuxLiveDataValidator`  есть состояние

```kotlin
muxValidator.state
```

Состояние это `LiveData<ValidationResult>` в котором находится последний результат проверки.

### Активация MuxLiveDataValidator

Тут как у `LiveDataValidator` - доступ только по подписке

```kotlin
viewModel.muxValidator.observe(viewLifecycleOwner) { validationResult ->
    // apply validatioin result
}
```

Примечание

Когда вы подписываетесь на `MuxLiveDataValidator`, то все его `LiveDataValidator` активируются, то есть подписка распространяется и на них (такой вот аналог семейной подписки у `MediatorLiveData` ). То есть если вы подписались на `MuxLiveDataValidator`, то не можно не подписываться на те `LiveDataValidator`, за которыми он следит.

### Добавление валидатора

Добавить `LiveDataValidator` можно при создании `MuxLiveDataValidator`

```kotlin
val muxValidator = MuxLiveDataValidator(
    textField1Validator,
    textField2Validator
)
```

Можно и после создания

```kotlin
muxValidator.addValidator(textField3Validator)

//Можешь докинуть сразу несколько
muxValidator.addValidators(
  listOf(
    textField4Validator, 
    textField5Validator
  )
)
```

### Удаление валидатора

Ну тут типа ваще всё изян

```kotlin
muxValidator.removeValidator(textField3Validator)
```

### Установка оператора

По-умолчанию `MuxLiveDataValidator` использует оператор-конъюнкцию. Чтобы поменять логику выдачи финального `ValidationResult` нужно установить другой оператор

```kotlin
muxValidator.setOperator(Validator.Operator.Disjunction())
```

Есть возможность следить за сменой оператора чтобы, например, очистить ошибку на view.

```kotlin
muxValidator.addOperatorChangedListener {
    // on operator changed
}

//Удалить слушателя можно примерно вот так
 muxValidator.removeOperatorChangedListener(listener)
```



## Подключение валидаторов к view

### ConditionViewBinder

`ConditionViewBinder` базовый связыватель `view` и `Condition`

Работает так:

В момент вызова `ConditionViewBinder.validate()` достает из view данные для проверки абстрактным методом `getValidationData()`. Эти данные улетают в `Condition`, который проверит их и вернет `ValidationResult`. Затем этот `ValidationResult` передаётся абстрактному методу `onValidationResult()` в котором и происходит изменения view.

![Снимок экрана 2021-08-21 в 1.43.18](/Users/egorblagocinnov/Documents/Снимок экрана 2021-08-21 в 1.43.18.png)

#### Пример

```kotlin
val editText1 = requireView().findViewById<EditText>(R.id.edit_text1)
val condition = Conditions.TextMaxLength<String?>(10)

val conditionBinder = object : ConditionViewBinder<TextView, String?>(WeakReference(editText1), condition) {
    override fun getValidationData(view: TextView?): String? {
        return view?.text?.toString()
    }

    override fun onValidationResult(view: TextView?, result: ValidationResult?) {
        if (result?.isValid == true) {
            view?.error = null
        } else {
            view?.error = result?.errorMessage
        }
    }
}

conditionBinder.check()
```

Таким образом можно привязать любой валидатор к любой view



### ValidatorViewBinder

Предназначен для более удобной работы с `Validator`: следит за изменениями оператора и условий валидатора.

### LiveDataValidatorViewBinder

`LiveDataValidator` - особый пациент. Для него свой binder, который:

- сам подписывается/отписывается на `LiveDataValidator` ( чтобы активировать его)
- `getValidationData()`  берется не из view, а прямо из валидатора (из его `source`)

#### Активация

`LiveDataValidatorViewBinder` нужно активировать. Тут 2 способа:

- Через конструктор. В конструктор передать `LifeycleOwner`

  ```kotlin
  object : LiveDataValidatorViewBinder<TextView, String?>(
    	viewLifecycleOwner, 
    	WeakReference(binding.editText1), 
    	viewModel.textField1Validator) {
          override fun onValidationResult(view: TextView?, result: ValidationResult?) {
  
          }
  
          override fun onConditionsChanged(conditions: Set<Condition<String?>>) {
  
          }
  
          override fun onOperatorChanged() {
  
          }
  }
  ```

- Просто вызвать `attach`

  ```kotlin
  liveDataValidatorViewBinder.attach(viewLifecycleOwner)
  ```



### Готовые реализации

#### TextConditionViewBinder

Связывает простые `Condition` с `TextView`. Проверяет поле при каждом изменении текста в нём

Использовать так:

```kotlin
val editText1 = requireView().findViewById<EditText>(R.id.edit_text1)
val condition = Conditions.TextMaxLength<CharSequence?>(10)

editText.validateBy(condition)
```

#### TextViewLiveDataValidatorBinder

Тут то же самое, что и `TextConditionViewBinder`, но тут работаем с `LiveDataValidator`.

Использовать так:

```Kotlin
val editText = requireView().findViewById<EditText>(R.id.edit_text1)
val liveDataValidator = LiveDataValidator(viewModel.textField1, Conditions.RequiredField())

editText.validateBy(viewLifecycleOwner, liveDataValidator)
```



## Рекомендации

1. Все валидаторы должны находиться во ViewModel (ну или в Presenter)
   Не надо выносить логику валидирования во фрагменты, активности и вообще на view уровень. 
2. По-возможности используйте `LiveDataValidator`.
   Он самый прокаченный. И вообще вся библиотека ради него написана была
3. Аккуратнее с множеством условий. 
   Вы можете добавить на поле противоречащие друг другу условия и будет непонятно что!
4. Делайте свои реализации.
   Создавайте свои `ConditionViewBinder`ы, чтобы работать с кастомными view
   Создавайте свои валидаторы если вам нужна более сложная валидация



## Примеры

### Простая валидация

1. Во ViewModel делаем простейший `Condition`

   ```kotlin
   val textFieldCondition = Conditions.RegEx<CharSequence?>(
       "[a-z]+".toRegex(), 
       "only a-z symbols allowed"
   )
   ```

2. Во фрагменте (или активити) применяем условие к текстовому полю

   ```kotlin
   val editText = requireView().findViewById<EditText>(R.id.edit_text1)
   editText.validateBy(viewModel.textFieldCondition)
   ```

3. Готово

   <video src="/Users/egorblagocinnov/Desktop/validation demo/simple_condition1.webm"></video>

### Сложная валидация

Допустим у нас есть 3 поля: поле для ввода цифр, поле для ввода букв и поле, которое указывает максимальную длину поля ввода цифр. О как!
А ещё нужно выводить общее состояние валидации всей формы в отдельное текстовое поле!

1. Для начала объявим сами поля и валидаторы к ним во ViewModel]

   ```Kotlin
   //Поле, которое определяет максимальную длину поля для ввода цифр
   val textMaxLength: MutableLiveData<String?> = MutableLiveData<String?>()
   
   //Цифровое поле
   val textField1: MutableLiveData<String?> = MutableLiveData<String?>()
   //Про этот валидатор - чуть ниже
   val textField1Validator = ExampleValidators.DigitsFieldValidator(textField1).apply {
       watchOn(textMaxLength) {
           val maxLength = kotlin.runCatching { it?.toInt() }.getOrNull()
           this.setMaxLength(maxLength)
       }
   }
   
   //Буквенное поле
   val textField2: MutableLiveData<String?> = MutableLiveData<String?>()
   val textField2Validator: LiveDataValidator<String?> = LiveDataValidator(textField2).apply {
       addCondition(Conditions.RequiredField())
       addCondition(Conditions.RegEx("[a-z]+".toRegex(), "only a-z symbols allowed"))
       addCondition(Conditions.TextMaxLength(10))
   }
   
   //Обобщенный валидатор
   val muxValidator = MuxLiveDataValidator(
       textField1Validator,
       textField2Validator
   )
   ```

2. Чтобы динамически менять условия валидации - лучше всего написать свой валидатор. Потому что для смены условий нужно хранить ссылки на эти самые условия, а это лучше сделать в отдельном классе

   ```Kotlin
   class ExampleValidators {
       class DigitsFieldValidator<S : CharSequence?>(
           source: LiveData<S>,
           initialCondition: Condition<S?>? = null,
           operator: Operator = Operator.Conjunction()
       ) : LiveDataValidator<S>(
           source,
           initialCondition,
           operator
       ) {
           val onlyDigitsCondition = Conditions.RegEx<S>("[0-9]+".toRegex(), "only digits allowed")
           private var maxLengthCondition = Conditions.TextMaxLength<S?>(5) //по-умолчанию пусть будет 5
   
           init {
               addCondition(onlyDigitsCondition)
               addCondition(maxLengthCondition)
           }
   
           fun setMaxLength(maxLength: Int?) {
               if (maxLength == null || maxLength < 0) {
                   removeCondition(maxLengthCondition)
                   return
               }
   
               val newCondition = Conditions.TextMaxLength<S?>(maxLength)
   
               changeConditionsSet {
                   remove(maxLengthCondition)
                   maxLengthCondition = newCondition
                   add(maxLengthCondition)
               }
           }
       }
   }
   ```

3. Теперь идём во фрагмент и подключаем всё это дело

   ```kotlin
   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
           super.onViewCreated(view, savedInstanceState)
   
           viewModel = ViewModelProvider(this).get(ExampleViewModel1::class.java)
           binding.viewModel = viewModel
           
     			// Подключаем валидатор к цифровому полю
           binding.editText1.validateBy(
               viewLifecycleOwner, 
               viewModel.textField1Validator
           )
     
   				// Подключаем валидатор к буквенному полю
           binding.editText2.validateBy(
               viewLifecycleOwner,
               viewModel.textField2Validator
           )
   				
     			//Следим за обобщенным (mux) валидатором
           viewModel.muxValidator.observe(viewLifecycleOwner) {
               setGeneralValidationResult(it)
           }
       }
       
   		//Отображем результат общей валидации в отдельном текстовом поле "state"
       private fun setGeneralValidationResult(validationResult: ValidationResult) {
           if (validationResult.isValid) {
               binding.state.text = "Correct!"
               binding.state.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
           } else {
               binding.state.text = validationResult.errorMessage ?: "Error message is null"
               binding.state.setTextColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
           }
       }
   ```

   Готово!

   <video src="/Users/egorblagocinnov/Desktop/validation demo/complex_validation.webm"></video>
