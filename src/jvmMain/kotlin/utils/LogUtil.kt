package utils

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf

object LogUtil {
    val messages = mutableStateListOf<Any>()
}