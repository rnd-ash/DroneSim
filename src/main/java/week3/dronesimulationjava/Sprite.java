package week3.dronesimulationjava;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


/**
 * Sprite base class
 * Implemented by all Sprites within the game.
 *
 * The class contains core functions for animation and updating the UI of the game with sprite positions.
 *
 * @author Ashcon Mohseninia
 * @since 1.0
 */
public abstract class Sprite {
    /** Maximum velocity of all sprites in pixels per frame **/
    static double MAX_VELOCITY = 2;

    Image image;
    ImageView imageView;

    /** Unique ID of the sprite **/
    UUID uuid;

    /** Layer Sprite should be a part of **/
    Pane layer;

    // Top left corner X of sprite
    protected double x;
    // Top left corner Y of sprite
    protected double y;

    /** Sprites velocity in Y axis **/
    double dy;

    /** Sprites velocity in X anxis **/
    double dx;

    /** Width of sprite in pixels **/
    protected double width;

    /** Height of sprite in pixels **/
    protected double height;

    // Last time in MS that animation frame change was done
    private long lastAnimationMillis;

    // Number of animation frames in the sprite's image (Vertically stacked)
    private int animation_stages;

    // Current frame number of animation being shown on screen
    private int frame_number;

    // Animation Interval in MS Based on animation_fps
    private int animation_frame_interval_ms;

    /** Rotation of sprite in degrees (0 = Vertically) **/
    protected double rotation;

    // Debug boundary rectangle
    private Rectangle rect;

    // Debug boolean if boundary is currently being shown on screen
    protected boolean boundaryShown = false;

    protected SpriteImageClass sprite_image;

    /**
     * Sprite class constructor
     * @param layer Layer to display the sprite on
     * @param x Initial X coords of the sprite
     * @param y Initial Y coords of the sprite
     * @param r Initial Rotation (degrees) of the sprite
     * @param animation_stage Number of frames in the animation for sprite. If static animation, use 1
     * @param animation_fps FPS to play the sprite animation at
     */
    public Sprite(Pane layer, double x, double y, double r, int animation_stage, int animation_fps) {
        this.layer = layer;
        this.rotation = r;
        frame_number = 0;
        this.x = x;
        this.y = y;
        this.uuid = UUID.randomUUID();
        animation_stages = animation_stage-1;
        animation_frame_interval_ms = 1000 / animation_fps;
    }

    public Sprite(Pane layer, JSONObject j) throws JSONException {
        this.layer = layer;
        this.animation_stages = j.getInt("Animation_stages");
        //this.anim
    }

