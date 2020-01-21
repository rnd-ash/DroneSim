package week3.dronesimulationjava;

import javafx.scene.image.Image;



/**
 * Abstract class for all Image classes for Sprites
 */
public abstract class SpriteImageClass {
    /** Boolean indicating if image has been loaded into memory yet **/
    protected boolean hasImage;

    /** Image object **/
    protected Image img;

    /** Name of sprite image source **/
    String imgName;

    /**
     * Gets the image name string from resources folder within JAR. Used for saving data of the sprite
     * @return String name of the image file used
     */
    public String getImageName() {
        return imgName;
    }

    /**
     * Gets an image from a name.
     * @param name Name of image to load from resource DIR
     * @return Image representing the resource.
     */
    public Image getImageFromName(String name) {
        hasImage = true;
        img = new Image(getClass().getResourceAsStream(name));
        return img;
    }

    /**
     * Gets a random image for a sprite on initial load.
     * @return Image (randomised) based on Inherited class' {@link #getImageAsString()} function
     */
    public Image getRandomImage() {
        this.imgName = getImageAsString();
        hasImage = true;
        img = new Image(getClass().getResourceAsStream(imgName));
        return img;
    }

    /**
     * Used to get the name of the image to load into a sprite. Here is where we can add random image loading
     * based on switch statements.
     * @return Image name from resource DIR.
     */
    public abstract String getImageAsString();
}
