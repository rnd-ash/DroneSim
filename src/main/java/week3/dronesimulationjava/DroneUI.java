package week3.dronesimulationjava;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Drone game main class for JavaFX application
 */
public class DroneUI extends Application {
    /** Arena width **/
    static int ARENA_MAX_WIDTH = 0;

    /** Arena height **/
    static int ARENA_MAX_HEIGHT = 0;

    /** Frame time in MS (default represents 60FPS) **/
    private int FRAME_TIME_MS = 1000/60;

    // Update times for keeping track of when frames were drawn
    private long updateTime = System.currentTimeMillis();
    // Update time for keeping track of when lazers were last shot
    private long lastLazerShoot = System.currentTimeMillis();
    // Debug mode boolean
    private boolean debug_mode = false;

    // Booleans to indicate if keys are being held down - Allows for non-blocking animations when key is held down
    private boolean right_held = false;
    private boolean left_held = false;
    private boolean shoot_held = false;


    ArrayList<DroneSprite> drones = new ArrayList<>();
    ArrayList<TurretSprite> turrets = new ArrayList<>();
    ArrayList<LazerSprite> lazers = new ArrayList<>();
    Pane droneLayer;
    Random r = new Random();
    public void start(Stage s) {
        s.setTitle("Drone Simulation (26011139)");
        BorderPane bp = new BorderPane();
        droneLayer = new Pane();
        droneLayer.setBackground(new Background(new BackgroundFill(Color.rgb(53, 81, 92), CornerRadii.EMPTY, Insets.EMPTY )));
        bp.setTop(menu());
        bp.setCenter(droneLayer);
        s.setWidth(1600);
        s.setHeight(900);
        s.setResizable(true);
        Scene scene = new Scene(bp, 1600, 900, Color.BLUE);
        s.setScene(scene);
        s.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        s.show();
        bp.setRight(new VBox());
        bp.setBottom(getBottomBar());
        ARENA_MAX_WIDTH = (int) droneLayer.getWidth(); // Last 1/3 of horizontal space for UI
        ARENA_MAX_HEIGHT = (int) droneLayer.getHeight();

        setupElements((int) (Math.random() * 5), (int) (Math.random() * 20));

        // On key pressed
        scene.setOnKeyPressed(k -> {
            KeyCode x = k.getCode();
            if (x == KeyCode.W) {
                shoot_held = true;
            } else if (x == KeyCode.A) {
                left_held = true;
            } else if (x == KeyCode.D) {
                right_held = true;
            } else if (x == KeyCode.H) {
                // Toggle health bar of the drones
                drones.forEach(DroneSprite::onClicked);
            }
        });

        scene.setOnKeyReleased(k -> {
            KeyCode x = k.getCode();
            if (x == KeyCode.W) {
                shoot_held = false;
            } else if (x == KeyCode.A) {
                left_held = false;
            } else if (x == KeyCode.D) {
                right_held = false;
            }
        });

        // Animation loop - This runs whenever the screen is drawn at VBlank interval
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Due to the university PC's running at 75Hz, and my laptop running at either 144Hz or 60Hz,
                // I have chosen to lock the framerate of the simulation to 60 Hz. This way, the drones don't
                // Move insanely fast if the screen's refresh rate is quick.
                if (System.currentTimeMillis() - updateTime >= FRAME_TIME_MS) {
                    updateTime = System.currentTimeMillis();
                    // Handle user actions
                    if (left_held) {
                        turrets.forEach(w -> w.rotation-=5);
                    }
                    if (right_held) {
                        turrets.forEach(w -> w.rotation+=5);
                    }

                    // Check if the player is shooting, and that the last time the Lazers were shot is 50ms
                    // Ago. This prevents a huge swarm of Lazers being spawned every frame.
                    if (shoot_held && System.currentTimeMillis() - lastLazerShoot >= 50) {
                        lastLazerShoot = System.currentTimeMillis();
                        turrets.forEach(w -> {
                            lazers.add(new LazerSprite(
                                    droneLayer,
                                    (w.getBounds().getMaxX() + w.getBounds().getMinX()) / 2,
                                    (w.getBounds().getMaxY() + w.getBounds().getMinY()) / 2,
                                    w.rotation-180));
                        });
                    }

                    // Update turrets location
                    turrets.forEach(Sprite::updateUI);

                    // For each drone, remove it if it has no health left
                    drones.forEach(d -> {
                        if (d.health_points <= 0) {
                            d.remove();
                        }
                    });
                    drones.removeIf(d -> (d.health_points <= 0));

                    // Build a list of all sprites in the arena, to be used by the drones collision detection
                    ArrayList<Sprite> sprites = new ArrayList<>();
                    sprites.addAll(drones);
                    sprites.addAll(turrets);
                    sprites.addAll(lazers);

                    // Iterate over all remaining drones
                    for (DroneSprite d : drones) {
                        // Clear each drones radar data
                        d.clearRadar();
                        for (Sprite s : sprites) {
                            // If the drones UUID is not equal to the sprite that we are iterating over
                            // IE (The drone isn't itself), scan surroundings with this sprite as the target
                            if (s.uuid != d.uuid) {
                                d.scanSurroundings(s);
                            }
                        }
                        // Drone should now act upon its radar data
                        d.actOnRadar();
                        // Move the drone
                        d.move();
                        // Update each drones UI location
                        d.updateUI();
                    }

                    // Update each lazer's location
                    lazers.forEach(l -> {
                        l.move();
                        l.updateUI();
                        // Check if a lazer has hit a drone yet
                        for (DroneSprite d : drones) {
                            if (l.collides(d)) {
                                l.onCollides(d);
                            }
                        }
                        // Remove the lazer if it hit a drone or if its maxTicksAlive has expired.
                        if (l.isHit || l.lazerDead()) { l.remove(); }
                    });
                    // Remove from lazer array
                    lazers.removeIf(l -> l.isHit || l.lazerDead());
                }
            }
        };
        loop.start();
    }


    /**
     * Gets the menubar for top of the screen
     * @return Menubar for top of display
     */
    public MenuBar menu() {
        MenuBar menu = new MenuBar();
        // File menu
        Menu file = new Menu("File");
        MenuItem open = new MenuItem("Open Simulation");
        // On clicked, show a JFileChooser for user to load save file from
        open.setOnAction(event -> {
            JFrame frame = new JFrame();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Simulation location");
            int selection = chooser.showSaveDialog(frame);
            if (selection == JFileChooser.APPROVE_OPTION) {
                loadSim(chooser.getSelectedFile());
            }
        });

        MenuItem save = new MenuItem("Save Simulation");
        // On clicked, run saveSim()
        save.setOnAction(event -> {
            saveSim();
        });
        file.getItems().add(open);
        file.getItems().add(save);
        menu.getMenus().add(file);
        menu.setUseSystemMenuBar(true); // For Linux
        return menu;
    }

    /**
     * Function to randomly generate or add elements to the screen
     * @param numTurrets Number of Turrets to add
     * @param numDrones Number of Drones to add
     */
    private void setupElements(int numTurrets, int numDrones) {
        // Atomic references for x_pos and y_pos to be used in lambdas
        AtomicReference<Double> x_pos = new AtomicReference<>(Math.random() * ARENA_MAX_WIDTH-100);
        AtomicReference<Double> y_pos = new AtomicReference<>(Math.random() * ARENA_MAX_HEIGHT-100);

        for (int i = 0; i < numTurrets; i++) {
            x_pos.set(Math.random() * ARENA_MAX_WIDTH-100);
            y_pos.set(Math.random() * ARENA_MAX_HEIGHT-100);
            ArrayList<Sprite> sprites = new ArrayList(turrets);
            double finalX_pos = x_pos.get();
            double finalY_pos = y_pos.get();
            sprites.forEach(d -> {
                int attempts = 0;
                while (attempts < 100) {
                    if (!d.collidesWithPoints(finalX_pos, finalY_pos, 300, 300)) {
                        break;
                    } else {
                        if (x_pos.get() < ARENA_MAX_WIDTH - 100) {
                            x_pos.updateAndGet(v -> (v + 100));
                        } else {
                            x_pos.set(0.0);
                            if (y_pos.get() < ARENA_MAX_HEIGHT-100) {
                                y_pos.updateAndGet(v -> (v + 100));
                            } else {
                                y_pos.set(0.0);
                            }
                        }
                    }
                    attempts++;
                }
            });
            turrets.add(new TurretSprite(droneLayer, x_pos.get(), y_pos.get(), 0));
        }

        for (int i = 0; i < numDrones; i++) {
            x_pos.set(Math.random() * ARENA_MAX_WIDTH-50);
            y_pos.set(Math.random() * ARENA_MAX_HEIGHT-50);
            ArrayList<Sprite> sprites = new ArrayList();
            sprites.addAll(drones);
            sprites.addAll(turrets);
            sprites.forEach(d -> {
                int attempts = 0;
                while (attempts < 100) {
                    if (!d.collidesWithPoints(x_pos.get(), y_pos.get(), 50, 50)) {
                        break;
                    } else {
                        if (x_pos.get() < ARENA_MAX_WIDTH - 50) {
                            x_pos.updateAndGet(v -> (v + 10));
                        } else {
                            x_pos.set(0.0);
                            if (y_pos.get() < ARENA_MAX_HEIGHT - 50) {
                                y_pos.updateAndGet(v -> (v + 50));
                            } else {
                                y_pos.set(0.0);
                            }
                        }
                    }
                    attempts++;
                }
            });
            drones.add(new DroneSprite(droneLayer, x_pos.get(), y_pos.get()));
        }

    }

    /**
     * Loads the simulation from a JSON File specified
     * @param name Name of file to load.
     */
    public void loadSim(File name) {
        // Resulting String from file
        StringBuilder res = new StringBuilder();
        try {
            // Try to read all the lines in the file and concat them to res
            BufferedReader br = new BufferedReader(new FileReader(name));
            for (String line; (line = br.readLine())  != null;) {
                res.append(line).append("\n");
            }
        } catch (IOException e) {
            // IO Error whilst reading the file. Bail.
            Logger.error(String.format("IO Error loading simulation from %s", name.getAbsoluteFile()));
            return;
        }
        // Reading file successful. Now try to parse it

        // JSON Object from file.
        JSONObject json;
        try {
            json = new JSONObject(res.toString());
        } catch (JSONException e) {
            // JSON is malformed!
            Logger.error(String.format("Error processing json from file %s", name.getAbsoluteFile()));
            return;
        }

        // Check that the simulation JSON is valid
        JSONArray objs = json.optJSONArray("Objects");
        if (objs == null) {
            // JSON Does not meet the spec. Its not for this program. Bail
            Logger.error(String.format("Cannot load simulation from %s. JSON format invalid", name.getAbsoluteFile()));
        } else {
            // Now we try to load all the elements into temporary arrays
            ArrayList<DroneSprite> drones_temp = new ArrayList<>();
            ArrayList<LazerSprite> lazers_temp = new ArrayList<>();
            ArrayList<TurretSprite> turr_temp = new ArrayList<>();
            for (int i = 0; i < objs.length(); i++) {
                // Try to parse each element in JSON Object array
                try {
                    JSONObject obj = (JSONObject) objs.get(i);
                    String className = obj.optString("Class_Name");
                    if (className.equals(DroneSprite.class.getCanonicalName())) { // JSON represents a Drone
                        drones_temp.add(new DroneSprite(droneLayer, obj));
                    } else if (className.equals(LazerSprite.class.getCanonicalName())) { // JSON represents a Lazer
                        lazers_temp.add(new LazerSprite(droneLayer, obj));
                    } else if (className.equals(TurretSprite.class.getCanonicalName())) { // JSON represents a Turret
                        turr_temp.add(new TurretSprite(droneLayer, obj));
                    } else {
                        // Unsupported object name. Ignore it
                        Logger.warn(String.format("Error loading object into sim with unsupported class name: %s", className));
                    }
                } catch (Exception e) {
                    // Exception when initialising an object. Throw an error but continue
                    Logger.error(String.format("Error whilst loading object into sim. Caused by %s", e.getCause()));
                }
            }
            // Now that we have initialised all the objects, we can clear the current simulation lists
            // And fill them with the newly initialised objects.
            drones.forEach(Sprite::remove);
            turrets.forEach(Sprite::remove);
            lazers.forEach(Sprite::remove);
            drones = drones_temp;
            lazers = lazers_temp;
            turrets = turr_temp;
        }
    }

    /**
     * Saves simulation to a JSON File, as specified by the user with a file dialog
     */
    public void saveSim() {
        JFrame frame = new JFrame();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Simulation location");
        int selection = chooser.showSaveDialog(frame);
        if (selection == JFileChooser.APPROVE_OPTION) {
            JSONObject json = new JSONObject();
            JSONArray objects = new JSONArray();

            drones.forEach(d -> {
                try {
                    objects.put(d.toJson());
                } catch (JSONException e) {
                    Logger.error(String.format("Error saving JSON for drone ID %s", d.uuid));
                }
            });

            lazers.forEach(d -> {
                try {
                    objects.put(d.toJson());
                } catch (JSONException e) {
                    Logger.error(String.format("Error saving JSON for Lazer ID %s", d.uuid));
                }
            });

            turrets.forEach(d -> {
                try {
                    objects.put(d.toJson());
                } catch (JSONException e) {
                    Logger.error(String.format("Error saving JSON for Turret ID %s", d.uuid));
                }
            });
            try {
                json.put("Objects", objects);
            } catch (JSONException e) {
                Logger.error("Error appending JSON Data. Skipping Save action");
                return;
            }
            File f = chooser.getSelectedFile();
            try {
                BufferedWriter br = new BufferedWriter(new FileWriter(f));
                br.write(json.toString(1));
                br.flush();
                br.close();
            } catch (Exception e) {
                Logger.error("Error writing to file");
            }
        }
    }

    private HBox getBottomBar() {
        Button add_drone = new Button("Add Drone");
        add_drone.setOnAction(event -> {
            if (drones.size() < 20) {
                setupElements(0, 1);
            } else {
                Logger.info("Too many drones! - Not adding any more");
            }
        });

        Button add_turret = new Button("Add Turret");
        add_turret.setOnAction(event -> {
            if (turrets.size() < 5) {
                setupElements(1, 0);
            } else {
                Logger.info("Too many Turrets! - Not adding any more");
            }
        });

        Button debug = new Button("Toggle debug points");
        debug.setOnAction(event -> {
            if (!debug_mode) {
                drones.forEach(Sprite::showBoundary);
                turrets.forEach(Sprite::showBoundary);
                lazers.forEach(Sprite::showBoundary);

            } else {
                drones.forEach(Sprite::hideBoundary);
                turrets.forEach(Sprite::hideBoundary);
                lazers.forEach(Sprite::hideBoundary);
            }
            debug_mode = !debug_mode;
        });

        HBox hbox =  new HBox(new Label("Add: "), add_drone, add_turret, new Label("Additional: "), debug);
        hbox.setMinHeight(30);
        hbox.setSpacing(10);
        return hbox;
    }
}

