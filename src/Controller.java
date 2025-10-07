import java.text.DecimalFormat;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller {
    @FXML private Button resetButton;
    @FXML private Button followButton;
    @FXML private Text posText;
    @FXML private Pane simPane;
    @FXML private Slider speedSlider;
    private long speedFactor = 1;
    public long getSpeedFactor() { return speedFactor; }
    @FXML private ToggleGroup group1;
    private int orbitDisplayMode = 1;
    @FXML private Button speedReset;
    private Group simGroup;
    @FXML private TextField spdField;
    @FXML private Text fpsText;
    @FXML private Text xyText;

    private ContextMenu contextMenu = new ContextMenu();
    private double mouseX, mouseY;
    private double x, y;
    private double defScale = Config.getDouble("controller.defScale");
    private boolean follow = false;

    private Graphics graphics;
    private Simulation simulation;
    private Physics physics;
    private Body selectedBody;
    public Body getSelectedBody() { return selectedBody; }
    private AnimationTimer timer;
    
    @FXML
    private void initialize() {
        long start = System.nanoTime();
        initSimulation();
        initUIControls();
        initMouseHandlers();
        initTimer();
        System.out.println((System.nanoTime() - start)/1_000_000.0 + " ms");
        //simulation.readJSON("test.json");
        //graphics.init();
    }

    public void initSimulation() {
        simulation = new Simulation();
        physics = new Physics(simulation);
        
        //simulation.readJSON("test.json");
        
        simulation.addBody("Sun", 0, 0, 1.989e30, 6.957e8, "YELLOW");
        simulation.addBody("Earth", 1.471e11, 0, 6e24, 6.378e6, "BLUE");
        simulation.addBody("Moon", 1.471e11+3.636e8, 0, 7.36e22, 1.737e6, "GRAY");
        simulation.addBody("Jupiter", 7.415e11, 0, 1.898e27, 6.991e7, "ORANGE");
        simulation.addBody("Mercury", 4.6e10, 0, 3.301e23, 2.439e6, "DIMGRAY");

        simulation.addBody("Io", 7.415e11+4.217e8, 0, 8.93e22, 1.8216e6, "CORAL");
        simulation.addBody("Europa", 7.415e11+6.709e8, 0, 4.80e22, 1.5608e6, "WHITESMOKE");
        simulation.addBody("Ganymede", 7.415e11+1.07e9, 0, 1.48e23, 2.6312e6, "LIGHTGRAY");
        simulation.addBody("Callisto", 7.415e11+1.883e9, 0, 1.08e23, 2.4103e6, "DARKGRAY");
        List<Body> bodies = simulation.getBodies();
        bodies.get(1).setAtmosphere(new Atmosphere(1e5, 0.2, Color.LIGHTBLUE));
        bodies.get(2).addParent(bodies.get(1));
        bodies.get(1).addParent(bodies.get(0));
        bodies.get(3).addParent(bodies.get(0));
        bodies.get(4).addParent(bodies.get(0));
        
        bodies.get(5).addParent(bodies.get(3));
        bodies.get(6).addParent(bodies.get(3));
        bodies.get(7).addParent(bodies.get(3));
        bodies.get(8).addParent(bodies.get(3));

        simGroup = new Group();
        simPane.getChildren().add(simGroup);

        graphics = new Graphics(simulation, simGroup, simPane, this);
        graphics.init();
        
        Platform.runLater(this::setupInitialOrbits);
    }
    public void setupInitialOrbits() {
        List<Body> bodies = simulation.getBodies();
        centerSystem();
        bodies.get(1).setOrbit(bodies.get(0), 0.0167);
        bodies.get(2).setOrbit(bodies.get(1), 0.055);
        bodies.get(3).setOrbit(bodies.get(0), 0.048);
        bodies.get(4).setOrbit(bodies.get(0), 0.206);

        bodies.get(5).setOrbit(bodies.get(3), 0.0041);
        bodies.get(6).setOrbit(bodies.get(3), 0.0094);
        bodies.get(7).setOrbit(bodies.get(3), 0.0013);
        bodies.get(8).setOrbit(bodies.get(3), 0.0074);
        //simulation.saveJSON("real.json");
        simPane.widthProperty().addListener((obs, oldVal, newVal) -> centerSystem());
        simPane.heightProperty().addListener((obs, oldVal, newVal) -> centerSystem());
    }

    public void initContextMenu() {
        MenuItem itemAdd = new MenuItem("Create new body");
        itemAdd.setOnAction(event -> {
            createBodyWindow();
        });
        contextMenu.getItems().add(itemAdd);
        simPane.setOnContextMenuRequested(event -> {
            contextMenu.show(simGroup, event.getScreenX(), event.getScreenY());
        });
    }

    public void createBodyWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/addBody.fxml"));
            Parent root = loader.load();
            CreateBodyController createBodyController = loader.getController();
            Stage stage = new Stage();
            createBodyController.set(simulation, graphics, x, y, stage);
            createBodyController.loadBodies();
            stage.initStyle(StageStyle.UTILITY);
            stage.setOpacity(0.9);
            stage.setTitle("Create new body");
            stage.initOwner(simPane.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBody() {
    }

    public void initUIControls() {
        simPane.setScaleX(defScale);
        simPane.setScaleY(defScale);

        resetButton.setOnAction(event -> resetSimulation());
        speedReset.setOnAction(event -> {
            speedFactor = 1;
            speedSlider.valueProperty().set(1);
        });
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> speedFactor = newVal.intValue());
        followButton.setOnAction(event -> {
            follow = !follow;
            followButton.setText((follow) ? "Unfollow" : "Follow");
        });
        group1.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            RadioButton selected = (RadioButton) newToggle;
            orbitDisplayMode = "Orbits".equals(selected.getText()) ? 2 : 1;
        });
        initContextMenu();
        spdField.setOnKeyTyped(event -> {
            if (!spdField.getText().isEmpty()) speedFactor = Integer.valueOf(spdField.getText());
            else speedFactor = 0;
        });
    }

    public void resetSimulation() {
        List<Body> bodies = simulation.getBodies();
        List<Polyline> trails = graphics.getTrails();

        for (int i = 0; i < bodies.size(); i++) {
            bodies.get(i).reset();
            trails.get(i).getPoints().clear();
        }
        resetCam();
    }

    public void resetCam() {
        simPane.setTranslateX(0);
        simPane.setTranslateY(0);
        simPane.setScaleX(defScale);
        simPane.setScaleY(defScale);
    }

    public void initMouseHandlers() {
        Rectangle bg = new Rectangle();
        bg.setWidth(1e14);
        bg.setHeight(1e14);
        bg.setLayoutX(-bg.getWidth() / 2);
        bg.setLayoutY(-bg.getHeight() / 2);
        bg.setFill(Color.TRANSPARENT);
        simPane.getChildren().add(0, bg);
        simPane.setOnMousePressed(event -> {
            contextMenu.hide();
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            Point2D simCoords = simGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
            x = simCoords.getX();
            y = simCoords.getY();
            List<Body> bodies = simulation.getBodies();
            for (Body body : bodies) {
                Body main = body.main;
                if (main == null) main = bodies.get(0);
                if (Math.sqrt(Math.pow((body.x-x), 2) + Math.pow((body.y - y), 2))<20 / simPane.getScaleX()) {
                    selectedBody = body;
                    break;
                }
            }
        });
        simPane.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double dx = event.getSceneX() - mouseX;
                double dy = event.getSceneY() - mouseY;

                simPane.setTranslateX(simPane.getTranslateX() + dx);
                simPane.setTranslateY(simPane.getTranslateY() + dy);
            }
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        simPane.addEventFilter(ScrollEvent.SCROLL, event -> zoomHandler(event));
    }

    public void zoomHandler(ScrollEvent event) {
        double zoomFactor = 1.15;
        double deltaY = event.getDeltaY();
        if (Math.abs(event.getTextDeltaY()) > 0.0) {
            deltaY = event.getTextDeltaY();
        }
        if (deltaY > 0) {
            simPane.setScaleX(simPane.getScaleX() * zoomFactor);
            simPane.setScaleY(simPane.getScaleY() * zoomFactor);
        } else if (deltaY < 0) {
            simPane.setScaleX(simPane.getScaleX() / zoomFactor);
            simPane.setScaleY(simPane.getScaleY() / zoomFactor);
        }
        event.consume();
    }

    public void centerSystem() {
        double cx = simPane.getWidth() / 2;
        double cy = simPane.getHeight() / 2;

        Body main = simulation.getBodies().get(0);

        simGroup.setTranslateX(cx - main.x);
        simGroup.setTranslateY(cy - main.y);
    }

    public void follow(Body body) {
        List<Body> bodies = simulation.getBodies();
        if (body == null) {
            body = bodies.get(0);
            selectedBody = bodies.get(1);
        }
        double scale = simPane.getScaleX();
        simPane.setTranslateX(-body.x * scale);
        simPane.setTranslateY(-body.y * scale);
    }
    private long realTimeNS;
    private void initTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                posText.setText(Long.toString(speedFactor)+"x");
                realTimeNS = System.nanoTime();
                for (int i = 0; i < speedFactor; i++) {
                    physics.step();
                }
                graphics.update(orbitDisplayMode);
                realTimeNS = System.nanoTime() - realTimeNS;
                double fps = 1e9/realTimeNS;
                fpsText.setText("FPS: " + (int)fps);
                if (follow) follow(selectedBody);
                DecimalFormat df = new DecimalFormat("0.00E0");
                String selected = (selectedBody != null) ? selectedBody.name : "Nothing";
                String main = (selectedBody != null && selectedBody.main != null) ? selectedBody.main.name : "Nothing";
                xyText.setText("X: " + df.format(x) +
                "\nY: " + df.format(y) + 
                "\nSelected: " + selected + 
                "\nDistance: " + df.format(graphics.getDist() / 1_000) + " km" +
                "\nMain: " + main);
            }
        };
        timer.start();
    }
}