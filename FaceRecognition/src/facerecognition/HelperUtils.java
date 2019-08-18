/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Anshu Anand
 */
public class HelperUtils {
    public static final String OPENCVDEMO = "OpenCv Demo";
    public static final float VERSION = 0.1f;
    public static final String OPENCVDEMO_COMPLETE = OPENCVDEMO + " v" + VERSION;

    public static final int MINIMUM_TRAIN_SET_SIZE =100;
    public static final String CLASSIFIERS_PATH = "data";
    public static final String TRAINING_FACES_PATH = "C:\\Users\\Anshu Anand\\Desktop\\FaceRecognition\\dataSet";
    public static int TRAIN_FACE_IMAGE_HEIGHT = 140;
    public static int TRAIN_FACE_IMAGE_WIDTH = TRAIN_FACE_IMAGE_HEIGHT;
    public static Size TRAIN_FACE_IMAGE_SIZE = new Size( (double) TRAIN_FACE_IMAGE_HEIGHT, (double)TRAIN_FACE_IMAGE_HEIGHT);
    public static double FACE_RECOGNITION_THRESHOLD = 1000;
    public static double CONFIDENCE_THRESOLD = 55;
    public static int MAX_IMAGES_NUMBER_FOR_TRAINING = 5;
    public static String DEFAULT_FACE_CLASSIFIER = "/data/haarcascades/haarcascade_frontalface_alt.xml";

    public static Scalar WHITE = new Scalar(255,255,255);
    public static Scalar BLACK = new Scalar(0,0,0);

    public static float RECOGNIZED_NAME_FONT_SIZE = 0.5f;
    public static final String NOT_RECOGNIZED_FACE = "unknown";
    public static final FaceObject UNKNOWN_FACE = new FaceObject(NOT_RECOGNIZED_FACE, 0d);

    
    /**
     * This method returns filename for given file in this system.
     * @param file
     * @return 
     */
    public static String getFileName(File file) {
        String fileName = "";
        
        if (file.exists()) {
            int a = file.toString().lastIndexOf('\\');
            int b = file.toString().lastIndexOf('.');
            String str = file.toString().substring(a + 1, b);

            int c = str.lastIndexOf('.');
            str = str.substring(0, c);
            fileName = str;
        }
        
        return fileName;

    }
    
    
    /**
     * This method transforms mat to image
     * @param frame
     * @return 
     */
    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
    
    /**
     * This method resizes images
     * @param originalImage
     * @return 
     */
    public static Mat resizeFace(Mat originalImage) {
        Mat resizedImage = new Mat();
        Imgproc.resize(originalImage, resizedImage, HelperUtils.TRAIN_FACE_IMAGE_SIZE);
        return resizedImage;
    }

    /**
     * this method converts images to gray scale
     * @param image
     * @return 
     */
    public static Mat toGrayScale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    /**
     * 
     * @param image
     * @return 
     */
    public static BufferedImage getBufferedImageFromMat(Mat image) {

        // TODO: avoid this check!
//        int type = BufferedImage.TYPE_BYTE_GRAY;
//        if (image.channels() > 1) {
           int type = BufferedImage.TYPE_3BYTE_BGR;
//        }
        BufferedImage bufferedImage = new BufferedImage(image.cols(), image.rows(), type);
        byte[] b = new byte[image.channels() * image.cols() * image.rows()];
        image.get(0, 0, b);
        final byte[] targetPixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return bufferedImage;
    }

    /**
     * This method saves mat file to directory in JPG file
     * @param face
     * @param filename 
     */
    public static void saveAsJpg(Mat face, String filename) {
        try {
            ImageIO.write(HelperUtils.getBufferedImageFromMat(face), "JPG", new File(filename));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
