import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SpriteSheetLoader {
    public BufferedImage loadSpriteSheet(String filename) throws IOException {
        return ImageIO.read(new File(filename));
    }

    public BufferedImage loadSprite(BufferedImage spriteSheet, int x, int y, int width, int height) {
        return spriteSheet.getSubimage(x, y, width, height);
    }
}
