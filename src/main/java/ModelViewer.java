import com.sun.javafx.scene.paint.GradientUtils;
import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ModelViewer {

    //Containters
    @FXML
    private Stage primaryStage;
    @FXML
    private VBox vbGroupContainer;
    @FXML
    private AnchorPane apTranslations;
    @FXML
    private AnchorPane apLighting;
    @FXML
    private AnchorPane apMaterial;


    //Controls
    @FXML
    private Button btnBrowse;
    @FXML
    private TextField tfModel;
    @FXML
    private Label lbVertices;
    @FXML
    private Label lbFaces;
    @FXML
    private Label lbTexCoords;
    @FXML
    private CheckBox cbWireframe;
    @FXML
    private CheckBox cbCullFace;
    @FXML
    private CheckBox cbMouseControl;
    @FXML
    private Button btnBrowseMat;
    @FXML
    private TextField tfMaterial;
    @FXML
    private CheckBox cbApplyTexture;
    @FXML
    private ImageView imgMatPreview;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private CheckBox rotateX;
    @FXML
    private CheckBox rotateY;
    @FXML
    private CheckBox rotateZ;
    @FXML
    private CheckBox cbSelfIlluminate;
    @FXML
    private CheckBox cbReflective;
    @FXML
    private Slider sliderX;
    @FXML
    private Slider sliderY;
    @FXML
    private Slider sliderZ;
    @FXML
    private ColorPicker colorPickerLight;
    @FXML
    private CheckBox cbAnimateLight;



    private SubScene scene3D;
    private Group model;
    private MeshView[] meshes;
    private PhongMaterial mat;
    private PointLight pointLight;

    private ResourceManager resourseManager;
    private SettingsService settingsService;

    private static final int VIEWPORT_SIZE = 900;
    private static final double MODEL_SCALE_FACTOR = 10;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 0;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE / 2;

    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);

    private RotateTransition rotateTransX;
    private RotateTransition rotateTransY;
    private RotateTransition rotateTransZ;

    private boolean lightAdded;

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            pointLight.setRotate(pointLight.getRotate() + 1);
        }
    };


    @FXML
    public void initialize() {
        this.primaryStage = Main.getPrimaryStage();
        resourseManager = new ResourceManager();
        settingsService = new SettingsService();
        mat = new PhongMaterial();

        initModelChooser();

        apTranslations.disableProperty().bind(settingsService.modelLoadedProperty().not());
        apLighting.disableProperty().bind(settingsService.modelLoadedProperty().not());
        apMaterial.disableProperty().bind(settingsService.modelLoadedProperty().not());

    }

    private void initModelChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose model to import");
        fileChooser.setInitialDirectory(new File("C:/Users/Kristina/IdeaProjects/DigitalniMediji/src/main/resources"));

        cbMouseControl.setSelected(true);
        cbMouseControl.setDisable(true);

        btnBrowse.setOnAction(event -> {
            File modelFile = fileChooser.showOpenDialog(primaryStage);
            try {
                model = ResourceManager.loadModel(modelFile);
                meshes = ResourceManager.loadModelIntoMesh(modelFile);
                model.getChildren().addAll(meshes);
                if(model!=null) {
                    resetValues();
                    fillModelInfo(modelFile.getName());
                    scene3D = createScene3D(model);
                    settingsService.setModelLoaded(true);
                    initControls();
                }
                repaintScene3D();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        btnBrowseMat.setOnAction(event -> {
            File matFile = fileChooser.showOpenDialog(primaryStage);
            if(matFile!=null) {
                Image matImg = new Image(matFile.toURI().toString());
                tfMaterial.setText(matFile.getName());
                mat.setDiffuseMap(matImg);
                imgMatPreview.setImage(matImg);
            }

        });

        File imgSelfIllumination = new File("C:\\Users\\Kristina\\IdeaProjects\\DigitalniMediji\\src\\main\\resources\\materials\\illuminate.jpg");
        Image imgSelfIll = new Image(imgSelfIllumination.toURI().toString());
        mat.selfIlluminationMapProperty().bind(
                Bindings.when(
                        cbSelfIlluminate.selectedProperty())
                        .then(imgSelfIll)
                        .otherwise((Image) null)
        );
        File imgReflection = new File("C:\\Users\\Kristina\\IdeaProjects\\DigitalniMediji\\src\\main\\resources\\materials\\spec.jpg");
        Image imgRefl = new Image(imgReflection.toURI().toString());
        mat.specularMapProperty().bind(
                Bindings.when(
                        cbReflective.selectedProperty())
                        .then(imgRefl)
                        .otherwise((Image) null)
        );

        colorPicker.setOnAction(event -> {
            mat.setDiffuseColor(colorPicker.getValue());
        });

        colorPickerLight.setOnAction(event -> {
            model.getChildren().remove(pointLight);

            pointLight = new PointLight();
            pointLight.setColor(colorPickerLight.getValue());
            pointLight.getTransforms().add(new Translate(0,-100,50));
            pointLight.setRotationAxis(Rotate.Y_AXIS);

            Sphere sphere = new Sphere(2);
            sphere.getTransforms().setAll(pointLight.getTransforms());
            sphere.rotateProperty().bind(pointLight.rotateProperty());
            sphere.rotationAxisProperty().bind(pointLight.rotationAxisProperty());

            Node[] light = new Node[] {pointLight,sphere};
            model.getChildren().addAll(light);
            cbAnimateLight.setDisable(false);

        });

        cbAnimateLight.setDisable(true);
        cbAnimateLight.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) timer.start();
            else timer.stop();
        });

    }

    private void resetValues() {
        anchorAngleX = 0;
        anchorAngleY = 0;
        lightAdded = false;
        cbAnimateLight.setSelected(false);
        cbSelfIlluminate.setSelected(false);
        cbWireframe.setSelected(false);
        rotateX.setSelected(false);
        rotateY.setSelected(false);
        rotateZ.setSelected(false);
        cbApplyTexture.setSelected(false);
        //mat = new PhongMaterial();

    }

    private void initControls() {
        for (MeshView mesh: meshes) {
            mesh.drawModeProperty().bind(
                    Bindings.when(
                            cbWireframe.selectedProperty())
                            .then(DrawMode.LINE)
                            .otherwise(DrawMode.FILL)
            );
            mesh.cullFaceProperty().bind(
                    Bindings.when(
                            cbCullFace.selectedProperty())
                            .then(CullFace.BACK)
                            .otherwise(CullFace.NONE)
            );
            mesh.materialProperty().bind(
                    Bindings.when(
                            cbApplyTexture.selectedProperty())
                            .then(mat)
                            .otherwise((PhongMaterial) null)
            );
        }

        //translations
        sliderX.setMax(900);
        sliderX.setMin(0);
        sliderX.adjustValue(450);
        sliderX.setStyle("-fx-base: blue");
        model.translateXProperty().bind(sliderX.valueProperty());

        sliderY.setMax(900);
        sliderY.setMin(0);
        sliderY.adjustValue(450);
        sliderY.setStyle("-fx-base: blue");
        model.translateYProperty().bind(sliderY.valueProperty());

        //rotations
        rotateTransX = createRotateXAxis(model);
        rotateTransY = createRotateYAxis(model);
        rotateTransZ = createRotateZAxis(model);
        rotateX.selectedProperty().addListener(observable -> {
            if (rotateX.isSelected()) {
                rotateY.setSelected(false);
                rotateZ.setSelected(false);
                rotateTransX.play();
            } else {
                rotateTransX.pause();
            }
        });
        rotateY.selectedProperty().addListener(observable -> {
            if (rotateY.isSelected()) {
                rotateX.setSelected(false);
                rotateZ.setSelected(false);
                rotateTransY.play();
            } else {
                rotateTransY.pause();
            }
        });
        rotateZ.selectedProperty().addListener(observable -> {
            if (rotateZ.isSelected()) {
                rotateX.setSelected(false);
                rotateY.setSelected(false);
                rotateTransZ.play();
            } else {
                if(rotateTransZ != null) rotateTransZ.pause();
                model.getTransforms().remove(rotateTransZ);
                rotateTransZ = null;
            }
        });


    }

    private void fillModelInfo(String name) {
        tfModel.setText(name);
        lbFaces.setText(String.valueOf(ResourceManager.countFaces(meshes)));
        lbVertices.setText(String.valueOf(ResourceManager.countVertices(meshes)));
        lbTexCoords.setText(String.valueOf(ResourceManager.countTexCoords(meshes)));

    }

    private void repaintScene3D() {
        if(vbGroupContainer.getChildren().size()!=0)vbGroupContainer.getChildren().remove(0);
        vbGroupContainer.getChildren().add(scene3D);
    }

    private SubScene createScene3D(Group group) {

        group.translateXProperty().set(VIEWPORT_SIZE / 2);
        group.translateYProperty().set(VIEWPORT_SIZE / 2);
        group.setTranslateZ(600);
        group.setScaleX(MODEL_SCALE_FACTOR);
        group.setScaleY(MODEL_SCALE_FACTOR);
        group.setScaleZ(MODEL_SCALE_FACTOR);

        SubScene scene3d = new SubScene(group, 960, 890, true, SceneAntialiasing.BALANCED);
        scene3d.setFill(Color.WHITESMOKE);
        PerspectiveCamera camera = new PerspectiveCamera();
        //camera.setTranslateZ();
        scene3d.setCamera(camera);

        //initMouseControl(scene3d,group);
        initMouseControl(group,scene3d,Main.getPrimaryStage());

        return  scene3d;

    }

    private void initMouseControl(Group group, SubScene scene, Stage stage) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });

        stage.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            group.translateZProperty().set(group.getTranslateZ() + delta);
        });

    }

    private RotateTransition createRotateXAxis(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.X_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }

    private RotateTransition createRotateYAxis(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }

    private RotateTransition createRotateZAxis(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.Z_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }


}
