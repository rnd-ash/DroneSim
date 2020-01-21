package week3.dronesimulationjava;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.geom.Rectangle2D;
import java.util.Random;

public class LazerSprite extends Sprite {
    // Override MAX Velocity
    static {
        MAX_VELOCITY = 2.5;
    }
    // Number of ticks the Lazer was alive for
    private int ticksAlive = 0;

    // Number of max ticks allowed
    private int maxTicksAlive;

    /** Indicates if the the lazer has hit something yet **/
    boolean isHit = false;


    public LazerSprite(Pane layer, double x, double y, double r) {
        super(layer, x, y, r, 1,1);
        this.dx = Math.cos(Math.toRadians(r+90)) * MAX_VELOCITY;
        this.dy = Math.sin(Math.toRadians(r+90)) * MAX_VELOCITY;
        maxTicksAlive = 200 + (int) (Math.random() * 100);
        try {
            super.setImageData(new Lazer(), null);
        } catch (JSONException ignored){}
    }

    public LazerSprite(Pane layer, JSONObject j) {
        super(layer, j);
        try {
            setImageData(new Lazer(), j);
        } catch (JSONException ignored){

        }
    }

    /**
     * Indicates if the lazer has expired and should be removed from the screen
     * @return Boolean indicating if the lazer has been alive for too long and should be removed from the screen
     */
    public boolean lazerDead() {return this.ticksAlive >= maxTicksAlive; }

    @Override
    public void move() {
        super.move();
        this.ticksAlive++;
        this.rotation = Math.toDegrees(Math.atan2(dy, dx))+90;
    }


    public void onCollides(Sprite hit) {
        if (hit instanceof DroneSprite) {
            DroneSprite drone = (DroneSprite) hit;
            drone.health_points -= Math.random() * 3;
            isHit = true;
        }
    }


    @Override
    public void onClicked() {
        // Do nothing on clicked
    }

    @Override
    public void onRemove() {
        super.onRemove();
        this.layer.getChildren().remove(imageView);
    }
}
