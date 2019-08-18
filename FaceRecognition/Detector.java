/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 *
 * @author Anshu Anand
 */
public class Detector {

    public FaceIdRecognizer faceRecognizer;
    private CascadeClassifier faceCascade;
    private List<DetectedObject> detectedObject;
    // minimum face size
    private int absoluteFaceSize;
    private Scalar[] colors;
    
    public Detector() {

        this.faceRecognizer = new FaceIdRecognizer();
        this.faceCascade = new CascadeClassifier();
        //lbpcascade_frontalface
        this.faceCascade.load("data/haarcascades/haarcascade_frontalface_default.xml");
        this.absoluteFaceSize = 0;
        
         // defines colors for drawing the frame for detected elements
        colors = new Scalar[] {
                new Scalar(255, 0, 0),
                new Scalar(0, 255, 0),
                new Scalar(0, 0, 255),
                new Scalar(255, 0, 255),
                new Scalar(255, 255, 0),
                new Scalar(0, 255, 255),
                new Scalar(100, 100, 100),
                new Scalar(200, 200, 200),
                new Scalar(255, 99, 71)
        };
        
    }

    public String detectAndDisplay(Mat frame) {
        
        String id="";
        // init
        List<DetectedObject> detectedObjects;
        
        // loops over all the activated detectors
        // gets the elements detected by this detector
            detectedObjects = detectObject(frame);
            for (DetectedObject detectedObject: detectedObjects) {

                // gets the image transformed by the detector
                frame = detectedObject.getSourceImage();

                // if has to recognize a face
                if (detectedObject != null && detectedObject.getDetectedImageObject()!= null) {                    

                    // recognizes the face
                    FaceObject recognizedFace = faceRecognizer.recognizeFace(detectedObject.getDetectedImageObject());
                    String name;
                    if (recognizedFace == HelperUtils.UNKNOWN_FACE) {
                        name = HelperUtils.NOT_RECOGNIZED_FACE;
                    }
                    else {
                        int percentage = (int)(100 * (HelperUtils.FACE_RECOGNITION_THRESHOLD - recognizedFace.getConfidence()) / HelperUtils.FACE_RECOGNITION_THRESHOLD);
                        name = "Welcome back "+recognizedFace.getName() + "  [" + percentage + "%]";
                        id= recognizedFace.getName();                         
                    }

                    // writes the name of the recognized person (sort of embossed)
                    Point position = detectedObject.getPosition();
                    position.y -= 11;
                    position.x -= 1;
                    Imgproc.putText(frame, name, position, Core.FONT_HERSHEY_TRIPLEX, HelperUtils.RECOGNIZED_NAME_FONT_SIZE, HelperUtils.BLACK);

                    position.y += 1;
                    position.x += 1;
                    Imgproc.putText(frame, name, position, Core.FONT_HERSHEY_TRIPLEX, HelperUtils.RECOGNIZED_NAME_FONT_SIZE, colors[3]);
                }
            
        }

        return id;
    }

    /**
     * This method detects multi face .
     *
     * @param grayFrame
     * @param faces
     */
    public void detectMultiScale(Mat grayFrame, MatOfRect faces) {
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(
                this.absoluteFaceSize, this.absoluteFaceSize), new Size());
        return;
    }

    /**
     * This method returns detected faces in frame.
     * @param frame
     * @return 
     */
    public List<DetectedObject> detectObject(Mat frame) {

        detectedObject = new ArrayList<>(10);

        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(
                this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face
        Rect[] facesArray = faces.toArray();

        for (Rect rect : facesArray) {
            //create square around faces
            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
            //store all the images in the frame
            detectedObject.add(new DetectedObject(frame, new Mat(frame.clone(), rect), new Point(rect.x, rect.y)));
        }

        return detectedObject;
    }
}
