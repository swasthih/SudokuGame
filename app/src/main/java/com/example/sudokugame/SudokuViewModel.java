package com.example.sudokugame; // Assuming this is your package

import android.util.Pair; // Using android.util.Pair

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random; // Needed for the placeholder generator
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// You'll need SudokuCell.java and Difficulty.java in the same package or imported

public class SudokuViewModel extends ViewModel {

    private final MutableLiveData<SudokuCell[][]> _sudokuBoard = new MutableLiveData<>();
    public LiveData<SudokuCell[][]> sudokuBoard = _sudokuBoard;

    private final MutableLiveData<Pair<Integer, Integer>> _selectedCellLiveData = new MutableLiveData<>();
    public LiveData<Pair<Integer, Integer>> selectedCellLiveData = _selectedCellLiveData;

    private final MutableLiveData<Boolean> _isSolved = new MutableLiveData<>(false); // Initialize
    public LiveData<Boolean> isSolved = _isSolved;

    private final MutableLiveData<Long> _elapsedTime = new MutableLiveData<>(0L);
    public LiveData<Long> elapsedTime = _elapsedTime;
    // TODO: Implement Timer logic (e.g., using a Handler and Runnable or another mechanism)
    // This timer should update _elapsedTime.postValue(newTime) periodically.

    private int[][] solution = new int[9][9];
    private SudokuCell[][] currentPuzzleCells = emptySudokuBoard();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private SudokuCell[][] emptySudokuBoard() {
        SudokuCell[][] board = new SudokuCell[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                board[r][c] = new SudokuCell(r, c, 0);
            }
        }
        return board;
    }

    public SudokuViewModel() {
        // Post initial empty board or a loading state if generation is slow
        _sudokuBoard.postValue(currentPuzzleCells);
        _isSolved.postValue(false);
    }

    public void startNewGame(Difficulty difficulty) {
        _isSolved.postValue(false); // Reset solved state immediately
        _elapsedTime.postValue(0L); // Reset timer immediately
        _selectedCellLiveData.postValue(null); // Deselect any cell

        executorService.execute(() -> {
            // CRITICAL: Replace SudokuGeneratorPlaceholder with your actual robust SudokuGenerator
            // Your generator should handle creating a full valid Sudoku solution
            // and then removing cells to create a puzzle of the given difficulty.
            SudokuGeneratorPlaceholder generator = new SudokuGeneratorPlaceholder();
            int[][] puzzleArray;
            int[][] generatedSolution;

            int cellsToRemove = 35; // Default for Easy
            switch (difficulty) {
                case MEDIUM:
                    cellsToRemove = 45;
                    break;
                case HARD:
                    cellsToRemove = 55;
                    break;
                case EASY: // Fall-through or explicitly set
                default:
                    cellsToRemove = 35;
                    break;
            }

            generatedSolution = generator.generateFullBoard();
            puzzleArray = generator.createPuzzleFromSolution(generatedSolution, cellsToRemove);
            solution = generatedSolution; // Store the full solution

            SudokuCell[][] newBoard = new SudokuCell[9][9];
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    newBoard[r][c] = new SudokuCell(r, c, puzzleArray[r][c], puzzleArray[r][c] != 0);
                }
            }
            currentPuzzleCells = newBoard;

            _sudokuBoard.postValue(currentPuzzleCells); // Post value as we are on a background thread
            // TODO: Start timer logic here. Ensure it interacts with LiveData on the main thread
            // or uses postValue().
        });
    }

    public void selectCell(int row, int col) {
        if (row < 0 || row >= 9 || col < 0 || col >= 9) { // Boundary check for new selection
            // Optionally deselect current if any
            Pair<Integer, Integer> currentSelection = _selectedCellLiveData.getValue();
            if (currentSelection != null) {
                currentPuzzleCells[currentSelection.first][currentSelection.second].isSelected = false;
                _selectedCellLiveData.postValue(null); // Post value for observers
                _sudokuBoard.postValue(currentPuzzleCells); // Update board to reflect deselection
            }
            return;
        }

        Pair<Integer, Integer> currentSelection = _selectedCellLiveData.getValue();
        SudokuCell[][] boardToUpdate = currentPuzzleCells; // Work with a local reference

        // Deselect previous cell
        if (currentSelection != null) {
            if (currentSelection.first >= 0 && currentSelection.first < 9 &&
                    currentSelection.second >= 0 && currentSelection.second < 9) {
                boardToUpdate[currentSelection.first][currentSelection.second].isSelected = false;
            }
        }

        // Select new cell
        boardToUpdate[row][col].isSelected = true;
        _selectedCellLiveData.postValue(new Pair<>(row, col)); // Use postValue for LiveData if needed, setValue is fine if called from main
        _sudokuBoard.postValue(boardToUpdate); // Trigger UI update
    }

    public void inputNumber(int number) {
        Pair<Integer, Integer> selected = _selectedCellLiveData.getValue();
        if (selected == null || Boolean.TRUE.equals(_isSolved.getValue())) { // Also check if already solved
            return;
        }

        int row = selected.first;
        int col = selected.second;

        if (currentPuzzleCells[row][col].isStartingCell) {
            return;
        }

        currentPuzzleCells[row][col].value = number;
        currentPuzzleCells[row][col].isIncorrect = false; // Assume correct until validated
        _sudokuBoard.postValue(currentPuzzleCells); // Update board
        checkIfSolved(); // Check solution after input
    }

    public void clearSelectedCell() {
        Pair<Integer, Integer> selected = _selectedCellLiveData.getValue();
        if (selected == null || Boolean.TRUE.equals(_isSolved.getValue())) {
            return;
        }

        int row = selected.first;
        int col = selected.second;

        if (currentPuzzleCells[row][col].isStartingCell) {
            return;
        }
        currentPuzzleCells[row][col].value = 0;
        currentPuzzleCells[row][col].isIncorrect = false;
        _sudokuBoard.postValue(currentPuzzleCells);
        // No need to call checkIfSolved here, as clearing a cell cannot solve the puzzle.
        // If _isSolved was true, it should be set to false.
        if (Boolean.TRUE.equals(_isSolved.getValue())) {
            _isSolved.postValue(false);
        }
    }

    private void checkIfSolved() {
        // This method can be called from background threads (e.g., after hint)
        // or main thread (after inputNumber).
        // LiveData's postValue is thread-safe for updating its value.
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (currentPuzzleCells[r][c].value == 0 || currentPuzzleCells[r][c].value != solution[r][c]) {
                    _isSolved.postValue(false);
                    return;
                }
            }
        }
        _isSolved.postValue(true);
        // TODO: Stop timer logic here
    }

    public void validateBoard() {
        if (Boolean.TRUE.equals(_isSolved.getValue())) return; // No need to validate if already solved

        boolean allCorrectAndFilled = true;
        SudokuCell[][] boardToUpdate = currentPuzzleCells;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (boardToUpdate[r][c].value == 0) {
                    allCorrectAndFilled = false;
                    // boardToUpdate[r][c].isIncorrect = true; // Optional: mark empty cells as "incorrect" on explicit check
                } else if (boardToUpdate[r][c].value != solution[r][c]) {
                    boardToUpdate[r][c].isIncorrect = true;
                    allCorrectAndFilled = false;
                } else {
                    boardToUpdate[r][c].isIncorrect = false;
                }
            }
        }
        _sudokuBoard.postValue(boardToUpdate); // Update UI with incorrect flags

        if (allCorrectAndFilled) {
            _isSolved.postValue(true);
            // TODO: Stop timer
        } else {
            // If it's not solved after validation, ensure _isSolved is false
            // (it might have been true from a previous state before an error was introduced)
            _isSolved.postValue(false);
        }
    }

    public void provideHint() {
        if (Boolean.TRUE.equals(_isSolved.getValue())) return;

        executorService.execute(() -> {
            boolean hintProvided = false;
            // Create a copy to modify and then post, to avoid concurrent modification issues
            // if the main thread is also trying to read currentPuzzleCells while it's being iterated here.
            // However, since postValue takes care of delivering to main thread, direct modification
            // of currentPuzzleCells followed by postValue is generally okay for this structure.

            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    SudokuCell cell = currentPuzzleCells[r][c]; // Direct reference
                    if (cell.value == 0 && !cell.isHinted) {
                        cell.value = solution[r][c];
                        cell.isHinted = true;
                        cell.isStartingCell = true; // Treat hinted cells like starting cells (cannot be changed by user)
                        hintProvided = true;
                        break;
                    }
                }
                if (hintProvided) break;
            }

            if (hintProvided) {
                _sudokuBoard.postValue(currentPuzzleCells);
                checkIfSolved(); // This will run on the executorService thread, but _isSolved.postValue() is safe.
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        // TODO: Ensure any active timer is also stopped and resources released
    }

    // --- Placeholder for Sudoku Generation Logic ---
    // CRITICAL: Replace this with your actual SudokuGenerator class (Java)
    // This placeholder is NOT suitable for a real game.
    private static class SudokuGeneratorPlaceholder {
        private final Random random = new Random();

        public int[][] generateFullBoard() {
            // This needs a proper backtracking algorithm for a valid Sudoku.
            // Returning a pre-filled simple board for placeholder purposes.
            return new int[][]{
                    {5, 3, 4, 6, 7, 8, 9, 1, 2},
                    {6, 7, 2, 1, 9, 5, 3, 4, 8},
                    {1, 9, 8, 3, 4, 2, 5, 6, 7},
                    {8, 5, 9, 7, 6, 1, 4, 2, 3},
                    {4, 2, 6, 8, 5, 3, 7, 9, 1},
                    {7, 1, 3, 9, 2, 4, 8, 5, 6},
                    {9, 6, 1, 5, 3, 7, 2, 8, 4},
                    {2, 8, 7, 4, 1, 9, 6, 3, 5},
                    {3, 4, 5, 2, 8, 6, 1, 7, 9}
            };
        }

        public int[][] createPuzzleFromSolution(int[][] solutionBoard, int cellsToRemove) {
            int[][] puzzle = new int[9][9];
            for (int i = 0; i < 9; i++) {
                puzzle[i] = solutionBoard[i].clone();
            }

            int count = cellsToRemove;
            while (count > 0) {
                int r = random.nextInt(9);
                int c = random.nextInt(9);
                if (puzzle[r][c] != 0) {
                    puzzle[r][c] = 0;
                    count--;
                }
            }
            return puzzle;
        }
    }
}
