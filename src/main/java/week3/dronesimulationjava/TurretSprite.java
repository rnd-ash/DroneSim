package week3.dronesimulationjava;

import javafx.scene.layout.Pane;
import org.json.JSONException;

import java.util.Random;

public class TurretSprite extends Sprite {
    static int IDENTIFIER = 0;

    Turret w;
    Random rand;
    int id;
    public TurretSprite(Pane layer, double x, double y, double r) {
        super(layer, x, y, r, 1,5);
        w = new Turret();
        rand = new Random();
        id = IDENTIFIER;
        IDENTIFIER++;
        try {
            super.setImageData(new Turret(), null);
        } catch (JSONException e){
            Logger.error("Cannot set image data for Lazer!");
        }
    }
    public void move() {

    }

    public void onCollides(Sprite hit) {
    }

    @Override
    public void onClicked() {

    }

    @Override
    public void onRemove() {
        super.onRemove();
    }

    void summonLazer() {

    }
}
