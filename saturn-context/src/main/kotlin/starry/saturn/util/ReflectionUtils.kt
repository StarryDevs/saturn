package starry.saturn.util

import kotlin.reflect.KCallable
import kotlin.reflect.KParameter
import kotlin.reflect.full.contextParameters
import kotlin.reflect.full.extensionReceiverParameter

/**
 * 获取一个 [KCallable] 的所有参数，包括普通参数、上下文参数和扩展接收者参数。
 */
@OptIn(ExperimentalContextParameters::class)
public val KCallable<*>.allParameters: Set<KParameter> get() = buildSet {
    addAll(parameters)
    addAll(contextParameters)
    extensionReceiverParameter?.let(::add)
}
