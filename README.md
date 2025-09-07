SudokuGame 🎮

Project Summary:

SudokuGame is a feature-rich Android Sudoku app built with Java and MVVM architecture. It includes multiple difficulty levels, timer mode, hints, scoring, and uses a Backtracking algorithm for puzzle generation and solving, all wrapped in a clean Material Design UI.


✨ Features:

🎯 Three difficulty levels: Easy, Medium, and Hard

⏱️ Timer mode to track gameplay duration

🧮 Sudoku puzzle generation and solving using Backtracking Algorithm

💡 Hint feature to assist players in finding correct moves

🖌️ Clean and modern UI with Material Design

📱 Optimized for different screen sizes

💾 Save & Resume game functionality

🏆 Scoring system based on difficulty and time taken

🏗️ MVVM architecture for clean, maintainable code



🏗️ Architecture:

The project follows MVVM architecture:

Model → Represents the Sudoku board, puzzle logic, timer, scoring, and hints

View → XML layouts for Level Selection, Sudoku grid, timer, and game result

ViewModel → Connects UI with game logic, handles puzzle generation, solving, timer, scoring, and hints


🔢 Core Algorithm:

Backtracking Algorithm is used for:

Sudoku puzzle generation ensuring unique, valid boards

Efficient puzzle solving and validation

Timer Logic tracks elapsed time for scoring

Hint Logic suggests correct numbers for incomplete or wrong moves

Scoring System calculates scores based on difficulty level and completion time



🚀 How to Run:

Clone the repository:

git clone https://github.com/swasthih/SudokuGame.git

Open the project in Android Studio

Sync Gradle and build the project

Run on an emulator or physical device



🛠️ Tech Stack & Frameworks:

Language: Java

IDE: Android Studio

UI: XML + Material Design

Architecture: MVVM

Algorithm: Backtracking for puzzle generation and solving

Frameworks / Libraries Used:

AndroidX (AppCompat, ConstraintLayout, RecyclerView, etc.)

Material Components for modern UI elements

LiveData & ViewModel for reactive UI updates

SharedPreferences for save/resume functionality
