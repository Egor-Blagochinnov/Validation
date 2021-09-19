# Validation

Validation is a simple library that allows you to organize complex data validation. The library is primarily designed to validate user input. 

Validation will allow you to: 

- Separate validation logic from presentation logic. 
- Add multiple check conditions to the field 
- Create your own validators 
- Bind validators to any view 



# Installation 

## Gradle

```gradle
repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation 'com.github.Egor-Blagochinnov:Validation:v1.0.0-alpha2'
}
```



# Usage

### Simple validation 

1. Make the simplest `Condition` in the ViewModel

   ```kotlin
   val textFieldCondition = Conditions.RegEx<CharSequence?>(
       "[a-z]+".toRegex(), 
       "only a-z symbols allowed"
   )
   ```

2. In a fragment (or activity), apply a condition to the text field

   ```kotlin
   val editText = requireView().findViewById<EditText>(R.id.edit_text1)
   editText.validateBy(viewModel.textFieldCondition)
   ```

3. Done!

More complex examples of use can be found in the sample app: 
https://github.com/Egor-Blagochinnov/ValidationSample 
