package net.minecraft.launcher.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;

public class TexturedPanel extends JPanel
{
    private static final Logger LOGGER;
    private static final long serialVersionUID = 1L;
    private Image image;
    private Image bgImage;
    
    public TexturedPanel(final String filename) {
        this.setOpaque(true);
        try {
            this.bgImage = ImageIO.read(TexturedPanel.class.getResource(filename)).getScaledInstance(32, 32, 16);
        }
        catch (IOException e) {
            TexturedPanel.LOGGER.error("Unexpected exception initializing textured panel", e);
        }
    }
    
    @Override
    public void update(final Graphics g) {
        this.paint(g);
    }
    
    public void paintComponent(final Graphics graphics) {
        final int width = this.getWidth() / 2 + 1;
        final int height = this.getHeight() / 2 + 1;
        if (this.image == null || this.image.getWidth(null) != width || this.image.getHeight(null) != height) {
            this.image = this.createImage(width, height);
            this.copyImage(width, height);
        }
        graphics.drawImage(this.image, 0, 0, width * 2, height * 2, null);
    }
    
    protected void copyImage(final int width, final int height) {
        final Graphics imageGraphics = this.image.getGraphics();
        for (int x = 0; x <= width / 32; ++x) {
            for (int y = 0; y <= height / 32; ++y) {
                imageGraphics.drawImage(this.bgImage, x * 32, y * 32, null);
            }
        }
        if (imageGraphics instanceof Graphics2D) {
            this.overlayGradient(width, height, (Graphics2D)imageGraphics);
        }
        imageGraphics.dispose();
    }
    
    protected void overlayGradient(final int width, final int height, final Graphics2D graphics) {
        final int gh = 1;
        graphics.setPaint(new GradientPaint(new Point2D.Float(0.0f, 0.0f), new Color(553648127, true), new Point2D.Float(0.0f, gh), new Color(0, true)));
        graphics.fillRect(0, 0, width, gh);
        graphics.setPaint(new GradientPaint(new Point2D.Float(0.0f, 0.0f), new Color(0, true), new Point2D.Float(0.0f, height), new Color(1610612736, true)));
        graphics.fillRect(0, 0, width, height);
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
}
