package week3.dronesimulationjava;


import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Random;

public class DroneSprite extends Sprite {

    // Debug rings for range collision detections (F3 activates these)
    Circle view_low;
    Circle view_med;
    Circle view_high;

    Drone d;
    Random rand;

    // View ring object lists. These fill up depending on what Objects the drones see in their radar rings
    private ArrayList<Sprite> inLowRadar = new ArrayList<>(); // Low Priority ring
    private ArrayList<Sprite> inMedRadar = new ArrayList<>(); // Medium priority ring
    private ArrayList<Sprite> inHighRadar = new ArrayList<>(); // High priority ring

    float health_points = 100; // Health points (set to 100)

    /**
     * Drone Sprite constructor
     * @param layer Layer to set the drone on
     * @param x X Coordinate to start the drone on
     * @param y Y Coordinate to start the drone on
     *
     * Rotation is not specified in this constructor as the Rotation of the drone is calculated based on
     * its velocities.
     */
    public DroneSprite(Pane layer, double x, double y) {
        super(layer, x, y, 0, 4,25);
        rand = new Random();

        // Set the drones initial velocities to be anywhere from -1 to 1
        this.dx = -1 + (Math.random() * 2);
        this.dy = -1 + (Math.random() * 2);
        d = new Drone();
        try {
            setImageData(new Drone(), null);
        } catch (JSONException ignored){}
    }

    /**
     * Overriding Sprite's setImageData function.
     * @param img Image to set
     * @param j
     * @throws JSONException
     */
    @Override
    public void setImageData(SpriteImageClass img, JSONObject j) throws JSONException {
        // Do Sprites setImage function
        super.setImageData(img, j);

        // Now create the 3 Radar ring objects for debugging. However the shapes themselves are still used
        // to detect nearby objects to
        this.view_low = new Circle();
        view_low.setStroke(Color.GREEN);
        view_low.setStrokeType(StrokeType.INSIDE);
        view_low.setStrokeWidth(5);
        view_low.setRadius(this.width * 2);
        view_low.setFill(Color.TRANSPARENT);

        this.view_med = new Circle();
        view_med.setStroke(Color.YELLOW);
        view_med.setStrokeType(StrokeType.INSIDE);
        view_med.setStrokeWidth(2);
        view_med.setRadius(this.width * 1.5);
        view_med.setFill(Color.TRANSPARENT);

        this.view_high = new Circle();
        view_high.setStroke(Color.ORANGE);
        view_high.setStrokeType(StrokeType.INSIDE);
        view_high.setStrokeWidth(2);
        view_high.setRadius(this.width);
        view_high.setFill(Color.TRANSPARENT);
    }

    @Override
    public void updateUI() {
        super.updateUI();

        this.view_low.setCenterX(this.x + this.width / 2);
        this.view_low.setCenterY(this.y + this.height / 2);

        this.view_med.setCenterX(this.x + this.width / 2);
        this.view_med.setCenterY(this.y + this.height / 2);

        this.view_high.setCenterX(this.x + this.width / 2);
        this.view_high.setCenterY(this.y + this.height / 2);
    }

    @Override
    public void move() {
        super.move();
        this.dy += (-0.05 * Math.random()/10);
        this.dx += (-0.05 * Math.random()/10);

        if (healthShown) {
            getHealthBar();
            health.relocate(this.x, this.y-15);
        }

        this.rotation = (Math.toDegrees(Math.atan2(dy, dx))+90);
    }

    private boolean view_collision(Rectangle2D target, Circle view) {
        return view.getBoundsInParent().intersects(target.getMinX(), target.getMinY(), target.getWidth(), target.getHeight());
    }

    public void scanSurroundings(Sprite target) {
        if (view_collision(target.getBounds(), view_high)) {
            view_high.setStroke(Color.RED);
            view_med.setStroke(Color.RED);
            view_low.setStroke(Color.RED);
            this.inHighRadar.add(target);
        } else if (view_collision(target.getBounds(), view_med)) {
            view_med.setStroke(Color.RED);
            view_low.setStroke(Color.RED);
            this.inMedRadar.add(target);
        } else if (view_collision(target.getBounds(), view_low)) {
            view_low.setStroke(Color.RED);
            this.inLowRadar.add(target);
        }
        // Too damn late. The drone hit it. Stupid AI!
        if (this.collides(target)) {
            this.onCollides(target);
        }

    }

