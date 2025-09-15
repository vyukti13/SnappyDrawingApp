package com.example.snappydrawingapp.draw

class History {
    private val undoStack = mutableListOf<Stroke>()
    private val redoStack = mutableListOf<Stroke>()

    fun add(stroke: Stroke) {
        undoStack.add(stroke)
        redoStack.clear()
    }

    fun undo(): Stroke? {
        if (undoStack.isNotEmpty()) {
            val stroke = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(stroke)
            return stroke
        }
        return null
    }

    fun redo(): Stroke? {
        if (redoStack.isNotEmpty()) {
            val stroke = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(stroke)
            return stroke
        }
        return null
    }

    fun getStrokes(): List<Stroke> = undoStack.toList()
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

