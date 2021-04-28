import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SettingsService {

    private BooleanProperty mouseControlEnabled = new SimpleBooleanProperty();
    private BooleanProperty wireframe = new SimpleBooleanProperty();
    private BooleanProperty cullFace = new SimpleBooleanProperty();
    private BooleanProperty modelLoaded = new SimpleBooleanProperty();

    public SettingsService() {
        //default settings
        mouseControlEnabled.set(true);
        wireframe.set(false);
        cullFace.set(false);
    }

    public boolean isMouseControlEnabled() {
        return mouseControlEnabled.get();
    }

    public BooleanProperty mouseControlEnabledProperty() {
        return mouseControlEnabled;
    }

    public void setMouseControlEnabled(boolean mouseControlEnabled) {
        this.mouseControlEnabled.set(mouseControlEnabled);
    }

    public boolean isWireframe() {
        return wireframe.get();
    }

    public BooleanProperty wireframeProperty() {
        return wireframe;
    }

    public void setWireframe(boolean wireframe) {
        this.wireframe.set(wireframe);
    }

    public boolean isCullFace() {
        return cullFace.get();
    }

    public BooleanProperty cullFaceProperty() {
        return cullFace;
    }

    public void setCullFace(boolean cullFace) {
        this.cullFace.set(cullFace);
    }

    public boolean isModelLoaded() {
        return modelLoaded.get();
    }

    public BooleanProperty modelLoadedProperty() {
        return modelLoaded;
    }

    public void setModelLoaded(boolean modelLoaded) {
        this.modelLoaded.set(modelLoaded);
    }
}
