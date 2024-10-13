import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HangmanGame extends Application {

    // GUI Components
    private Label wordDisplay;    // Display the word with dashes for unguessed letters
    private TextField guessInput; // Input field for the user's guess
    private Label messageLabel;   // Display messages (e.g., correct/incorrect guess, game status)
    private Label attemptsLabel;  // Display remaining attempts
    private Label guessedLettersLabel; // Display the letters guessed so far
    private Label difficultyLabel; // Display the selected difficulty level
    private Label playerScoreLabel; // Display the player's score
    private Label hangmanVisual; // Simple visual representation of the hangman

    // Game Logic
    private HangmanLogic gameLogic;

    // Word list for random word selection
    private WordList wordList;

    // Scoreboard and Player
    private Scoreboard scoreboard;
    private Player currentPlayer;

    // Current difficulty level
    private String difficulty = "medium";  // Default to "medium"

    // Set to track guessed letters
    private Set<Character> guessedLetters = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize the scoreboard
            scoreboard = new Scoreboard();

            // Validate the player name
            String playerName = "Player1"; // Fixed name for simplicity
            if (playerName == null || playerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Player name cannot be empty.");
            }
            currentPlayer = new Player(playerName);

            // Initialize the word list with words for each difficulty level
            wordList = new WordList(
                    Arrays.asList("CAT", "DOG", "BALL"),
                    Arrays.asList("APPLE", "ORANGE", "BANANA"),
                    Arrays.asList("ELEPHANT", "CROCODILE", "HIPPOPOTAMUS")
            );

            // Set up the GUI and gameplay components
            initializeGameComponents(primaryStage);

        } catch (IllegalArgumentException e) {
            showErrorAlert("Error", e.getMessage());
        }
    }

    // Initialize all GUI components and game logic
    private void initializeGameComponents(Stage primaryStage) {
        try {
            // Select a random word from the word list based on the difficulty level
            String randomWord = wordList.getRandomWord(difficulty);
            gameLogic = new HangmanLogic(randomWord);

            // Set up the word display (initially with underscores)
            wordDisplay = new Label(gameLogic.getDisplayedWord());
            wordDisplay.setStyle("-fx-font-size: 24px;");

            // Set up the input field for guesses
            guessInput = new TextField();
            guessInput.setPromptText("Enter a letter");
            guessInput.setMaxWidth(50);

            // Set up message label for game status
            messageLabel = new Label("Start guessing!");

            // Set up attempts remaining label
            attemptsLabel = new Label("Attempts remaining: " + gameLogic.getRemainingAttempts());

            // Set up guessed letters label
            guessedLettersLabel = new Label("Guessed letters: ");

            // Set up player score label
            playerScoreLabel = new Label("Score: " + currentPlayer.getScore());

            // Set up difficulty label
            difficultyLabel = new Label("Difficulty: " + difficulty);

            // Placeholder for hangman visual
            hangmanVisual = new Label("Hangman: ");

            // Create buttons for difficulty selection and gameplay
            createDifficultyButtons(primaryStage);

        } catch (Exception e) {
            showErrorAlert("Error", "An error occurred while starting the game: " + e.getMessage());
        }
    }

    // Create buttons for selecting difficulty and submitting guesses
    private void createDifficultyButtons(Stage primaryStage) {
        Button easyButton = new Button("Easy");
        Button mediumButton = new Button("Medium");
        Button hardButton = new Button("Hard");

        easyButton.setOnAction(e -> setDifficulty("easy"));
        mediumButton.setOnAction(e -> setDifficulty("medium"));
        hardButton.setOnAction(e -> setDifficulty("hard"));

        Button submitButton = new Button("Submit Guess");
        Button restartButton = new Button("Restart Game");

        submitButton.setOnAction(e -> handleGuess());
        restartButton.setOnAction(e -> restartGame());

        HBox difficultyButtons = new HBox(10, easyButton, mediumButton, hardButton);
        difficultyButtons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, wordDisplay, guessInput, submitButton, messageLabel, attemptsLabel, guessedLettersLabel, hangmanVisual, playerScoreLabel, difficultyLabel, difficultyButtons, restartButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20px;");

        Scene scene = new Scene(layout, 400, 500);
        primaryStage.setTitle("Hangman Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Handle guess input
    private void handleGuess() {
        try {
            String guess = guessInput.getText().toUpperCase();
            if (guess.isEmpty() || guess.length() != 1 || !Character.isLetter(guess.charAt(0))) {
                throw new IllegalArgumentException("Please enter a valid letter.");
            }

            char guessedChar = guess.charAt(0);
            if (gameLogic.isLetterGuessed(guessedChar)) {
                messageLabel.setText("You've already guessed that letter.");
                guessInput.clear();
                return;
            }

            // Add the guessed letter to the set and update the label
            guessedLetters.add(guessedChar);
            guessedLettersLabel.setText("Guessed letters: " + guessedLetters.toString());

            boolean correctGuess = gameLogic.guessLetter(guessedChar);
            if (correctGuess) {
                messageLabel.setText("Correct!");
            } else {
                messageLabel.setText("Incorrect! Try again.");
            }

            wordDisplay.setText(gameLogic.getDisplayedWord());
            attemptsLabel.setText("Attempts remaining: " + gameLogic.getRemainingAttempts());

            // Update the visual hangman
            updateHangmanVisual();

            checkGameStatus();
            guessInput.clear();

        } catch (IllegalArgumentException e) {
            showErrorAlert("Invalid Guess", e.getMessage());
        }
    }

    // Update the simple visual representation of the hangman
    private void updateHangmanVisual() {
        int remainingAttempts = gameLogic.getRemainingAttempts();
        String hangmanState = switch (remainingAttempts) {
            case 5 -> "Hangman: O";      // Head
            case 4 -> "Hangman: O\n|";    // Body
            case 3 -> "Hangman: O\n/|";   // One arm
            case 2 -> "Hangman: O\n/|\\ "; // Two arms
            case 1 -> "Hangman: O\n/|\\ \n/ "; // One leg
            case 0 -> "Hangman: O\n/|\\ \n/ \\ "; // Two legs (Game over)
            default -> "Hangman: ";
        };
        hangmanVisual.setText(hangmanState);
    }

    // Check if the game is won or lost
    private void checkGameStatus() {
        if (gameLogic.isGameWon()) {
            messageLabel.setText("You won!");
            guessInput.setDisable(true);

            currentPlayer.incrementScore();
            scoreboard.updateScore(currentPlayer, currentPlayer.getScore());
            playerScoreLabel.setText("Score: " + currentPlayer.getScore());

        } else if (gameLogic.isGameOver()) {
            messageLabel.setText("Game over! The word was: " + gameLogic.getSecretWord());
            guessInput.setDisable(true);
        }
    }

    // Restart the game
    private void restartGame() {
        try {
            guessedLetters.clear(); // Clear guessed letters
            guessedLettersLabel.setText("Guessed letters: ");
            String randomWord = wordList.getRandomWord(difficulty);
            gameLogic = new HangmanLogic(randomWord);
            wordDisplay.setText(gameLogic.getDisplayedWord());
            messageLabel.setText("Game restarted. Start guessing!");
            guessInput.clear();
            guessInput.setDisable(false);
            attemptsLabel.setText("Attempts remaining: " + gameLogic.getRemainingAttempts());
            hangmanVisual.setText("Hangman: "); // Reset hangman visual
        } catch (Exception e) {
            showErrorAlert("Error", "An error occurred while restarting the game: " + e.getMessage());
        }
    }

    // Set the difficulty and restart the game
    private void setDifficulty(String difficultyLevel) {
        difficulty = difficultyLevel;
        difficultyLabel.setText("Difficulty: " + difficultyLevel);
        restartGame();
    }

    // Show error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
