package tw.com.louis383.coffeefinder

import androidx.lifecycle.LifecycleOwner

interface BaseView {
    fun provideLifecycleOwner(): LifecycleOwner
}