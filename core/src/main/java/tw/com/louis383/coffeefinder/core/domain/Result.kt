package tw.com.louis383.coffeefinder.core.domain

sealed class Result<out T, out E> {
    data class Success<out T>(val value: T) : Result<T, Nothing>()
    data class failed<out E>(val error: E) : Result<Nothing, E>()

    inline fun <C> fold(success: (T) -> C, failed: (E) -> C): C = when (this) {
        is Success -> success(this.value)
        is failed -> failed(this.error)
    }
}

typealias SimpleResult<T> = Result<T, Throwable>