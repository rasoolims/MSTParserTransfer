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
public class AveragedPerceptron extends OnlineClassifier implements Serializable {
    HashMap<String, Double> weights;
    HashMap<String, Double> avgWeights;

    public AveragedPerceptron() {
        weights = new HashMap<String, Double>(1000000);
        avgWeights = new HashMap<String, Double>(1000000);
        iteration = 1;
    }

    @Override
    public void updateWeight(String feature, double change){
        if(!weights.containsKey(feature)){
            weights.put(feature,change);
        }  else{
            weights.put(feature,weights.get(feature)+change);
        }

        if(!avgWeights.containsKey(feature)){
            avgWeights.put(feature,iteration*change);
        }  else{
            avgWeights.put(feature,avgWeights.get(feature)+iteration*change);
        }
    }

    @Override
    public void saveModel(String modelPath) throws  Exception {
        HashMap<String, Double> finalAverageWeight = new HashMap<String, Double>(avgWeights.size());

        for (String feat : weights.keySet()) {
            double newValue = weights.get(feat) - (avgWeights.get(feat) / iteration);
            if (newValue != 0.0)
                finalAverageWeight.put(feat, newValue);
        }
        FileOutputStream fos = new FileOutputStream(modelPath);
        GZIPOutputStream gz = new GZIPOutputStream(fos);

        ObjectOutput writer = new ObjectOutputStream(gz);
        
        writer.writeObject(finalAverageWeight);
        writer.flush();
        writer.close();
    }

    @Override
    public OnlineClassifier loadModel(String modelPath) throws  Exception {
        FileInputStream fos = new FileInputStream(modelPath);
        GZIPInputStream gz = new GZIPInputStream(fos);

        ObjectInputStream reader = new ObjectInputStream(gz);
        HashMap<String, Double> avgWeights= (HashMap<String, Double>)reader.readObject();

        AveragedPerceptron averagedPerceptron=new AveragedPerceptron();
        averagedPerceptron.avgWeights=avgWeights;

        return averagedPerceptron;
    }

    @Override
    public double score(ArrayList<String> features,boolean decode){
        double score=0.0;
            HashMap<String,Double> map;
            if(decode)
                map= avgWeights;
            else
                map= weights;
        for(String feature:features){
            if(map.containsKey(feature))
                    score+=map.get(feature);
        }
        return score;
    }

    @Override
    public int size(){
        return avgWeights.size();
    }
}
