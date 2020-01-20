package week3.dronesimulationjava;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import org.json.JSONException;

import java.awt.geom.Rectangle2D;
import java.util.Random;

public class LazerSprite extends Sprite {
    static {
        MAX_VELOCITY = 2.5;
    }
    static int IDENTIFIER = 0;
    int ticksAlive = 0;
    int maxTicksAlive;
    Random rand;
    int id;
    public LazerSprite(Pane layer, double x, double y, double r) {
        super(layer, x, y, r, 1,1);
        rand = new Random();
        id = IDENTIFIER;
        IDENTIFIER++;
        this.dx = Math.cos(Math.toRadians(r+90)) * MAX_VELOCITY;
        this.dy = Math.sin(Math.toRadians(r+90)) * MAX_VELOCITY;
        maxTicksAlive = 200 + rand.nextInt(100);
        try {
            super.setImageData(new Lazer(), null);
        } catch (JSONException ignored){}
    }

    public boolean lazerDead() {return this.ticksAlive >= maxTicksAlive; }

    @Override
    public void move() {
        super.move();
        this.ticksAlive++;
        this.rotation = Math.toDegrees(Math.atan2(dy, dx))+90;
    }


    int hitCount = 0;
    public void onCollides(Sprite hit) {
        if (hit instanceof DroneSprite) {
            DroneSprite drone = (DroneSprite) hit;
            this.layer.getChildren().remove(this.imageView);
            this.image = new Image(getClass().getResourceAsStream("/hit.png"));
            this.imageView.setImage(image);
            this.layer.getChildren().add(this.imageView);
            if (hitCount == 0) {
                drone.health_points -= 1;
            }
            hitCount++;
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
