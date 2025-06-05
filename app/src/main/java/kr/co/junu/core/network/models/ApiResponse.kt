package kr.co.junu.core.network.models

import androidx.annotation.Keep

@Keep
enum class Status {
    SUCCESS,
    FAIL
}

@Keep
data class ApiResponse<T> (
    val status: Status,
    val message: String,
    val data: T?
)
