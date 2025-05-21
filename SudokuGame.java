import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SudokuGame extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private int[][] board = new int[9][9];
    private int[][] solution = new int[9][9];
    private boolean[][] fixedCells = new boolean[9][9];

    public SudokuGame() {
        setTitle("Sudoku Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Create main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Title panel with title and close button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("Sudoku Solver", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 16));
        closeButton.setBackground(new Color(33, 150, 243));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(new Color(66, 165, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(new Color(33, 150, 243));
            }
        });
        closeButton.addActionListener(e -> System.exit(0));
        titlePanel.add(closeButton, BorderLayout.EAST);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Sudoku grid panel
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        gridPanel.setBackground(new Color(245, 245, 245));

        // Create 3x3 sub-grids
        for (int blockRow = 0; blockRow < 3; blockRow++) {
            for (int blockCol = 0; blockCol < 3; blockCol++) {
                JPanel subGrid = new JPanel(new GridLayout(3, 3, 2, 2));
                subGrid.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
                subGrid.setBackground(new Color(255, 255, 255));

                // Add cells to sub-grid
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int row = blockRow * 3 + i;
                        int col = blockCol * 3 + j;
                        JTextField cell = new JTextField();
                        cell.setHorizontalAlignment(JTextField.CENTER);
                        cell.setFont(new Font("Arial", Font.BOLD, 20));
                        cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                        cell.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                char c = e.getKeyChar();
                                if (!Character.isDigit(c) || c < '1' || c > '9') {
                                    e.consume();
                                }
                            }
                        });
                        cells[row][col] = cell;
                        subGrid.add(cell);
                    }
                }
                gridPanel.add(subGrid);
            }
        }
        mainPanel.add(gridPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        JButton solveButton = new JButton("Solve");
        JButton clearButton = new JButton("Clear");
        JButton newPuzzleButton = new JButton("New Puzzle");
        JButton checkButton = new JButton("Check");

        // Button styling
        for (JButton button : new JButton[]{solveButton, clearButton, newPuzzleButton, checkButton}) {
            button.setFont(new Font("Arial", Font.PLAIN, 16));
            button.setBackground(new Color(33, 150, 243));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(66, 165, 245));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(33, 150, 243));
                }
            });
        }

        solveButton.addActionListener(e -> solvePuzzle());
        clearButton.addActionListener(e -> clearBoard());
        newPuzzleButton.addActionListener(e -> generateNewPuzzle());
        checkButton.addActionListener(e -> checkUserInput());

        buttonPanel.add(solveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(newPuzzleButton);
        buttonPanel.add(checkButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        generateNewPuzzle();
    }

    private void generateNewPuzzle() {
        // Clear board
        clearBoard();
        // Generate a full valid Sudoku board
        fillBoard();
        // Copy to solution
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, solution[i], 0, 9);
        }
        // Remove some numbers to create puzzle
        removeNumbers(40); // Adjust difficulty by changing number of removed cells
        updateUIFromBoard();
    }

    private void fillBoard() {
        Random rand = new Random();
        // Fill diagonal 3x3 blocks
        for (int i = 0; i < 9; i += 3) {
            fillBox(i, i, rand);
        }
        // Fill remaining cells
        fillRemaining(0, 3);
    }

    private void fillBox(int row, int col, Random rand) {
        int num;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                do {
                    num = rand.nextInt(9) + 1;
                } while (!isValid(row + i, col + j, num));
                board[row + i][col + j] = num;
                fixedCells[row + i][col + j] = true; // Mark initial values
            }
        }
    }

    private boolean fillRemaining(int row, int col) {
        if (col >= 9 && row < 8) {
            row++;
            col = 0;
        }
        if (row >= 9 && col >= 9) {
            return true;
        }
        if (row < 3) {
            if (col < 3) {
                col = 3;
            }
        } else if (row < 6) {
            if (col == (row / 3) * 3) {
                col += 3;
            }
        } else {
            if (col == 6) {
                row++;
                col = 0;
                if (row >= 9) {
                    return true;
                }
            }
        }
        for (int num = 1; num <= 9; num++) {
            if (isValid(row, col, num)) {
                board[row][col] = num;
                fixedCells[row][col] = true; // Mark initial values
                if (fillRemaining(row, col + 1)) {
                    return true;
                }
                board[row][col] = 0;
                fixedCells[row][col] = false;
            }
        }
        return false;
    }

    private void removeNumbers(int count) {
        Random rand = new Random();
        while (count > 0) {
            int row = rand.nextInt(9);
            int col = rand.nextInt(9);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                fixedCells[row][col] = false;
                count--;
            }
        }
    }

    private boolean isValid(int row, int col, int num) {
        // Check row
        for (int x = 0; x < 9; x++) {
            if (board[row][x] == num) {
                return false;
            }
        }
        // Check column
        for (int x = 0; x < 9; x++) {
            if (board[x][col] == num) {
                return false;
            }
        }
        // Check 3x3 box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private void solvePuzzle() {
        // Copy current board state
        updateBoardFromUI();
        // Solve
        if (solveSudoku()) {
            updateUIFromBoard();
            JOptionPane.showMessageDialog(this, "Puzzle solved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No solution exists!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean solveSudoku() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku()) {
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

    private void checkUserInput() {
        updateBoardFromUI();
        boolean isCorrect = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Check all cells, including empty ones
                if (board[i][j] == 0 || board[i][j] != solution[i][j]) {
                    isCorrect = false;
                    if (board[i][j] != 0 || !fixedCells[i][j]) { // Highlight non-fixed empty or incorrect cells
                        cells[i][j].setBackground(new Color(255, 204, 204)); // Light red for incorrect/empty
                    }
                } else {
                    if (!fixedCells[i][j]) { // Reset non-fixed correct cells
                        cells[i][j].setBackground(Color.WHITE);
                    }
                }
            }
        }
        if (isCorrect) {
            int result = JOptionPane.showOptionDialog(
                this,
                "Congratulations! Your solution is correct!",
                "Success",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"New Game", "Close"},
                "New Game"
            );
            if (result == JOptionPane.YES_OPTION) {
                generateNewPuzzle();
            } else if (result == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        } else {
            int result = JOptionPane.showOptionDialog(
                this,
                "Some entries are incorrect or empty. Would you like to go back or see the solution?",
                "Incorrect",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Go Back", "Solve Again"},
                "Go Back"
            );
            if (result == JOptionPane.NO_OPTION) {
                solvePuzzle();
            }
        }
    }

    private void updateBoardFromUI() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!fixedCells[i][j]) { // Only update non-fixed cells
                    String text = cells[i][j].getText().trim();
                    if (text.isEmpty()) {
                        board[i][j] = 0;
                    } else {
                        try {
                            int num = Integer.parseInt(text);
                            board[i][j] = (num >= 1 && num <= 9) ? num : 0;
                        } catch (NumberFormatException e) {
                            board[i][j] = 0;
                        }
                    }
                }
            }
        }
    }

    private void updateUIFromBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setBackground(Color.WHITE);
                    cells[i][j].setForeground(new Color(33, 33, 33));
                } else {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                    if (fixedCells[i][j]) {
                        cells[i][j].setEditable(false);
                        cells[i][j].setBackground(new Color(220, 220, 220));
                        cells[i][j].setForeground(new Color(0, 0, 0));
                    } else {
                        cells[i][j].setEditable(true);
                        cells[i][j].setBackground(Color.WHITE);
                        cells[i][j].setForeground(new Color(33, 33, 33));
                    }
                }
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = 0;
                fixedCells[i][j] = false;
                cells[i][j].setText("");
                cells[i][j].setEditable(true);
                cells[i][j].setBackground(Color.WHITE);
                cells[i][j].setForeground(new Color(33, 33, 33));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SudokuGame().setVisible(true);
        });
    }
}
