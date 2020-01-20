package week3.dronesimulationjava;

import javafx.scene.image.Image;


/**
 * Abstract class for all Image classes for Sprites
 */
public abstract class SpriteImageClass {
    protected boolean hasImage;
    protected Image img;
    String imgName;
    public String getImageName() {
        return imgName;
    }

    public Image getImageFromName(String name) {
        hasImage = true;
        img = new Image(getClass().getResourceAsStream(name));
        return img;
    }

    public Image getRandomImage() {
        this.imgName = getImageAsString();
        hasImage = true;
        img = new Image(getClass().getResourceAsStream(imgName));
        return img;
    }

    public abstract String getImageAsString();

}
