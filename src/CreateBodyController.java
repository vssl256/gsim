import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CreateBodyController {
    @FXML private TextField nameField;
    @FXML private TextField massField;
    @FXML private TextField radiusField;
    @FXML private TextField colorField;

    @FXML private TextField atmColorField;
    @FXML private TextField atmOpacityField;
    @FXML private TextField atmHeightField;

    @FXML private TextField eField;
    @FXML private TextField xField;
    @FXML private TextField yField;

    @FXML private Button createBodyBtn;
    @FXML private CheckBox atmCheckBox;
    private boolean hasAtmosphere = false;
    
    private double atmRadius;
    private double atmOpacity;
    private Color atmColor;

    private Simulation simulation;
    private Graphics graphics;
    private double x, y;
    private Stage stage;
    public void set(Simulation simulation, Graphics graphics, double x, double y, Stage stage) {
        this.simulation = simulation;
        this.graphics = graphics;
        this.x = x;
        this.y = y;
        this.stage = stage;
    }

    public void initialize() {
        xField.setText(Double.toString(x));
        yField.setText(Double.toString(y));
        x = Double.parseDouble(xField.getText());
        y = Double.parseDouble(yField.getText());
        atmCheckBox.setOnAction(event -> {
            hasAtmosphere = atmCheckBox.isSelected();
            updateField(atmColorField, hasAtmosphere);
            updateField(atmOpacityField, hasAtmosphere);
            updateField(atmHeightField, hasAtmosphere);
        });
        createBodyBtn.setOnAction(event -> {
            createBody();
            stage.close();
        });
    }
    public void createBody() {
        double mass = Double.valueOf(massField.getText());
        double radius = Double.valueOf(radiusField.getText());
        Color color = Color.valueOf(colorField.getText());

        if (hasAtmosphere) {
            atmRadius = Double.valueOf(atmHeightField.getText());
            atmOpacity = Double.valueOf(atmOpacityField.getText());
            atmColor = Color.valueOf(atmColorField.getText());
        }

        simulation.addBody(x, y, mass, radius, color);
        Body body = simulation.getBodies().get(simulation.getBodies().size() - 1);
        if (hasAtmosphere) body.setAtmosphere(new Atmosphere(atmRadius, atmOpacity, atmColor));
        body.setOrbit(simulation.getBodies().get(0), Double.parseDouble(eField.getText()));
        graphics.init();
    }
    public void updateField(TextField field, boolean enabled) {
        field.setEditable(enabled);
        field.setStyle(enabled ? "-fx-background-color: WHITE" : "-fx-background-color: DIMGRAY");
        if (!enabled) field.clear();
    }
}
