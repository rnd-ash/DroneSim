package week3.dronesimulationjava;

import java.util.Random;

public class Drone extends SpriteImageClass {

    @Override
    public String getImageAsString() {
        switch (new Random().nextInt(3)) {
            case 2:
                return "/drone_2.png";
            case 1:
                return "/drone_1.png";
            default:
                return "/drone_0.png";
        }
    }
}
