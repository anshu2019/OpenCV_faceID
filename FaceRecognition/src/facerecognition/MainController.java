/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 * This is main controller class
 *
 * @author Anshu Anand
 */
public class MainController implements Initializable {

    //UI CONTROLS......
    //Label Fields.....
    @FXML
    private Label lblAndrewId;

    @FXML
    private Label lblName;

    @FXML
    private Label lblProgram;

    @FXML
    private Label lblGender;

    @FXML
    private Label lblLastVisit;

    @FXML
    private Label lblReasonVisit;

    @FXML
    private Label lblAnnouncement;

    @FXML
    private Label lblNoOfVisit;

    //Analytics
    @FXML
    private Label lbl1;

    @FXML
    private ChoiceBox cmbReason;

    @FXML
    private ChoiceBox cmbGender;

    @FXML
    private ChoiceBox cmbProgram;

    //Text fields
    @FXML
    private TextField txtAndrewID;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtProgram;

    @FXML
    private TextField txtGender;

    @FXML
    private TextArea txtAreaLogger;

    @FXML
    private Button cameraButton;

    @FXML
    private Button btnSaveUser;

    @FXML
    private Button btnTrainSys;

    @FXML
    private Button btnUpdateImg;

    @FXML
    private Button btnGetInfo;

    @FXML
    private Label label;

    @FXML
    private Label sucessLbl;

    @FXML
    private ImageView originalFrame;

    @FXML
    private ImageView imgViewSeal;

    @FXML
    private ImageView imgUser;

    @FXML
    private HBox panRsnInsert;

    @FXML
    private HBox panSaveInfo;

    //Analytics  control
    @FXML
    private DatePicker dtFrm1;

    @FXML
    private DatePicker dtTo1;

    @FXML
    private DatePicker dtFrm2;

    @FXML
    private DatePicker dtTo2;

    @FXML
    private BarChart barReport1;

    @FXML
    private BarChart barReport2;

    @FXML
    private Label lblChart1Msg;

    @FXML
    private Label lblChart2Msg;

    private SimpleDateFormat dateFormat;
    // a timer for acquiring the video stream
    private Timer timer;

    private Timer timer1;
    // the OpenCV object that performs the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;

    private int absoluteFaceSize;

    private Image CamStream;

    private Detector faceDetector;

    private DataAccessLayer db;

    private String logger = "";

    private String studentAndrewId = "";

    private Image studentImage = null;

    private Image unknownPrsn = null;

    private DateFormat dtFormat;

