package Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 9/16/14
 * Time: 11:26 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class AveragedPerceptron implements Serializable {
    public HashMap<String,Integer> labelMap;
    HashMap<String, double[]> weights;
    HashMap<String, double[]> avgWeights;
    ArrayList<String> possibleLabels;
    HashMap<String,String[]> brownClusters;
    int iteration;

    HashMap<String, double[]> sharedWeights;
    HashMap<String, double[]> avgSharedWeights;
    int sharedRuleSize;
    
    public AveragedPerceptron(ArrayList<String> possibleLabels, int sharedRuleSize) {
        this.possibleLabels = possibleLabels;
        labelMap = new HashMap<String, Integer>();
        for (int i=0;i<possibleLabels.size();i++)
            labelMap.put(possibleLabels.get(i),i);
        weights = new HashMap<String, double[]>();
        avgWeights = new HashMap<String, double[]>();
        iteration = 1;
        brownClusters = new HashMap<String, String[]>();
        
        this.sharedRuleSize = sharedRuleSize;
        sharedWeights = new HashMap<String, double[]>();
        avgSharedWeights = new HashMap<String, double[]>();
    }

    public static AveragedPerceptron loadModel(String modelPath) throws Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        HashMap<String, double[]> avgWeights = (HashMap<String, double[]>) reader.readObject();
        HashMap<String, double[]> avgSharedWeights = (HashMap<String, double[]>) reader.readObject();
        int sharedRuleSize = reader.readInt();
        ArrayList<String> labels = (ArrayList<String>) reader.readObject();
        HashMap<String, String[]> bc = (HashMap<String, String[]>) reader.readObject();
        AveragedPerceptron averagedPerceptron = new AveragedPerceptron(labels,sharedRuleSize);
        averagedPerceptron.avgWeights = avgWeights;
        averagedPerceptron.avgSharedWeights = avgSharedWeights;
        averagedPerceptron.brownClusters = bc;

        return averagedPerceptron;
    }

    public void updateWeight(String feature, double change, String label){
        int labelIndex = labelMap.get(label);
        double[] w = new double[possibleLabels.size()];
        if (weights.containsKey(feature)) {
            w = weights.get(feature);
        } else {
            weights.put(feature, w);
        }
        w[labelIndex] += change;

        double[] aw = new double[possibleLabels.size()];
        if (avgWeights.containsKey(feature)) {
            aw = avgWeights.get(feature);
        } else {
            avgWeights.put(feature, aw);
        }
        aw[labelIndex] += iteration * change;
    }

    public void updateSharedWeight(String feature, double change, int labelIndex){
        double[] w = new double[sharedRuleSize];
        if (sharedWeights.containsKey(feature)) {
            w = sharedWeights.get(feature);
        } else {
            sharedWeights.put(feature, w);
        }
        w[labelIndex] += change;

        double[] aw = new double[sharedRuleSize];
        if (avgSharedWeights.containsKey(feature)) {
            aw = avgSharedWeights.get(feature);
        } else {
            avgSharedWeights.put(feature, aw);
        }
        aw[labelIndex] += iteration * change;
    }


    public  void incrementIteration(){
        iteration++;
    }

    public void saveModel(String modelPath, ArrayList<String> possibleLabels) throws  Exception {
        HashMap<String, double[]> finalAverageWeight = new HashMap<String, double[]>();

        for (String feat : weights.keySet()) {
            double[] w = weights.get(feat);
            double[] aw = avgWeights.get(feat);
            double[] newValue = new double[w.length];
            for (int i = 0; i < w.length; i++) {
                newValue[i] = w[i] - (aw[i] / iteration);
            }
            finalAverageWeight.put(feat, newValue);
        }

        HashMap<String, double[]> finalAverageSharedWeight = new HashMap<String, double[]>();

        for (String feat : sharedWeights.keySet()) {
            double[] w = sharedWeights.get(feat);
            double[] aw = avgSharedWeights.get(feat);
            double[] newValue = new double[w.length];
            for (int i = 0; i < w.length; i++) {
                newValue[i] = w[i] - (aw[i] / iteration);
            }
            finalAverageSharedWeight.put(feat, newValue);
        }
        
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);

        writer.writeObject(finalAverageWeight);
        writer.writeObject(finalAverageSharedWeight);
        writer.writeInt(sharedRuleSize);
        writer.writeObject(possibleLabels);
        writer.writeObject(brownClusters);

        writer.flush();
        writer.close();
    }

    public double[] score(ArrayList<String> features, boolean decode) {
        double[] scores = new double[possibleLabels.size()];
        HashMap<String, double[]> map;
        if (decode)
            map = avgWeights;
        else
            map = weights;
        for (String feature : features) {
            if (map.containsKey(feature)) {
                double[] w = map.get(feature);
                for (int k = 0; k < possibleLabels.size(); k++)
                    scores[k] += w[k];
            }
        }
        return scores;
    }

    public double sharedScore(ArrayList<String> features, boolean decode, int index) {
        double scores = 0f;
        HashMap<String, double[]> map;
        if (decode)
            map = avgSharedWeights;
        else
            map = sharedWeights;
        for (String feature : features) {
            if (map.containsKey(feature)) {
                double[] w = map.get(feature);
                scores += w[index] + w[index + 12];
            }
        }
        return scores;
    }

    public double[] sharedScores(ArrayList<String> features, boolean decode) {
        double[] scores = new double[sharedRuleSize/2];
        HashMap<String, double[]> map;
        if (decode)
            map = avgSharedWeights;
        else
            map = sharedWeights;
        for (String feature : features) {
            if (map.containsKey(feature)) {
                double[] w = map.get(feature);
                for(int index =0;index<sharedRuleSize/2;index++) {
                    scores[index] += w[index] + w[index + 12];
                }
            }
        }
        return scores;
    }

    public ArrayList<String> getPossibleLabels() {
        return possibleLabels;
    }

    public HashMap<String, String[]> getBrownClusters() {
        return brownClusters;
    }

    public void readBrownClusters(String path) throws  Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;

        brownClusters = new HashMap<String, String[]>();
        while ((line = reader.readLine()) != null) {
            String[] spl = line.trim().split("\t");
            if (spl.length > 2) {
                String[] fb = new String[3];
                fb[0] = spl[0];
                String word = spl[1];
                fb[1] = spl[0].length() > 4 ? spl[0].substring(0, 4) : spl[0];
                fb[2] = spl[0].length() > 6 ? spl[0].substring(0, 6) : spl[0];
                brownClusters.put(word, fb);
            }
        }
    }
}
