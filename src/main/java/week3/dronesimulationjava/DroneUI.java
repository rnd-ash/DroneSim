package week3.dronesimulationjava;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.scene.control.MenuBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static javax.swing.text.StyleConstants.Background;


public class DroneUI extends Application {
    static int ARENA_MAX_WIDTH = 0;
    static int ARENA_MAX_HEIGHT = 0;
    static int FRAME_TIME_MS = 1000/60;
    long updateTime = System.currentTimeMillis();
    long lastLazerShoot = System.currentTimeMillis();
    boolean debug = false;

    private boolean right_held = false;
    private boolean left_held = false;
    private boolean shoot_held = false;

    ArrayList<DroneSprite> drones = new ArrayList<>();
    ArrayList<TurretSprite> walls = new ArrayList<>();
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
        ARENA_MAX_WIDTH = (int) droneLayer.getWidth(); // Last 1/3 of horizontal space for UI
        ARENA_MAX_HEIGHT = (int) droneLayer.getHeight();



        setupElements(5, 20);
        scene.setOnKeyPressed(k -> {
            KeyCode x = k.getCode();
            if (x == KeyCode.W) {
                shoot_held = true;
            } else if (x == KeyCode.A) {
                left_held = true;
            } else if (x == KeyCode.D) {
                right_held = true;
            } else if (x == KeyCode.F3) {
                if (!debug) {
                    drones.forEach(Sprite::showBoundary);
                    walls.forEach(Sprite::showBoundary);
                    lazers.forEach(Sprite::showBoundary);

                } else {
                    drones.forEach(Sprite::hideBoundary);
                    walls.forEach(Sprite::hideBoundary);
                    lazers.forEach(Sprite::hideBoundary);
                }
                debug = !debug;
            } else if (x == KeyCode.H) {
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

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (System.currentTimeMillis() - updateTime >= FRAME_TIME_MS) {
                    updateTime = System.currentTimeMillis();


                    // Handle user actions
                    if (left_held) {
                        walls.forEach(w -> w.rotation-=5);
                    }

                    if (right_held) {
                        walls.forEach(w -> w.rotation+=5);
                    }

                    if (shoot_held && System.currentTimeMillis() - lastLazerShoot >= 10) {
                        lastLazerShoot = System.currentTimeMillis();
                        walls.forEach(w -> {
                            lazers.add(new LazerSprite(
                                    droneLayer,
                                    (w.getBounds().getMaxX() + w.getBounds().getMinX()) / 2,
                                    (w.getBounds().getMaxY() + w.getBounds().getMinY()) / 2,
                                    w.rotation-180));
                        });
                    }

                    walls.forEach(Sprite::updateUI);
                    // Handle drones that have died
                    drones.forEach(d -> {
                        if (d.health_points <= 0) {
                            d.remove();
                        }
                    });
                    drones.removeIf(d -> (d.health_points <= 0));

                    ArrayList<Sprite> sprites = new ArrayList<>();
                    sprites.addAll(drones);
                    sprites.addAll(walls);
                    sprites.addAll(lazers);

                    // Iterate over all remaining drones
                    for (DroneSprite d : drones) {
                        d.clearRadar();
                        for (Sprite s : sprites) {
                            if (s.uuid != d.uuid) {
                                d.scanSurroundings(s);
                            }
                        }
                        d.actOnRadar();
                        d.move();
                        d.updateUI();
                    }

                    lazers.forEach(l -> {
                        l.move();
                        l.updateUI();
                        for (DroneSprite d : drones) {
                            if (l.collides(d)) {
                                l.onCollides(d);
                            }
                        }
                        if (l.hitCount > 0) {
                            l.hitCount++;
                        }
                        if (l.hitCount >= 10 || l.lazerDead()) { l.remove(); }
                    });
                    lazers.removeIf(l -> l.hitCount >= 10 || l.lazerDead());
                }
            }
        };
        loop.start();
    }


    public MenuBar menu() {
        MenuBar menu = new MenuBar();
        // File menu
        Menu file = new Menu("File");
        MenuItem open = new MenuItem("Open Simulation");
        open.setOnAction(event -> {
            loadSim(null);
        });

        MenuItem save = new MenuItem("Save Simulation");
        save.setOnAction(event -> {
            saveSim();
        });
        /*
        save.setOnAction(event -> {
            drones.forEach(droneSprite -> {
                    try {
                        droneSprite.toJson();
                    } catch (JSONException ignored){}
            });
        });
         */
        file.getItems().add(open);
        file.getItems().add(save);
        menu.getMenus().add(file);

        // Done sim menu
        Menu sim = new Menu("Simulation");
        MenuItem add_drone_random = new MenuItem("Add drone (Random)");
        add_drone_random.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setupElements(0, 1);
            }
        });
        MenuItem add_obstacle = new MenuItem("Add obstacle");
        add_obstacle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                setupElements(1,0);
            }
        });
        sim.getItems().add(add_drone_random);
        sim.getItems().add(add_obstacle);
        menu.getMenus().add(sim);
        menu.setUseSystemMenuBar(true);
        return menu;
    }

    private void setupElements(int numWalls, int numDrones) {
        AtomicReference<Double> x_pos = new AtomicReference<>(Math.random() * 1550);
        AtomicReference<Double> y_pos = new AtomicReference<>(Math.random() * 850);

        for (int i = 0; i < numWalls; i++) {
            x_pos.set(Math.random() * 1550);
            y_pos.set(Math.random() * 850);
            ArrayList<Sprite> sprites = new ArrayList(walls);
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
            walls.add(new TurretSprite(droneLayer, x_pos.get(), y_pos.get(), 0));
        }

        for (int i = 0; i < numDrones; i++) {
            x_pos.set(Math.random() * 1550);
            y_pos.set(Math.random() * 850);
            ArrayList<Sprite> sprites = new ArrayList();
            sprites.addAll(drones);
            sprites.addAll(walls);
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
    public void loadSim(File name) {
        drones.forEach(Sprite::remove);
        walls.forEach(Sprite::remove);
        lazers.forEach(Sprite::remove);
        drones.clear();
        walls.clear();
        lazers.clear();
    }

    public void saveSim() {
        JFrame frame = new JFrame();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Simulation location");
        int selection = chooser.showSaveDialog(frame);
        if (selection == JFileChooser.APPROVE_OPTION) {
            System.out.println(chooser.getSelectedFile());
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

            walls.forEach(d -> {
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
}

