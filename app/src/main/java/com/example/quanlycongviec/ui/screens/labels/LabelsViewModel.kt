package com.example.quanlycongviec.ui.screens.labels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.LabelRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Label
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class LabelsViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val labelRepository = AppModule.provideLabelRepository()

    private val _uiState = MutableStateFlow(LabelsUiState())
    val uiState: StateFlow<LabelsUiState> = _uiState.asStateFlow()

    init {
        loadLabels()
    }

    private fun loadLabels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val labels = labelRepository.getLabelsForUser(currentUserId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            labels = labels
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load labels: ${e.message}"
                    )
                }
            }
        }
    }

    fun createLabel(name: String, color: String) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch

                val newLabel = Label(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    color = color,
                    userId = currentUserId
                )

                labelRepository.createLabel(newLabel)
                loadLabels()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to create label: ${e.message}")
                }
            }
        }
    }

    fun updateLabel(label: Label) {
        viewModelScope.launch {
            try {
                labelRepository.updateLabel(label)
                loadLabels()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update label: ${e.message}")
                }
            }
        }
    }

    fun deleteLabel(labelId: String) {
        viewModelScope.launch {
            try {
                labelRepository.deleteLabel(labelId)
                loadLabels()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete label: ${e.message}")
                }
            }
        }
    }
}

data class LabelsUiState(
    val isLoading: Boolean = false,
    val labels: List<Label> = emptyList(),
    val errorMessage: String? = null
)
