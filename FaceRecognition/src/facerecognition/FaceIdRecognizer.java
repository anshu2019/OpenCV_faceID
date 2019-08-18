/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.nashorn.internal.runtime.logging.Loggable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.face.FaceRecognizer;

import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Anshu Anand
 */
public class FaceIdRecognizer {

    private List images;
    private MatOfInt labelsBuffer;
    private FaceRecognizer faceRecognizer;
    private Loggable logger;
    Map<Integer, String> idToNameMapping = null;
    private int[] labels;
    private double[] confidence;

    public FaceIdRecognizer() {
        
        this.faceRecognizer = LBPHFaceRecognizer.create();
        //this.trainRecognizer();

    }

    /**
     * This is method to load dataset 
     */
    private void loadDataSet() {
        try {
            String folderPath = HelperUtils.TRAINING_FACES_PATH;

            File folder = new File(folderPath);
            File[] listOfFiles = folder.listFiles();
            //create hasmap for all the file names .
            idToNameMapping = createMapping(listOfFiles);

            //store images in a arraylist
            images = new ArrayList(listOfFiles.length);

            labelsBuffer = new MatOfInt(new int[listOfFiles.length]);

            int counter = 0;

            for (File file : listOfFiles) {
                System.out.println(file.toString());
                // reads the training image in grayscale
                Mat img = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

                // gets the id of this image
                String name = HelperUtils.getFileName(file);
                int labelId = getIdFromImage(name,idToNameMapping);

                // sets the image
                images.add(img);
                labelsBuffer.put(counter++, 0, labelId);

            }

            labels = new int[idToNameMapping.size()];
            confidence = new double[idToNameMapping.size()];

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }
        
        //Once dataset is load , train recogniser.
        

    }

    public void trainRecognizer() {
        this.loadDataSet();
        if(images.size()>0){
            System.out.println("Face training ");
        this.faceRecognizer.train(images, labelsBuffer);
        this.faceRecognizer.save("traineddata");
            System.out.println("");
        }
    }

    public void changeRecognizer(org.opencv.face.FaceRecognizer faceRecognizer) {
        this.faceRecognizer = faceRecognizer;
        trainRecognizer();
    }

    /**
     *
     * @param face
     * @return
     */
    public FaceObject recognizeFace(Mat face) {

        if (face == null) {
            return HelperUtils.UNKNOWN_FACE;
        }

        Mat resizedGrayFace = HelperUtils.toGrayScale(HelperUtils.resizeFace(face));
        //this.faceRecognizer.load("traineddata");
        this.faceRecognizer.predict(resizedGrayFace, labels, confidence);
        System.out.println("label"+labels[0]);
        System.out.println("confidence"+confidence[0]);
        if (confidence[0] > HelperUtils.CONFIDENCE_THRESOLD) {
            return new FaceObject(idToNameMapping.get(labels[0]), confidence[0]);
        }

        return HelperUtils.UNKNOWN_FACE;
    }

    /**
     *
     * @param trainingDir
     * @return
     */
    private File[] getImagesFiles(File trainingDir) {

        FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        return trainingDir.listFiles(imgFilter);
    }

    /**
     *
     * @param filename
     * @param idToNameMapping
     * @return
     */
    private int getIdFromImage(String filename, Map<Integer, String> idToNameMapping) {
        String name = filename;
        return idToNameMapping.keySet()
                .stream()
                .filter(id -> idToNameMapping.get(id).equals(name))
                .findFirst()
                .orElse(-1);
    }

    /**
     *This method creates a hash map for file based on name.
     * @param files
     * @return
     */
    private Map<Integer, String> createMapping(File[] files) {

        Map<Integer, String> idToNameMap = new HashMap<>();
        int idCounter = 0;
        for (File file : files) {

            String name = HelperUtils.getFileName(file);
            
            if (!idToNameMap.values().contains(name)) {
                idToNameMap.put(idCounter++, name);
            }

        }

        return idToNameMap;
    }

    
}
