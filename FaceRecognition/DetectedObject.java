package facerecognition;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class DetectedObject {

    private Mat sourceImage;
    private Mat detectedImageObject;
    private Point position;

    public DetectedObject(Mat sourceImage, Mat detectedImageElement, Point position) {

        this.sourceImage = sourceImage;
        this.detectedImageObject = detectedImageElement;
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public Mat getSourceImage() {
        return sourceImage;
    }

    public Mat getDetectedImageObject() {
        return detectedImageObject;
    }
}