    /**
     * This method initiates the system
     */
    protected void init() {
        loadOpenCv();

        //create a detector class for face detection.
        this.faceDetector = new Detector();
        this.db = new DataAccessLayer();

        this.absoluteFaceSize = 0;
        dateFormat = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss");

        dtFormat = DateFormat.getDateInstance(DateFormat.LONG);
        //set the logo on UI.
        Image img = new Image(new File("images/seal.png").toURI().toString());
        imgViewSeal.setImage(img);

        unknownPrsn = new Image(new File("images/Unknown-person.png").toURI().toString());
        //cmbReason = new ChoiceBox();
        cmbReason.setItems(FXCollections.observableArrayList("Other", "Tution Fee", "Complaints", "Collect Assignment", "Stapler", "Meet Nershnee", "Meet laurdes"));
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other", ""));
        cmbProgram.setItems(FXCollections.observableArrayList("MSIT", "MSIM", "Other", ""));
        cmbReason.setTooltip(new Tooltip("Select the reason of visit"));
        cmbGender.setTooltip(new Tooltip("Select the gender"));
        cmbProgram.setTooltip(new Tooltip("Select the Program"));

        btnGetInfo.setVisible(false);
        panRsnInsert.setVisible(false);
        panSaveInfo.setVisible(false);

        //bind the button visibilty to andrewId.
        //btnGetInfo.visibleProperty().bind(lblAndrewId.textProperty().isEmpty());
        //bind the button visibilty to andrewId.
        lblAndrewId.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            //System.out.println(t+"====="+t1);
            if (t1.equals("")) {
                btnGetInfo.setVisible(false);
                panRsnInsert.setVisible(false);
                panSaveInfo.setVisible(false);
            } else {
                btnGetInfo.setVisible(true);
                panRsnInsert.setVisible(true);
                panSaveInfo.setVisible(true);
            }
        });

        this.systemInfo();
    }

    /**
     * This method logs system info.
     */
    private void systemInfo() {
        this.logTrace("System (Face Recognizer)  Thresold value: " + HelperUtils.FACE_RECOGNITION_THRESHOLD);
        this.logTrace("Accuracy = (Thresold - Confidence value)/Thresold * 100");
        this.logTrace("Accuracy Deviation is 3 %");
    }

    /**
     * This method load openCV dynamic link library needed for project.
     */
    protected void loadOpenCv() {

        String arch = System.getProperty("sun.arch.data.model");

        switch (arch) {
            case "64":
                System.load("C:/CV3/java/opencv_java331.dll");
                break;
            case "32":
                System.load("C:/CV3/java/opencv_java331.dll");
                break;
        }
    }

    /**
     * This method starts web cam
     */
    @FXML
    private void startWebCam() {
        this.capture = new VideoCapture(0);
        if (!this.cameraActive) {
            // start the video capture
            this.logTrace("Web cam started..");
            this.capture.open(0);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                this.timerControl("t1");

                // update the button content
                this.cameraButton.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Failed to open the camera connection...");
            }
        } else {
            this.stopCamera();
            this.logTrace("Web Cam stopped..");
        }
    }

    /**
     * This method stops camera.
     */
    private void stopCamera() {
        if (this.cameraActive) {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.cameraButton.setText("Start Camera");

            // stop the timer
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
            // release the camera
            this.capture.release();
            // clean the image area
            originalFrame.setImage(null);
        }
    }

    /**
     * This method grabs image frame.
     *
     * @return
     */
    private Image grabFrame() {
        // init everything
        Image imageToShow = null;
        Mat frame = new Mat();

        String idName = "";
        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);
                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // face detection
                    idName = this.faceDetector.detectAndDisplay(frame);
                    imageToShow = HelperUtils.mat2Image(frame);

                    //If face is recognized ....
                    if (!idName.isEmpty()) {
                        this.logTrace("Student Identified as: " + idName);

                        this.studentImage = getImageFromFolder(idName);
                        this.studentAndrewId = idName;
                    } else {
                        this.studentImage = unknownPrsn;
                        this.studentAndrewId = "";
                    }
                    // convert the Mat object (OpenCV) to Image (JavaFX)

                }
            } catch (Exception e) {
                // log the (full) error
                System.err.print("ERROR");
            }
        }
        return imageToShow;
    }

    /**
     * This method saves user data.
     */
    @FXML
    private void saveUserData() {
        sucessLbl.setText("Student Image and Data registration in Progress............");
        String stuId = txtAndrewID.getText();
        int count = 0;
        this.stopCamera();

        if (this.validateUI("NEW")) {
            count = db.checkUserInSystem(stuId);
            if (count == 0) {
                //procceed with user inertion in system.
                if (createUserDataSet()) {
                    this.logTrace("User Images are saved for training....");

                    Student stu = new Student();
                    VisitInfo visit = new VisitInfo();

                    //prepare data to be saved on Ui .
                    stu.andrewId = txtAndrewID.getText();
                    stu.gender = cmbGender.getSelectionModel().getSelectedItem().toString();
                    stu.name = txtName.getText();
                    stu.program = cmbProgram.getSelectionModel().getSelectedItem().toString();

                    visit.andrewId = txtAndrewID.getText();
                    visit.visitTime = Calendar.getInstance().getTime();
                    visit.visitReason = "Registration";

                    if (db.insertToStudentDetail(stu, visit)) {
                        this.logTrace("Student Registered successfuly.....");
                        sucessLbl.setText("Student Registered successfuly.....");
                    }
                }
            } else {
                this.logTrace("Student with AndrewId " + stuId + " already exist in system.");
                sucessLbl.setText("Student with AndrewId " + stuId + " already exist in system.");
            }
        }

    }

    /**
     * this method updates images for existing user.
     */
    @FXML
    private void updateUserImage() {
        String stuId = txtAndrewID.getText();
        int count = 0;
        this.stopCamera();
        if (this.validateUI("EXIST")) {
            count = db.checkUserInSystem(stuId);
            if (count == 0) {
                this.logTrace("Student with AndrewId " + stuId + " does not exist in system.Please insert new record.");
                sucessLbl.setText("Student with AndrewId " + stuId + " does not exist in system.Please insert new record.");
            } else {
                this.createUserDataSet();
                this.logTrace("Image Updated  successfuly.....");
                sucessLbl.setText("Image Updated successfuly.....");
            }
        }

    }

    /**
     * This method invokes training system
     */
    @FXML
    private void trainSystem() {
        this.logTrace("Face Training Started ......");
        sucessLbl.setText("Face Training Started ......");
        this.faceDetector.faceRecognizer.trainRecognizer();
        this.logTrace("Face Training Is Completed ......");
        sucessLbl.setText("Face Training Completed ......");

    }

    /**
     * This method creates user data set.
     */
    private boolean createUserDataSet() {
        boolean isDone = false;

        Mat frame = new Mat();
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();
        Mat profileImg = new Mat();

        String andrewId = txtAndrewID.getText();
        int sampleNo = 1;
        int counter = HelperUtils.MINIMUM_TRAIN_SET_SIZE;
        // check if the capture is open
        this.capture.open(0);
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.timerControl("t2");
                while (sampleNo < counter + 1) {

                    this.capture.read(frame);
                    // if the frame is not empty, process it
                    if (!frame.empty()) {
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
                        this.faceDetector.detectMultiScale(grayFrame, faces);

                        // each rectangle in faces is a face
                        Rect[] facesArray = faces.toArray();
                        Rect rect_Crop = null;

                        if (facesArray.length > 0) {
                            for (Rect rect : facesArray) {
                                Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 3);
                                rect_Crop = new Rect(rect.x, rect.y, rect.width, rect.height);
                            }
                        } else {
                            continue;
                        }

                        Mat grayRect = new Mat(grayFrame, rect_Crop);

                        if (sampleNo == 1) {
                            //Imgproc.cvtColor(frame, profileImg, Imgproc.COLOR_mRGBA2RGBA);
                            profileImg = new Mat(frame, rect_Crop);
                            Imgcodecs.imwrite("studentImage/" + andrewId + ".jpg", profileImg);
                        }
                        if (Imgcodecs.imwrite("dataSet/" + andrewId + "." + sampleNo + ".jpg", grayRect)) {
                            this.logTrace("sample" + sampleNo + "is saved");
                            sampleNo = sampleNo + 1;

                        }

                        System.out.println("in the image loop");
                    }

                    if (sampleNo == counter + 1) {
                        System.out.println("100 samples are saved.");
                        this.logTrace("sample" + counter + " saved successfuly.");
                        isDone = true;
                        // stop the timer
                        if (this.timer1 != null) {
                            this.timer1.cancel();
                            this.timer1 = null;
                        }
                        // release the camera
                        this.capture.release();
                        this.startWebCam();
                    }
                }

            } catch (Exception e) {
                // log the (full) error
                System.err.print("ERROR");
            }
        }
        return isDone;
    }

    /**
     * This method clears data on screen.
     */
    @FXML
    private void clearData() {
        this.txtAndrewID.clear();

        this.txtName.clear();
        this.sucessLbl.setText("");
        this.logTrace("all the fields are reset.");
    }

    /**
     * This method validates data on UI
     *
     * @param type
     */
    private boolean validateUI(String type) {
        boolean valid = true;
        if (type.equals("NEW")) {
            if (this.txtAndrewID.getText().equals("")) {
                this.sucessLbl.setText("Andrew Id is required Field.");
                this.logTrace("Andrew Id is required Field.");
                valid = false;
            } else if (this.txtName.getText().equals("")) {
                this.sucessLbl.setText("Name is required field");
                this.logTrace("Gender is required Field");
                valid = false;
            } else if (this.cmbProgram.getSelectionModel().isEmpty()) {
                this.sucessLbl.setText("Program is required Field");
                this.logTrace("Gender is required Field");
                valid = false;
            } else if (this.cmbGender.getSelectionModel().isEmpty()) {
                this.sucessLbl.setText("Gender is required Field");
                this.logTrace("Gender is required Field");
                valid = false;
            }
        } else if (type.equals("EXIST")) {
            if (this.txtAndrewID.getText().equals("")) {
                this.sucessLbl.setText("Andrew Id is required Field.");
                this.logTrace("Andrew Id is required Field.");
                valid = false;
            }
        }
        return valid;

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    /**
     * This method controls timer on UI
     */
    private void timerControl(String t) {
        if (t.equals("t1")) {
            // grab a frame every 20 ms (50 frames/sec)
            TimerTask frameGrabber = new TimerTask() {
                @Override
                public void run() {
                    //grab image from webcam
                    CamStream = grabFrame();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            originalFrame.setImage(CamStream);
                            originalFrame.setFitWidth(600);
                            originalFrame.setPreserveRatio(true);

                            //set recognized face on UI...
                            imgUser.setImage(studentImage);
                            lblAndrewId.setText(studentAndrewId);
                        }
                    });
                }
            };
            this.timer = new Timer();
            this.timer.schedule(frameGrabber, 0, 20);

        } else if (t.equals("t2")) {
            // grab a frame every 20 ms (50 frames/sec)
            TimerTask frameGrabber = new TimerTask() {
                @Override
                public void run() {
                    CamStream = captureFrame();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            originalFrame.setImage(CamStream);
                            originalFrame.setFitWidth(600);
                            originalFrame.setPreserveRatio(true);
                        }
                    });
                }
            };
            this.timer1 = new Timer();
            this.timer1.schedule(frameGrabber, 0, 20);
        }

    }

    /**
     * This method is used to capture image.
     *
     * @return
     */
    private Image captureFrame() {
        // init everything
        Image imageToShow = null;
        Mat frame = new Mat();
        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);
                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // convert the Mat object (OpenCV) to Image (JavaFX)
                    imageToShow = HelperUtils.mat2Image(frame);
                }

            } catch (Exception e) {
                System.err.print("ERROR");
            }
        }

        return imageToShow;
    }

    /**
     * This method writes the log
     *
     * @param str
     */
    private void logTrace(String str) {
        logger += str + "\n";
        txtAreaLogger.setText(logger);
    }

    /**
     * This method pulls student image from directory
     *
     * @param andrewID
     * @return
     */
    private Image getImageFromFolder(String andrewID) {
        Image img = null;
        img = new Image(new File("studentImage/" + andrewID + ".jpg").toURI().toString());
        return img;

    }

    /**
     * This method populates student info.
     */
    @FXML
    private void populateStudentInfo() {
        ResultSet rs;
        String andrewId = lblAndrewId.getText();
        if (!andrewId.equals("")) {
            rs = db.getStudentInfo(andrewId);
            if (rs != null) {
                try {
                    while (rs.next()) {
                        lblAndrewId.setText(rs.getString("andrew_id"));
                        lblAnnouncement.setText(rs.getString("announcement"));
                        lblGender.setText(rs.getString("gender"));
                        lblName.setText(rs.getString("name"));
                        lblNoOfVisit.setText(rs.getString("no_of_visit"));
                        lblLastVisit.setText(dtFormat.format(rs.getDate("last_visit")));
                        lblProgram.setText(rs.getString("program"));
                        // dtFormat
                        lblReasonVisit.setText(rs.getString("purpose_of_visit"));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    /**
     * This method updates visit for existing students
     */
    @FXML
    private void updateVisitInfo() {

        VisitInfo visit = new VisitInfo();
        String visitReason = cmbReason.getSelectionModel().getSelectedItem().toString();
        visit.andrewId = lblAndrewId.getText();
        visit.visitTime = Calendar.getInstance().getTime();
        visit.visitReason = visitReason;

        if (db.updateVisitInfo(visit)) {
            this.logTrace("Visit Info Registered successfuly.....");
            sucessLbl.setText("Visit Info Registered successfuly.....");
        }
    }

    @FXML
    private void openAnalyticsWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("Analytics.fxml"));
            /* 
         * if "fx:controller" is not set in fxml
         * fxmlLoader.setController(NewWindowController);
             */
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setTitle("Analytics Report");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    /**
     * This method generates report1
     */
    @FXML
    private void genearteReport1() {

        DataAccessLayer db1 = new DataAccessLayer();
        if (dtFrm1.getValue() != null && dtTo1.getValue() != null) {
            java.sql.Date frmDt = java.sql.Date.valueOf(dtFrm1.getValue());
            java.sql.Date toDt = java.sql.Date.valueOf(dtTo1.getValue());

            if (frmDt != null && toDt != null) {
                lblChart1Msg.setText("");
                int x1 = db1.getStudentCountFromHistoryTable("Registration", frmDt, toDt);
                int x2 = db1.getStudentCountFromHistoryTable("Tution Fee", frmDt, toDt);
                int x3 = db1.getStudentCountFromHistoryTable("Complaints", frmDt, toDt);
                int x4 = db1.getStudentCountFromHistoryTable("Collect Assignment", frmDt, toDt);
                int x5 = db1.getStudentCountFromHistoryTable("Stapler", frmDt, toDt);
                int x6 = db1.getStudentCountFromHistoryTable("Meet Nershnee", frmDt, toDt);
                int x7 = db1.getStudentCountFromHistoryTable("Meet laurdes", frmDt, toDt);
                XYChart.Series<String, Number> Series = new XYChart.Series<>();

                //category.setCategories(FXCollections.<String>observableArrayList(Arrays.asList("DOCTOR", "TECHNICIAN", "TECHNICIAN", "ENGINEER")));
                Series.getData().add(new XYChart.Data<>("Registration", x1));
                Series.getData().add(new XYChart.Data<>("Tution Fee", x2));
                Series.getData().add(new XYChart.Data<>("Complaints", x3));
                Series.getData().add(new XYChart.Data<>("Collect Assignment", x4));
                Series.getData().add(new XYChart.Data<>("Stapler", x5));
                Series.getData().add(new XYChart.Data<>("Meet Nershnee", x6));
                Series.getData().add(new XYChart.Data<>("Meet laurdes", x7));
                barReport1.getData().add(Series);
            }
        } else {
            lblChart1Msg.setText("Please enter correct date before generating date");
        }
        db1 = null;
    }

    /**
     * This method generates report2
     */
    @FXML
    private void genearteReport2() {
        DataAccessLayer db1 = new DataAccessLayer();
        String m = "Male";
        String f = "Female";
        if (dtFrm2.getValue() != null && dtTo2.getValue() != null) {
            java.sql.Date frmDt = java.sql.Date.valueOf(dtFrm2.getValue());
            java.sql.Date toDt = java.sql.Date.valueOf(dtTo2.getValue());

            if (frmDt != null && toDt != null) {
                lblChart2Msg.setText("");
                //male data
                int m1 = db1.getStudentCountFromHistoryTableByGender("Registration", m, frmDt, toDt);
                int m2 = db1.getStudentCountFromHistoryTableByGender("Tution Fee", m, frmDt, toDt);
                int m3 = db1.getStudentCountFromHistoryTableByGender("Complaints", m, frmDt, toDt);
                int m4 = db1.getStudentCountFromHistoryTableByGender("Collect Assignment", m, frmDt, toDt);
                int m5 = db1.getStudentCountFromHistoryTableByGender("Stapler", m, frmDt, toDt);
                int m6 = db1.getStudentCountFromHistoryTableByGender("Meet Nershnee", m, frmDt, toDt);
                int m7 = db1.getStudentCountFromHistoryTableByGender("Meet laurdes", m, frmDt, toDt);
                //female data
                int f1 = db1.getStudentCountFromHistoryTableByGender("Registration", f, frmDt, toDt);
                int f2 = db1.getStudentCountFromHistoryTableByGender("Tution Fee", f, frmDt, toDt);
                int f3 = db1.getStudentCountFromHistoryTableByGender("Complaints", f, frmDt, toDt);
                int f4 = db1.getStudentCountFromHistoryTableByGender("Collect Assignment", f, frmDt, toDt);
                int f5 = db1.getStudentCountFromHistoryTableByGender("Stapler", f, frmDt, toDt);
                int f6 = db1.getStudentCountFromHistoryTableByGender("Meet Nershnee", f, frmDt, toDt);
                int f7 = db1.getStudentCountFromHistoryTableByGender("Meet laurdes", f, frmDt, toDt);

                XYChart.Series<String, Number> Series = new XYChart.Series<>();

                //category.setCategories(FXCollections.<String>observableArrayList(Arrays.asList("DOCTOR", "TECHNICIAN", "TECHNICIAN", "ENGINEER")));
                Series.getData().add(new XYChart.Data<>("Registration/MAN", m1));
                Series.getData().add(new XYChart.Data<>("Registration/WOMAN", f1));
                Series.getData().add(new XYChart.Data<>("Tution Fee/MAN", m2));
                Series.getData().add(new XYChart.Data<>("Tution Fee/WOMAN", f2));
                Series.getData().add(new XYChart.Data<>("Complaints/MAN", m3));
                Series.getData().add(new XYChart.Data<>("Complaints/WOMAN", f3));
                Series.getData().add(new XYChart.Data<>("Collect Assignment/MAN", m4));
                Series.getData().add(new XYChart.Data<>("Collect Assignment/WOMAN", f4));
                Series.getData().add(new XYChart.Data<>("Stapler/MAN", m5));
                Series.getData().add(new XYChart.Data<>("Stapler/WOMAN", f5));
                Series.getData().add(new XYChart.Data<>("Meet Nershnee/MAN", m6));
                Series.getData().add(new XYChart.Data<>("Meet Nershnee/WOMAN", f6));
                Series.getData().add(new XYChart.Data<>("Meet laurdes/MAN", m7));
                Series.getData().add(new XYChart.Data<>("Meet laurdes/WOMAN", f7));
                barReport2.getData().add(Series);
            }
        } else {
            lblChart2Msg.setText("Please enter correct date before generating date");
        }
        db1 = null;

    }

}
