package com.example.sudokugame;

import java.util.Random;

public class SudokuGenerator {
    private static final int GRID_SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private final Random random = new Random();

    // Generate Sudoku puzzle based on difficulty
    public int[][] generatePuzzle(Difficulty difficulty) {
        int[][] board = new int[GRID_SIZE][GRID_SIZE];

        // Step 1: Generate solved board
        fillBoard(board);

        // Step 2: Remove numbers based on difficulty
        int cellsToRemove = getCellsToRemove(difficulty);
        removeNumbers(board, cellsToRemove);

        return board;
    }

    // Fills the board with a complete valid Sudoku
    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= GRID_SIZE; num++) {
                        int candidate = (num + random.nextInt(GRID_SIZE)) % GRID_SIZE + 1;
                        if (isValid(board, row, col, candidate)) {
                            board[row][col] = candidate;
                            if (fillBoard(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // Check if placing a number is valid
    private boolean isValid(int[][] board, int row, int col, int num) {
        // Row
        for (int c = 0; c < GRID_SIZE; c++) {
            if (board[row][c] == num) return false;
        }
        // Column
        for (int r = 0; r < GRID_SIZE; r++) {
            if (board[r][col] == num) return false;
        }
        // 3x3 subgrid
        int startRow = row - row % SUBGRID_SIZE;
        int startCol = col - col % SUBGRID_SIZE;
        for (int r = startRow; r < startRow + SUBGRID_SIZE; r++) {
            for (int c = startCol; c < startCol + SUBGRID_SIZE; c++) {
                if (board[r][c] == num) return false;
            }
        }
        return true;
    }

    // Decide how many cells to remove based on difficulty
    private int getCellsToRemove(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return 35; // ~46 clues
            case MEDIUM:
                return 45; // ~36 clues
            case HARD:
                return 55; // ~26 clues
            default:
                return 40;
        }
    }

    // Remove cells randomly
    private void removeNumbers(int[][] board, int cellsToRemove) {
        int removed = 0;
        while (removed < cellsToRemove) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                removed++;
            }
        }
    }

    // Utility: print board to log (for debugging)
    public void printBoard(int[][] board) {
        for (int r = 0; r < GRID_SIZE; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < GRID_SIZE; c++) {
                row.append(board[r][c]).append(" ");
            }
            System.out.println(row.toString());
        }
    }
}
