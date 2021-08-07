package tw.com.louis383.coffeefinder

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by louis383 on 2017/1/13.
 */
abstract class BasePresenter<T: BaseView> : LifecycleObserver {
    protected var view: T? = null
    val isViewAttached: Boolean
        get() = view != null

    private val presenterJob: Job = SupervisorJob()
    private val defaultErrorHandling = CoroutineExceptionHandler { _, throwable ->
        Log.e("BasePresenter", Log.getStackTraceString(throwable))
    }
    private val coroutineContext: CoroutineContext = Dispatchers.Main + presenterJob + defaultErrorHandling
    val uiScope = CoroutineScope(coroutineContext)

    open fun attachView(view: T) {
        this.view = view
        view.provideLifecycleOwner().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detachView() {
        if (isViewAttached) {
            view = null
            presenterJob.cancel()
        }
    }
}