package com.androiddevs.shoppinglisttestingyt.other

//LiveData가 Event를 1번만 emit하게 도와줌
//만약 network를 통해 LiveData가 error를 받았는데 화면을 전환하면 또 error메세지를 받게됨
//이걸 방지해줌
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}