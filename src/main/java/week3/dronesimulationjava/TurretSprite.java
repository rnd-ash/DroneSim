package week3.dronesimulationjava;

import javafx.scene.layout.Pane;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class TurretSprite extends Sprite {
    public TurretSprite(Pane layer, double x, double y, double r) {
        super(layer, x, y, r, 1,5);
        try {
            super.setImageData(new Turret(), null);
        } catch (JSONException e){
            Logger.error("Cannot set image data for Lazer!");
        }
    }

    public TurretSprite(Pane layer, JSONObject j) {
        super(layer, j);
        try {
            setImageData(new Turret(), j);
        } catch (JSONException ignored){}
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
