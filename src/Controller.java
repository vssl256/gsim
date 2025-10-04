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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller {
    @FXML private Button resetButton;
    @FXML private Button addButton;
    @FXML private Text posText;
    @FXML private Pane simPane;
    @FXML private Slider speedSlider;
    private int speedFactor = 1;
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
    private double defZoom = 8e-8;

    private Physics physics;
    private Graphics graphics;
    private Simulation simulation;

    public int getOrbitDisplayMode() { return orbitDisplayMode; }

    private AnimationTimer timer;
    
    @FXML
    private void initialize() {
        initSimulation();
        initUIControls();
        initMouseHandlers();
        initTimer();
        //simulation.readJSON("test.json");
        //graphics.init();
    }

    public void initSimulation() {
        simulation = new Simulation();
        physics = new Physics(simulation);
        
        //simulation.addBody("Sun", 0, 0, 6e24, 20, "ORANGERED");
        //simulation.addBody("Earth", 100, 0, 400, 1, "DEEPSKYBLUE");
        //simulation.addBody("Moon", 103, 0, 0.001, 0.3, "GRAY");
        //simulation.addBody("Jupiter", 400, 0, 1000, 5, "ORANGE");
        //simulation.addBody("Mercury", 30, 0, 3, 1, "DARKGRAY");
        //simulation.readJSON("test.json");
        simulation.addBody("Sun", 0, 0, 1.989e30, 6.957e8, "YELLOW");
        simulation.addBody("Earth", 1.471e11, 0, 6e24, 6.378e6, "BLUE");
        simulation.addBody("Moon", 1.471e11+3.636e8, 0, 7.36e22, 1.737e6, "GRAY");
        List<Body> bodies = simulation.getBodies();
        bodies.get(1).setAtmosphere(new Atmosphere(1e5, 0.2, Color.LIGHTBLUE));
        bodies.get(2).addParent(bodies.get(1));
        simGroup = new Group();
        simPane.getChildren().add(simGroup);

        graphics = new Graphics(simulation, simGroup, simPane);
        graphics.init();
        
        Platform.runLater(this::setupInitialOrbits);
    }

    public void setupInitialOrbits() {
        List<Body> bodies = simulation.getBodies();
        centerSystem();
        bodies.get(1).setOrbit(bodies.get(0), 0.0167);
        bodies.get(2).setOrbit(bodies.get(1), 0.055);
        //bodies.get(2).setOrbit(bodies.get(1), 0);
        //bodies.get(3).setOrbit(bodies.get(0), 0);
        //bodies.get(4).setOrbit(bodies.get(0), 0.1);
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
        simPane.setScaleX(defZoom);
        simPane.setScaleY(defZoom);
        resetButton.setOnAction(event -> resetSimulation());
        speedReset.setOnAction(event -> {
            speedFactor = 1;
            speedSlider.valueProperty().set(1);
        });
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> speedFactor = newVal.intValue());
        addButton.setOnAction(event -> {});
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
        simPane.setScaleX(defZoom);
        simPane.setScaleY(defZoom);
    }

    public void initMouseHandlers() {
        simPane.setOnMousePressed(event -> {
            contextMenu.hide();
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            Point2D simCoords = simGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
            x = simCoords.getX();
            y = simCoords.getY();
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

    public void follow() {
        List<Body> bodies = simulation.getBodies();
        Body body = bodies.get(1);
        double scale = simPane.getScaleX();
        simPane.setTranslateX(-body.x * scale);
        simPane.setTranslateY(-body.y * scale);
    }
    private long realTimeNS;
    private void initTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
            posText.setText(Integer.toString(speedFactor));
                realTimeNS = System.nanoTime();
                for (int i = 0; i < speedFactor/100; i++) {
                    physics.step();
                }
                follow();
                graphics.update(orbitDisplayMode);
                realTimeNS = System.nanoTime() - realTimeNS;
                double fps = 1e9/realTimeNS;
                fpsText.setText("FPS: " + (int)fps);
                DecimalFormat df = new DecimalFormat("0.00E0");
                xyText.setText(df.format(x) + " :X"+"\n"+df.format(y) + " :Y");
            }
        };
        timer.start();
    }
}