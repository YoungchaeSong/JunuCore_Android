package kr.co.junu.core.extensions

fun String.camelToSnakeCase(): String {
    return replace(Regex("([a-z])([A-Z]+)"), "$1_$2")
        .lowercase()
}