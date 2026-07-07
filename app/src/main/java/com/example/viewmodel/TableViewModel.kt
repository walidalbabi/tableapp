package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.model.TableCell
import com.example.model.TableFile
import com.example.repository.TableRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.model.CellStyle

data class TableState(
    val rows: Int = 0,
    val columns: Int = 0,
    val cells: List<TableCell> = emptyList(),
    val isInitialized: Boolean = false,
    val selectedCells: Set<Pair<Int, Int>> = emptySet(),
    val copiedStyle: CellStyle? = null
)

class TableViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreManager = DataStoreManager(application)
    private val repository = TableRepository(application)
    
    private val _tableState = MutableStateFlow(TableState())
    val tableState: StateFlow<TableState> = _tableState.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val undoStack = mutableListOf<TableState>()
    private val redoStack = mutableListOf<TableState>()

    private var autosaveJob: Job? = null

    init {
        viewModelScope.launch {
            _isDarkMode.value = dataStoreManager.isDarkModeFlow.first()
            
            dataStoreManager.isDarkModeFlow.collect {
                _isDarkMode.value = it
            }
        }
        
        viewModelScope.launch {
            val autosaveData = dataStoreManager.autosaveDataFlow.first()
            if (autosaveData != null) {
                try {
                    val file = Json.decodeFromString<TableFile>(autosaveData)
                    _tableState.value = TableState(
                        rows = file.rows,
                        columns = file.columns,
                        cells = file.cells,
                        isInitialized = true
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        startAutosave()
    }

    private fun startAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            while (true) {
                delay(30_000)
                val currentState = _tableState.value
                if (currentState.isInitialized) {
                    val tableFile = TableFile(
                        rows = currentState.rows,
                        columns = currentState.columns,
                        theme = if (_isDarkMode.value) "dark" else "light",
                        cells = currentState.cells
                    )
                    try {
                        val jsonString = Json.encodeToString(tableFile)
                        dataStoreManager.setAutosaveData(jsonString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            dataStoreManager.setDarkMode(!_isDarkMode.value)
        }
    }

    fun createNewTable(rows: Int, columns: Int) {
        val initialCells = mutableListOf<TableCell>()
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                initialCells.add(TableCell(row = r, column = c))
            }
        }
        val newState = TableState(rows = rows, columns = columns, cells = initialCells, isInitialized = true)
        pushToUndoStack()
        _tableState.value = newState
    }

    fun updateCellText(row: Int, column: Int, text: String) {
        pushToUndoStack()
        val currentCells = _tableState.value.cells.toMutableList()
        val index = currentCells.indexOfFirst { it.row == row && it.column == column }
        if (index != -1) {
            currentCells[index] = currentCells[index].copy(text = text)
            _tableState.value = _tableState.value.copy(cells = currentCells)
        }
    }

    fun toggleAnswerCell(row: Int, column: Int) {
        pushToUndoStack()
        val currentCells = _tableState.value.cells.toMutableList()
        val index = currentCells.indexOfFirst { it.row == row && it.column == column }
        if (index != -1) {
            currentCells[index] = currentCells[index].copy(answerCell = !currentCells[index].answerCell)
            _tableState.value = _tableState.value.copy(cells = currentCells)
        }
    }
    
    fun clearCell(row: Int, column: Int) {
        pushToUndoStack()
        val currentCells = _tableState.value.cells.toMutableList()
        val index = currentCells.indexOfFirst { it.row == row && it.column == column }
        if (index != -1) {
            currentCells[index] = currentCells[index].copy(text = "")
            _tableState.value = _tableState.value.copy(cells = currentCells)
        }
    }

    fun addRow() {
        if (!_tableState.value.isInitialized) return
        pushToUndoStack()
        val state = _tableState.value
        val newRows = state.rows + 1
        val newCells = state.cells.toMutableList()
        for (c in 0 until state.columns) {
            newCells.add(TableCell(row = state.rows, column = c))
        }
        _tableState.value = state.copy(rows = newRows, cells = newCells)
    }

    fun removeRow() {
        if (!_tableState.value.isInitialized || _tableState.value.rows <= 1) return
        pushToUndoStack()
        val state = _tableState.value
        val newRows = state.rows - 1
        val newCells = state.cells.filter { it.row < newRows }
        _tableState.value = state.copy(rows = newRows, cells = newCells)
    }

    fun addColumn() {
        if (!_tableState.value.isInitialized) return
        pushToUndoStack()
        val state = _tableState.value
        val newCols = state.columns + 1
        val newCells = state.cells.toMutableList()
        for (r in 0 until state.rows) {
            newCells.add(TableCell(row = r, column = state.columns))
        }
        _tableState.value = state.copy(columns = newCols, cells = newCells)
    }

    fun removeColumn() {
        if (!_tableState.value.isInitialized || _tableState.value.columns <= 1) return
        pushToUndoStack()
        val state = _tableState.value
        val newCols = state.columns - 1
        val newCells = state.cells.filter { it.column < newCols }
        _tableState.value = state.copy(columns = newCols, cells = newCells)
    }

    fun duplicateRow(rowToDuplicate: Int) {
        if (!_tableState.value.isInitialized) return
        pushToUndoStack()
        val state = _tableState.value
        val newRows = state.rows + 1
        val newCells = mutableListOf<TableCell>()
        
        for (r in 0 until state.rows) {
            val cellsInRow = state.cells.filter { it.row == r }.map { it.copy(row = if (it.row > rowToDuplicate) it.row + 1 else it.row) }
            newCells.addAll(cellsInRow)
            
            if (r == rowToDuplicate) {
                val duplicatedCells = state.cells.filter { it.row == r }.map { it.copy(row = r + 1) }
                newCells.addAll(duplicatedCells)
            }
        }
        _tableState.value = state.copy(rows = newRows, cells = newCells)
    }

    fun duplicateColumn(colToDuplicate: Int) {
        if (!_tableState.value.isInitialized) return
        pushToUndoStack()
        val state = _tableState.value
        val newCols = state.columns + 1
        val newCells = mutableListOf<TableCell>()

        for (c in 0 until state.columns) {
            val cellsInCol = state.cells.filter { it.column == c }.map { it.copy(column = if (it.column > colToDuplicate) it.column + 1 else it.column) }
            newCells.addAll(cellsInCol)

            if (c == colToDuplicate) {
                val duplicatedCells = state.cells.filter { it.column == c }.map { it.copy(column = c + 1) }
                newCells.addAll(duplicatedCells)
            }
        }
        _tableState.value = state.copy(columns = newCols, cells = newCells)
    }
    
    // Selection Management
    fun selectCell(row: Int, column: Int, toggle: Boolean = false) {
        val currentSelection = _tableState.value.selectedCells.toMutableSet()
        val pair = Pair(row, column)
        if (toggle) {
            if (currentSelection.contains(pair)) {
                currentSelection.remove(pair)
            } else {
                currentSelection.add(pair)
            }
        } else {
            currentSelection.clear()
            currentSelection.add(pair)
        }
        _tableState.value = _tableState.value.copy(selectedCells = currentSelection)
    }

    fun clearSelection() {
        _tableState.value = _tableState.value.copy(selectedCells = emptySet())
    }
    
    fun selectAll() {
        val state = _tableState.value
        val allCells = mutableSetOf<Pair<Int, Int>>()
        for (r in 0 until state.rows) {
            for (c in 0 until state.columns) {
                allCells.add(Pair(r, c))
            }
        }
        _tableState.value = state.copy(selectedCells = allCells)
    }

    // Formatting Actions
    fun applyStyleToSelection(updater: (CellStyle) -> CellStyle) {
        val state = _tableState.value
        if (state.selectedCells.isEmpty()) return
        
        pushToUndoStack()
        val currentCells = state.cells.toMutableList()
        
        for (pair in state.selectedCells) {
            val index = currentCells.indexOfFirst { it.row == pair.first && it.column == pair.second }
            if (index != -1) {
                val updatedStyle = updater(currentCells[index].style)
                currentCells[index] = currentCells[index].copy(style = updatedStyle)
            }
        }
        _tableState.value = state.copy(cells = currentCells)
    }
    
    fun copyStyle() {
        val state = _tableState.value
        val firstSelected = state.selectedCells.firstOrNull()
        if (firstSelected != null) {
            val cell = state.cells.find { it.row == firstSelected.first && it.column == firstSelected.second }
            _tableState.value = state.copy(copiedStyle = cell?.style)
        }
    }
    
    fun pasteStyle() {
        val style = _tableState.value.copiedStyle ?: return
        applyStyleToSelection { style }
    }

    private fun pushToUndoStack() {
        val currentState = _tableState.value
        if (currentState.isInitialized) {
            undoStack.add(currentState)
            if (undoStack.size > 30) {
                undoStack.removeAt(0)
            }
            redoStack.clear()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_tableState.value)
            _tableState.value = undoStack.removeLast()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_tableState.value)
            _tableState.value = redoStack.removeLast()
        }
    }

    suspend fun saveToFile(uri: Uri): Boolean {
        val state = _tableState.value
        if (!state.isInitialized) return false
        val file = TableFile(
            rows = state.rows,
            columns = state.columns,
            theme = if (_isDarkMode.value) "dark" else "light",
            cells = state.cells
        )
        return repository.saveTableToFile(uri, file)
    }

    suspend fun loadFromFile(uri: Uri): Boolean {
        val file = repository.loadTableFromFile(uri)
        if (file != null) {
            pushToUndoStack()
            _tableState.value = TableState(
                rows = file.rows,
                columns = file.columns,
                cells = file.cells,
                isInitialized = true
            )
            if (file.theme == "dark" && !_isDarkMode.value) {
                toggleTheme()
            } else if (file.theme == "light" && _isDarkMode.value) {
                toggleTheme()
            }
            return true
        }
        return false
    }
}
