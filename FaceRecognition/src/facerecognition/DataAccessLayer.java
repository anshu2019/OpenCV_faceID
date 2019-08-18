/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is data access layer
 *
 * @author Anshu Anand
 */
public class DataAccessLayer {

    //private DriverManager drivermanager;
    private boolean daoInitiated;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/database";
    private static final String USER = "root";
    private static final String PWD = "anshua1";
    // jdbc Connection
    private static Connection connection = null;
    private static Statement myStmt = null;

    /**
     * This is constructor for Data Access Layer.
     */
    public DataAccessLayer() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            connection = DriverManager.getConnection(DB_URL, USER, PWD);
            myStmt = connection.createStatement();
            daoInitiated = true;

        } catch (SQLException ex) {
            Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method inserts data in DB.
     *
     * @param stu
     * @param visit
     * @return
     */
    public boolean insertToStudentDetail(Student stu, VisitInfo visit) {
        boolean result = false;
        if (stu != null) {
            try {
                // the mysql insert statement
                String query = " insert into student_detail (andrew_id, gender, name, program,last_visit,purpose_of_visit)"
                        + " values (?, ?, ?, ?,?,?)";

                PreparedStatement preparedStmt = connection.prepareStatement(query);

                preparedStmt.setString(1, stu.andrewId);
                preparedStmt.setString(2, stu.gender);
                preparedStmt.setString(3, stu.name);
                preparedStmt.setString(4, stu.program);
                preparedStmt.setDate(5, new java.sql.Date(visit.visitTime.getTime()));
                preparedStmt.setString(6, visit.visitReason);

                String query1 = " insert into student_history (visit_id,andrew_id, visit_time, reason_of_visit)" + " values (?,?, ?, ?)";
                PreparedStatement preparedStmt1 = connection.prepareStatement(query1);
                preparedStmt1.setString(1, null);
                preparedStmt1.setString(2, visit.andrewId);
                preparedStmt1.setDate(3, new java.sql.Date(visit.visitTime.getTime()));
                preparedStmt1.setString(4, visit.visitReason);

                // execute the preparedstatement
                boolean x = preparedStmt.execute();
                boolean y = preparedStmt1.execute();
                result = true;

            } catch (SQLException ex) {
                Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
                if (ex.getErrorCode() == 1062) {
                    System.out.println("Unique key violation..");
                }
            }
        }

        return result;
    }

    /**
     * This method updates visit info
     *
     * @param visit
     * @return
     */
    public boolean updateVisitInfo(VisitInfo visit) {
        boolean result = false;
        if (visit != null) {
            try {

                String query = " insert into student_history (andrew_id, visit_time, reason_of_visit)"
                        + " values (?, ?, ?)";

                PreparedStatement preparedStmt = connection.prepareStatement(query);

                preparedStmt.setString(1, visit.andrewId);
                preparedStmt.setDate(2, new java.sql.Date(visit.visitTime.getTime()));
                preparedStmt.setString(3, visit.visitReason);

                String query1 = "update student_detail set last_visit = ?,purpose_of_visit=?,no_of_visit=no_of_visit + 1 where andrew_id = ?";
                PreparedStatement preparedStmt1 = connection.prepareStatement(query1);

                preparedStmt1.setDate(1, new java.sql.Date(visit.visitTime.getTime()));
                preparedStmt1.setString(2, visit.visitReason);
                preparedStmt1.setString(3, visit.andrewId);

                // execute the preparedstatement
                preparedStmt.execute();
                preparedStmt1.execute();
                result=true;

            } catch (SQLException ex) {
                Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
                if (ex.getErrorCode() == 1062) {
                    System.out.println("Unique key violation..");
                }
            }
        }
        return result;
    }

    /**
     * This method gets student's info from data base based on andrewId.
     *
     * @param andrewId
     * @return
     */
    public ResultSet getStudentInfo(String andrewId) {
        ResultSet rs = null;
        try {
            String query = "select *from student_detail where andrew_id='" + andrewId + "'";
            rs = myStmt.executeQuery(query);

        } catch (SQLException ex) {
            Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    /**
     * This method checks if user exist in system
     *
     * @param andrewId
     * @return
     */
    public int checkUserInSystem(String andrewId) {
        int i = 0;
        if (!andrewId.equals("")) {
            try {

                String query = "SELECT COUNT(*) AS \"Number of Students\" FROM database.student_detail WHERE andrew_id='" + andrewId + "'";

                ResultSet rs = myStmt.executeQuery(query);
                while (rs.next()) {
                    i = Integer.parseInt(rs.getString("Number of Students"));
                    System.out.println(i);
                }

            } catch (SQLException ex) {
                Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return i;
    }
    
    /**
     * This method returns frequency from database
     * @param catgId
     * @param fromDt
     * @param toDt
     * @return 
     */
    public int getStudentCountFromHistoryTable(String catgId,java.sql.Date fromDt,java.sql.Date toDt) {
        int i = 0;
         //java.sql.Date frmDate= new java.sql.Date(fromDt.getTime());
         //java.sql.Date toDate= new java.sql.Date(toDt.getTime());
            try {

                String query = "SELECT COUNT(*) AS \"Number of Students\" FROM database.student_history WHERE reason_of_visit='" + catgId + "' AND visit_time BETWEEN '"+fromDt+"' AND '"+toDt+"'";

                ResultSet rs = myStmt.executeQuery(query);
                while (rs.next()) {
                    i = Integer.parseInt(rs.getString("Number of Students"));
                    System.out.println(i);
                }

            } catch (SQLException ex) {
                Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
       

        return i;
    }

    /**
     * This method returns data by gender
     * @param catgId
     * @param sex
     * @param fromDt
     * @param toDt
     * @return 
     */
   public int getStudentCountFromHistoryTableByGender(String catgId,String sex,java.sql.Date fromDt,java.sql.Date toDt) {
        int i = 0;
         //java.sql.Date frmDate= new java.sql.Date(fromDt.getTime());
         //java.sql.Date toDate= new java.sql.Date(toDt.getTime());
            try {

                String query = "SELECT COUNT(*) AS \"Number of Students\" FROM database.student_history WHERE reason_of_visit='" + catgId + "' AND visit_time BETWEEN '"+fromDt+"' AND '"+toDt+"' AND andrew_id in (select andrew_id FROM database.student_detail where gender='"+sex+"')";

                ResultSet rs = myStmt.executeQuery(query);
                while (rs.next()) {
                    i = Integer.parseInt(rs.getString("Number of Students"));
                    System.out.println(i);
                }

            } catch (SQLException ex) {
                Logger.getLogger(DataAccessLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
       

        return i;
    }
}
