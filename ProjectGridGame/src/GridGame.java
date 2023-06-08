import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GridGame extends JPanel implements KeyListener {
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 30;
    private static final int CELL_SIZE = 50;
    private static final int TILE_SIZE = 16;
    private static final int VIEWPORT_SIZE = 10;
    private static final int VIEWPORT_SIZE_WIDTH = 14;
    private static final int VIEWPORT_SIZE_HEIGHT = 12;

    private int playerRow;
    private int playerCol;
    private List<CollectableItem> collectableItems;
    private List<Enemy> enemies;
    private int collectedGold;
    private GoldCounter goldCounter;

    private BufferedImage playerImage;
    private BufferedImage enemyImage;
    private BufferedImage collectableItemImage;
    private BufferedImage floor;
    private int viewportRow;
    private int viewportCol;

    public GridGame() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        collectableItems = new ArrayList<>();
        enemies = new ArrayList<>();
        collectedGold = 0;

        playerRow = GRID_HEIGHT / 2;
        playerCol = GRID_WIDTH / 2;

        spawnCollectableItems(10);
        spawnEnemies(10);

        goldCounter = new GoldCounter();

        setLayout(new BorderLayout());
        add(goldCounter, BorderLayout.EAST);

        SpriteSheetLoader spriteSheetLoader = new SpriteSheetLoader();
        try {
            // Load custom sprites from sprite sheet
            BufferedImage spriteSheetChar = spriteSheetLoader.loadSpriteSheet("Resources/Dungeon_Character_2.png");
            BufferedImage spriteSheet = spriteSheetLoader.loadSpriteSheet("Resources/Dungeon_Tileset.png");
            playerImage = spriteSheetLoader.loadSprite(spriteSheetChar, 0, 0, TILE_SIZE, TILE_SIZE);
            enemyImage = spriteSheetLoader.loadSprite(spriteSheetChar, TILE_SIZE*4, TILE_SIZE, TILE_SIZE, TILE_SIZE);
            collectableItemImage = spriteSheetLoader.loadSprite(spriteSheet, TILE_SIZE*6, TILE_SIZE*8, TILE_SIZE, TILE_SIZE);
            floor = spriteSheetLoader.loadSprite(spriteSheet, TILE_SIZE*9, TILE_SIZE*7, TILE_SIZE, TILE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateViewport();
    }

    private void updateViewport() {
        viewportRow = playerRow - VIEWPORT_SIZE_WIDTH / 2;
        viewportCol = playerCol - VIEWPORT_SIZE_HEIGHT / 2;

        if (viewportRow < 0) {
            viewportRow = 0;
        } else if (viewportRow + VIEWPORT_SIZE > GRID_HEIGHT) {
            viewportRow = GRID_HEIGHT - VIEWPORT_SIZE_HEIGHT;
        }

        if (viewportCol < 0) {
            viewportCol = 0;
        } else if (viewportCol + VIEWPORT_SIZE > GRID_WIDTH) {
            viewportCol = GRID_WIDTH - VIEWPORT_SIZE_WIDTH;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if ((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) && playerRow > 0) {
            playerRow--;
        } else if ((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) && playerRow < GRID_HEIGHT - 1) {
            playerRow++;
        } else if ((keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) && playerCol > 0) {
            playerCol--;
        } else if ((keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) && playerCol < GRID_WIDTH - 1) {
            playerCol++;
        }

        handleCollectableItemCollision();
        handleEnemyCollision();

        updateViewport();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void spawnCollectableItems(int numItems) {
        Random random = new Random();

        for (int i = 0; i < numItems; i++) {
            int row, col;
            do {
                row = random.nextInt(GRID_HEIGHT);
                col = random.nextInt(GRID_WIDTH);
            } while (isOccupied(row, col));

            collectableItems.add(new CollectableItem(row, col));
        }
    }

    private boolean isOccupied(int row, int col) {
        if (playerRow == row && playerCol == col) {
            return true;
        }

        for (CollectableItem item : collectableItems) {
            if (item.getRow() == row && item.getCol() == col) {
                return true;
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.getRow() == row && enemy.getCol() == col) {
                return true;
            }
        }

        return false;
    }

    private void handleCollectableItemCollision() {
        Iterator<CollectableItem> iterator = collectableItems.iterator();
        while (iterator.hasNext()) {
            CollectableItem item = iterator.next();
            if (playerRow == item.getRow() && playerCol == item.getCol()) {
                iterator.remove();
                collectedGold++;
                goldCounter.updateGoldCount();
            }
        }
        if (collectableItems.isEmpty()) {
            spawnCollectableItems(5);
        }
    }

    private void spawnEnemies(int numEnemies) {
        Random random = new Random();

        for (int i = 0; i < numEnemies; i++) {
            int row, col;
            do {
                row = random.nextInt(GRID_HEIGHT);
                col = random.nextInt(GRID_WIDTH);
            } while (isOccupied(row, col));

            enemies.add(new Enemy(row, col));
        }
    }

    private void handleEnemyCollision() {
        for (Enemy enemy : enemies) {
            if (playerRow == enemy.getRow() && playerCol == enemy.getCol()) {
                if (collectedGold > 0) {
                    collectedGold--;
                    goldCounter.updateGoldCount();
                }

                int newRow, newCol;
                do {
                    newRow = new Random().nextInt(GRID_HEIGHT);
                    newCol = new Random().nextInt(GRID_WIDTH);
                } while (isOccupied(newRow, newCol));

                enemy.move(newRow, newCol);
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw grid
        for (int row = viewportRow; row < viewportRow + VIEWPORT_SIZE_HEIGHT; row++) {
            for (int col = viewportCol; col < viewportCol + VIEWPORT_SIZE_WIDTH; col++) {
                int x = (col - viewportCol) * CELL_SIZE;
                int y = (row - viewportRow) * CELL_SIZE;
                //g.setColor(Color.LIGHT_GRAY);
                //g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                g.drawImage(floor, x, y, CELL_SIZE, CELL_SIZE, null);
            }
        }

        // Draw player
        int playerX = (playerCol - viewportCol) * CELL_SIZE;
        int playerY = (playerRow - viewportRow) * CELL_SIZE;
        //g.setColor(Color.BLUE);
        //g.fillOval(playerX + CELL_SIZE / 6, playerY + CELL_SIZE / 6, CELL_SIZE * 2 / 3, CELL_SIZE * 2 / 3);
        g.drawImage(playerImage, playerX, playerY, CELL_SIZE, CELL_SIZE, null);

        // Draw collectable items
        //g.setColor(Color.YELLOW);
        for (CollectableItem item : collectableItems) {
            int x = (item.getCol() - viewportCol) * CELL_SIZE;
            int y = (item.getRow() - viewportRow) * CELL_SIZE;
            //g.fillOval(x + CELL_SIZE / 6, y + CELL_SIZE / 6, CELL_SIZE * 2 / 3, CELL_SIZE * 2 / 3);
            g.drawImage(collectableItemImage, x, y, CELL_SIZE, CELL_SIZE, null);
        }

        // Draw enemies
        //g.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            int x = (enemy.getCol() - viewportCol) * CELL_SIZE;
            int y = (enemy.getRow() - viewportRow) * CELL_SIZE;
            //g.fillOval(x + CELL_SIZE / 6, y + CELL_SIZE / 6, CELL_SIZE * 2 / 3, CELL_SIZE * 2 / 3);
            g.drawImage(enemyImage, x, y, CELL_SIZE, CELL_SIZE, null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Grid Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new GridGame());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static class CollectableItem {
        private int row;
        private int col;

        public CollectableItem(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }

    private static class Enemy {
        private int row;
        private int col;

        public Enemy(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public void move(int newRow, int newCol) {
            row = newRow;
            col = newCol;
        }
    }

    private class GoldCounter extends JPanel {
        private JLabel goldCountLabel;

        public GoldCounter() {
            setPreferredSize(new Dimension(100, 600));
            setLayout(new BorderLayout());

            goldCountLabel = new JLabel("Gold: " + collectedGold);
            goldCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
            goldCountLabel.setFont(new Font("Arial", Font.BOLD, 20));
            add(goldCountLabel, BorderLayout.NORTH);
        }

        public void updateGoldCount() {
            goldCountLabel.setText("Gold: " + collectedGold);
        }
    }
}
