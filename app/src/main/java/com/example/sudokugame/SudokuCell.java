
package com.example.sudokugame;

public class SudokuCell {
    public final int row;
    public final int col;
    public int value; // 0 for empty
    public boolean isStartingCell;
    public boolean isHinted;
    public boolean isIncorrect;
    public boolean isSelected;

    public SudokuCell(int row, int col, int value) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.isStartingCell = false;
        this.isHinted = false;
        this.isIncorrect = false;
        this.isSelected = false;
    }

    public SudokuCell(int row, int col, int value, boolean isStartingCell) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.isStartingCell = isStartingCell;
        this.isHinted = false;
        this.isIncorrect = false;
        this.isSelected = false;
    }
    // Add getters and setters if you prefer, or keep fields public for simplicity
    // within the package if this class is primarily used by the ViewModel.
}