    /**
     * Sets image data for sprite. This automatically calculates the width and height of the sprite,
     * as well as doing the initial relocate / rotate command.
     * @param img Image to set
     */
    public void setImageData(SpriteImageClass img, JSONObject j) throws JSONException {
        this.sprite_image = img;
        if (j == null) {
            this.image = img.getRandomImage();
        } else {
            this.image = img.getImageFromName(j.getString("Image_name"));
        }
        this.imageView = new ImageView(image);
        this.width = image.getWidth();
        this.height = this.image.getHeight() / (animation_stages+1);
        this.imageView.relocate(this.x, this.y);
        this.layer.getChildren().add(this.imageView);
        lastAnimationMillis = System.currentTimeMillis();
        this.imageView.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            onClicked();
            event.consume();
        });
    }

    public void addToJson(JSONObject j) throws JSONException {
        j.put("Class_Name", this.getClass().getCanonicalName());
        j.put("x_coord", x);
        j.put("y_coord", y);
        j.put("x_veloc", dx);
        j.put("y_veloc", dy);
        j.put("Rotation", rotation);
        j.put("Animation_stages", animation_stages);
        j.put("Animation_Interval_MS", animation_frame_interval_ms);
        j.put("UUID", uuid);
        j.put("Image_name", sprite_image.getImageName());
    }

    /**
     * Returns a JSON Object of the sprite representing various attributes
     * @return JsonObject of Sprites metadata
     */
    public final JSONObject toJson() throws JSONException {
        JSONObject j = new JSONObject();
        addToJson(j);
        return j;
    }

    /**
     * Function to run if the sprite is clicked on with the mouse
     */
    public abstract void onClicked();

    /**
     * Boolean to indicate if a collision has occurred between this and another sprite
     * @param x Sprite to check against
     * @return Boolean if this sprite collides with sprite [x]
     */
    public boolean collides(Sprite x) {
        return x.getBounds().intersects(this.getBounds());
    }

    /**
     * Gets a 2D Bouncing box of the sprite
     * @return Rectangle2D of the sprite representing its bouncing box
     */
    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    /**
     * Check if the sprite collides with a point range on the screen
     * @param x Top left X coordinate
     * @param y Top left Y coordinate
     * @param width Width of boundary to check
     * @param height Height out boundary to check
     * @return Boolean indicating if the sprite intercepts the area
     */
    public boolean collidesWithPoints(double x, double y, double width, double height) {
        return new Rectangle2D(x, y, width, height).intersects(this.getBounds());
    }

    /**
     * Called every frame. Used to move the sprite on the grid by changing its dy and dx values.
     */
    public void move() {
        if (this.x < 0-this.width) {
            this.x = DroneUI.ARENA_MAX_WIDTH;
        } else if (this.x > DroneUI.ARENA_MAX_WIDTH+this.width) {
            this.x = 0;
        }

        if (this.y < 0-this.height) {
            this.y = DroneUI.ARENA_MAX_HEIGHT;
        } else if (this.y > DroneUI.ARENA_MAX_HEIGHT+this.height) {
            this.y = 0;
        }
    }

    /**
     * Gets Center X coordinate of sprite relative to layout
     * @return Center X point
     */
    public double getCenterX() {
        return this.x + (this.width/2);
    }

    /**
     * Gets center Y coordinate of sprite relative to layout
     * @return Center Y point
     */
    public double getCenterY() {
        return this.y + (this.height/2);
    }

    /**
     * Updates the UI with new image data based on animation stage, and new location of the sprite on grid
     * after move {@link #move()} was called
     */
    public void updateUI() {

        // Clamp velocities to Maximum possible (in both + and - directions)
        if (this.dy > MAX_VELOCITY) {
            this.dy = MAX_VELOCITY;
        } else if (this.dy < -MAX_VELOCITY) {
            this.dy = -MAX_VELOCITY;
        }

        if (this.dx > MAX_VELOCITY) {
            this.dx = MAX_VELOCITY;
        } else if (this.dx < -MAX_VELOCITY) {
            this.dx =-MAX_VELOCITY;
        }

        // Update the X and Y of the sprite based on their velocities, as well as their rotations
        this.x += this.dx;
        this.y += this.dy;
        this.imageView.relocate(this.x, this.y);
        this.imageView.setRotate(rotation);

        // Update viewport based on animation phase
        this.imageView.setViewport(new Rectangle2D(0, frame_number * height ,width, height));
        if (animation_stages > 1) {
            // Time to update the sprite with the next frame of its animation
            if (System.currentTimeMillis() - lastAnimationMillis > animation_frame_interval_ms) {
                lastAnimationMillis = System.currentTimeMillis();
                if (frame_number == animation_stages) {
                    frame_number = 0;
                }
                frame_number++;
            }
        }

        // If debug boundary is enabled, update the location and rotation of that as well
        if (boundaryShown) {
            rect.relocate(this.x, this.y);
            rect.setRotate(rotation);
        }
    }

    /**
     * Removes sprite from UI
     */
    public void remove() {
        onRemove();
        this.layer.getChildren().remove(this.imageView);
    }

    /**
     * Debugging method that shows the bouncing box of the sprite on screen as a red rectangle.
     */
    public void showBoundary() {
        rect = new Rectangle();
        rect.setStroke(Color.RED);
        rect.setStrokeType(StrokeType.INSIDE);
        rect.setStrokeWidth(1);
        rect.setWidth(getBounds().getWidth());
        rect.setHeight(getBounds().getHeight());
        rect.setFill(Color.TRANSPARENT);
        this.layer.getChildren().add(rect);
        this.boundaryShown = true;
    }

    /**
     * Debug method to hide the bouncing box of the sprite.
     */
    public void hideBoundary() {
        if (rect != null) {
            this.layer.getChildren().remove(rect);
        }
        this.boundaryShown = false;
    }

    /**
     * Called when the Sprite is removed from the screen.
     */
    public void onRemove() {
        hideBoundary();
        this.layer.getChildren().remove(this.imageView);
    }

    /**
     * Called when the Sprite collides with another Sprite
     * @param hit The sprite that this collided with
     */
    public abstract void onCollides(Sprite hit);
}