    public void actOnRadar() {
        if (inHighRadar.isEmpty()) {
            view_high.setStroke(Color.ORANGE);
        } else {
            inHighRadar.sort((d1, d2) -> {
                Double res1 = Math.sqrt(Math.pow(d1.getCenterX() - getCenterX(), 2) + Math.pow(d1.getCenterY() - getCenterY(), 2));
                Double res2 = Math.sqrt(Math.pow(d2.getCenterX() - getCenterX(), 2) + Math.pow(d2.getCenterY() - getCenterY(), 2));
                return res1.compareTo(res2);
            });
            Sprite target = inHighRadar.get(0);
            resolveCollision(target, x-target.x, y-target.y, 3);
        }

        if (inMedRadar.isEmpty()) {
            view_med.setStroke(Color.YELLOW);
        } else {
            inMedRadar.sort((d1, d2) -> {
                Double res1 = Math.sqrt(Math.pow(d1.getCenterX() - getCenterX(), 2) + Math.pow(d1.getCenterY() - getCenterY(), 2));
                Double res2 = Math.sqrt(Math.pow(d2.getCenterX() - getCenterX(), 2) + Math.pow(d2.getCenterY() - getCenterY(), 2));
                return res1.compareTo(res2);
            });
            Sprite target = inMedRadar.get(0);
            resolveCollision(target, x-target.x, y-target.y, 2);
        }

        if (inLowRadar.isEmpty()) {
            view_low.setStroke(Color.GREEN);
        } else {
            inLowRadar.sort((d1, d2) -> {
                Double res1 = Math.sqrt(Math.pow(d1.getCenterX() - getCenterX(), 2) + Math.pow(d1.getCenterY() - getCenterY(), 2));
                Double res2 = Math.sqrt(Math.pow(d2.getCenterX() - getCenterX(), 2) + Math.pow(d2.getCenterY() - getCenterY(), 2));
                return res1.compareTo(res2);
            });
            Sprite target = inLowRadar.get(0);
            resolveCollision(target, x-target.x, y-target.y, 1);
        }
    }

    public void clearRadar() {
        this.inLowRadar.clear();
        this.inMedRadar.clear();
        this.inHighRadar.clear();
    }

    public void resolveCollision(Sprite collision, double xd, double yd, int urgency) {
        /*
        Take delta in X and Y direction, and invert it so as the sprite gets closer and closer to a target,
        the Diff numbers become bigger, so the drone can change their velocities faster.

        Limit it to 1/2 Unit per frame^2 Velocity change. Any more and it looks like the drone instantly changed
        its velocity to the opposite direction!
         */
        double xDiff = 1/(Math.abs(xd)-this.width/2);
        double yDiff = 1/(Math.abs(yd)-this.height/2);

        if (xd < 0) {
            this.dx -= xDiff * urgency;
        } else {
            this.dx += xDiff * urgency;
        }

        if (yd < 0) {
            this.dy -= yDiff * urgency;
        } else {
            this.dy += yDiff * urgency;
        }
    }

    /**
     * Function ran in the event that this Drone collided with something and couldn't avoid it
     * @param hit The sprite that this collided with
     */
    public void onCollides(Sprite hit) {
        // The drone just collided with something
        if (Math.abs(this.getCenterX() - hit.getCenterX()) < 75 ||
            Math.abs(this.getCenterY() - hit.getCenterY()) < 75
        ) {
            if (this.dy < 0) {
                this.dy = MAX_VELOCITY;
            } else {
                this.dy = -MAX_VELOCITY;
            }

            if (this.dx < 0) {
                this.dx = MAX_VELOCITY;
            } else {
                this.dx = -MAX_VELOCITY;
            }
        }
        // OOPS! The Drone ended up inside another object! Teleport the drone by changing its velocities drastically
        // In order to move the drone out of object its stuck inside of.
        else {
            this.dy = -this.dy - (-1 + new Random().nextDouble() + 1);
            this.dx = -this.dx - (-1 + new Random().nextDouble() + 1);
        }
    }

    boolean healthShown = false;
    private ImageView health = new ImageView();
    private void getHealthBar() {
        if (health_points > 75) {
            health.setImage(new Image(getClass().getResourceAsStream("/health_100.png")));
        } else if (health_points > 50) {
            health.setImage(new Image(getClass().getResourceAsStream("/health_75.png")));
        } else if (health_points > 25) {
            health.setImage(new Image(getClass().getResourceAsStream("/health_50.png")));
        } else if (health_points > 10) {
            health.setImage(new Image(getClass().getResourceAsStream("/health_25.png")));
        } else {
            health.setImage(new Image(getClass().getResourceAsStream("/health_0.png")));
        }
    }

    @Override
    public void onRemove() {
        super.onRemove();
        if (healthShown) {
            this.healthShown = false;
            this.layer.getChildren().remove(health);
        }
        this.layer.getChildren().remove(view_low);
        this.layer.getChildren().remove(view_med);
        this.layer.getChildren().remove(view_high);
        this.layer.getChildren().remove(imageView);
    }

    @Override
    public void hideBoundary() {
        super.hideBoundary();
        this.layer.getChildren().remove(view_low);
        this.layer.getChildren().remove(view_med);
        this.layer.getChildren().remove(view_high);
    }

    @Override
    public void showBoundary() {
        super.showBoundary();
        this.layer.getChildren().add(view_low);
        this.layer.getChildren().add(view_med);
        this.layer.getChildren().add(view_high);
    }

    @Override
    public void onClicked() {
        if (!healthShown) {
            getHealthBar();
            this.layer.getChildren().add(health);
            this.healthShown = true;
        } else {
            this.healthShown = false;
            this.layer.getChildren().remove(health);
        }
        health.relocate(this.x, this.y-15);
    }

    @Override
    public void addToJson(JSONObject j) throws JSONException {
        super.addToJson(j);
        j.put("Health", health_points);
        j.put("Health-Shown", healthShown);
    }
}
