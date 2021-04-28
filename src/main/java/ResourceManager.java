import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.File;

public class ResourceManager {

    public static Group loadModel(File file) {
        try {
            Group modelRoot = new Group();

            ObjModelImporter importer = new ObjModelImporter();
            importer.read(file);

            for (MeshView view : importer.getImport()) {
                modelRoot.getChildren().add(view);
                System.out.println("Mesh view added");
            }
            return modelRoot;
        } catch (Exception e) {
            System.out.println("There was an error loading the model.");
        }
        return null;
    }

    public static MeshView[] loadModelIntoMesh(File file) throws Exception {
        if(file == null) throw new Exception("There was no file selected");
        MeshView[] modelRoot;

        ObjModelImporter importer = new ObjModelImporter();
        importer.read(file);

        modelRoot = importer.getImport();

        return modelRoot;
    }

    public static int countFaces(MeshView[] meshes) {
        int count = 0;
        TriangleMesh mesh;
        for(MeshView m: meshes) {
            mesh = (TriangleMesh) m.getMesh();
            count+=mesh.getFaces().size();
        }
        return count;
    }

    public static int countVertices(MeshView[] meshes) {
        int count = 0;
        TriangleMesh mesh;
        for(MeshView m: meshes) {
            mesh = (TriangleMesh) m.getMesh();
            count+=mesh.getPoints().size();
        }
        return count;
    }

    public static int countTexCoords(MeshView[] meshes) {
        int count = 0;
        TriangleMesh mesh;
        for (MeshView m : meshes) {
            mesh = (TriangleMesh) m.getMesh();
            count += mesh.getTexCoords().size();
        }
        return count;
    }

}
