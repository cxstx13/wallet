package ru.rfdocs.wallet.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LockManager {

    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    fun isLocked(): Boolean = _locked.value

    fun lock() {
        _locked.value = true
    }

    fun unlock() {
        _locked.value = false
    }
}