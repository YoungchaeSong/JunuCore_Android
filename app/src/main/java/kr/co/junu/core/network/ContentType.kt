package kr.co.junu.core.network

const val boundary = "----OurVoteBoundary"

enum class ContentType(val value: String) {
    JSON("application/json;charset=UTF-8"),
    MULTIPART("multipart/form-data; boundary=$boundary"),
    FORM("application/x-www-form-urlencoded;charset=UTF-8")
}