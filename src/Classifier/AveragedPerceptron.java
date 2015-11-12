package Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    HashMap<String, Double>[] weights;
    HashMap<String, Double>[] avgWeights;
    ArrayList<String> possibleLabels;      
    public HashMap<String,Integer> labelMap;
    HashMap<String,String[]> brownClusters;
    int iteration;
    
    public AveragedPerceptron(ArrayList<String> possibleLabels) {
        this.possibleLabels = possibleLabels;
        labelMap = new HashMap<String, Integer>();
        for (int i=0;i<possibleLabels.size();i++)
            labelMap.put(possibleLabels.get(i),i);
        weights = new HashMap[possibleLabels.size()];
        avgWeights = new HashMap[possibleLabels.size()];
        
        for(int i=0;i<weights.length;i++) {
            weights[i] = new HashMap<String, Double>(1000);
            avgWeights[i] = new HashMap<String, Double>(1000);
        }
        iteration = 1;
        brownClusters = new HashMap<String, String[]>();
    }

    public void updateWeight(String feature, double change, String label){
        int labelIndex = labelMap.get(label);
        if(!weights[labelIndex].containsKey(feature)){
            weights[labelIndex].put(feature, change);
        }  else{
            weights[labelIndex].put(feature, weights[labelIndex].get(feature) + change);
        }

        if(!avgWeights[labelIndex].containsKey(feature)){
            avgWeights[labelIndex].put(feature, iteration * change);
        }  else{
            avgWeights[labelIndex].put(feature, avgWeights[labelIndex].get(feature) + iteration * change);
        }
    }
    
    public  void incrementIteration(){
        iteration++;
    }

    public void saveModel(String modelPath, ArrayList<String> possibleLabels) throws  Exception {
        HashMap<String, Double>[] finalAverageWeight = new HashMap[weights.length];
        for(int i=0;i<finalAverageWeight.length;i++)
            finalAverageWeight[i]= new HashMap<String, Double>(weights[i].size());

        for(int i=0;i<weights.length;i++) {
            for (String feat : weights[i].keySet()) {
                double newValue = weights[i].get(feat) - (avgWeights[i].get(feat) / iteration);
                if (newValue != 0.0)
                    finalAverageWeight[i].put(feat, newValue);
            }
        }
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);
        
        writer.writeObject(finalAverageWeight);
        writer.writeObject(possibleLabels);
        writer.writeObject(brownClusters);

        writer.flush();
        writer.close();
    }

    public static AveragedPerceptron loadModel(String modelPath) throws  Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        HashMap<String, Double>[] avgWeights= (HashMap[])reader.readObject();
        ArrayList<String> labels =  (ArrayList<String>)reader.readObject();
        HashMap<String,String[]> bc =(HashMap<String,String[]>)reader.readObject();
        AveragedPerceptron averagedPerceptron=new AveragedPerceptron(labels);
        averagedPerceptron.avgWeights=avgWeights;
        averagedPerceptron.brownClusters = bc;

        return averagedPerceptron;
    }

    public double score(ArrayList<String> features,boolean decode, String label){
        int labelIndex = labelMap.get(label);
        double score=0.0;
            HashMap<String,Double> map;
            if(decode)
                map= avgWeights[labelIndex];
            else
                map= weights[labelIndex];
        for(String feature:features){
            if(map.containsKey(feature))
                    score+=map.get(feature);
        }
        return score;
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
