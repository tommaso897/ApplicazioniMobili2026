package com.example.project2026.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project2026.data.Repository

class VeicoloViewModelFactory(private val repository:Repository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Controlliamo se la classe che vogliamo creare è proprio VeicoloViewModel
        if (modelClass.isAssignableFrom(VeicoloViewModel::class.java)) {
            return VeicoloViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel sconosciuta")
    }
}