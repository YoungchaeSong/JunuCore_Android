package kr.co.junu.core.extensions

fun <T : Any> T.toMap(clazz: Class<T>): Map<String, Any?> {
    return clazz.declaredFields
        .onEach { it.isAccessible = true }
        .associate { it.name to it.get(this) }
}

fun <T : Any> T.toFieldMap(clazz: Class<T>): Map<String, Any?> {
    return clazz.declaredFields
        .onEach { it.isAccessible = true }
        .associate { it.name to it.get(this) }
        .filterValues { it != null }
}