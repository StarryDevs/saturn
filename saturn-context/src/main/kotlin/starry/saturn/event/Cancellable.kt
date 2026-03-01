package starry.saturn.event

public interface Cancellable {
    public val cancelled: Boolean
    public fun cancel()
}

public fun rememberCancellable(initial: Boolean = false) : Cancellable {
    return object : Cancellable {
        private var isCancelled = initial
        override val cancelled: Boolean
            get() = isCancelled

        override fun cancel() {
            isCancelled = true
        }
    }
}

