package starry.saturn.event

import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

public class EventBus<E : Event> {

    private val listeners: MutableMap<KClass<out E>, MutableSet<(E) -> Unit>> = mutableMapOf()

    public fun find(type: KClass<out E>): Set<(E) -> Unit> =
        listeners
            .filterKeys { it.isSuperclassOf(type) }
            .values
            .flatten()
            .toSet()

    public fun emit(event: E) {
        for (listener in find(event::class)) {
            if (event is Cancellable && event.cancelled) break
            listener(event)
        }
    }

    @OptIn(ExperimentalContracts::class)
    @Suppress("UNCHECKED_CAST")
    public fun <T : E> on(
        type: KClass<T>,
        listener: (T) -> Unit
    ): (T) -> Unit {
        val set = listeners.getOrPut(type) { mutableSetOf() }
        set.add(listener as (E) -> Unit)
        return listener
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : E> off(listener: (T) -> Unit, type: KClass<out E>) {
        listeners
            .filterKeys { it.isSubclassOf(type) }
            .forEach { _, set -> set.remove(listener as (E) -> Unit) }
    }

}

@OptIn(ExperimentalContracts::class)
public inline fun <reified E : Event> EventBus<in E>.on(
    noinline listener: (E) -> Unit
): (E) -> Unit = on(E::class, listener)

public inline fun <reified E : Event> EventBus<in E>.off(
    noinline listener: (E) -> Unit
) {
    off(listener, E::class)
}
