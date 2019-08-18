/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition;

/**
 *
 * @author Anshu Anand
 */
public class FaceObject {

    private final String name;
    private final Double confidence;

    public FaceObject(String name, Double confidence) {
        this.confidence = confidence;
        this.name = name;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getName() {
        return name;
    }

}
