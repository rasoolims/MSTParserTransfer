package Trainer;

import Accessories.TreebankReader;
import Classifier.AveragedPerceptron;
import Decoder.FeatureExtractor;
import Decoder.GraphBasedParser;
import Structures.Sentence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 9/16/14
 * Time: 11:28 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class PartialTreeTrainer {

    public static void standardTrain(ArrayList<Sentence> trainSentences, ArrayList<Sentence> devSentences, ArrayList<String> possibleLabels,
                             AveragedPerceptron onlineClassifier, String modelPath, int maxIter) throws Exception {

        HashMap<String,String[]> brownClusters = (onlineClassifier).getBrownClusters();
        System.out.println("number of training instances: " + trainSentences.size());
        for (int iter = 0; iter < maxIter; iter++) {
            int numDep = 0;
            double correct = 0;
            GraphBasedParser trainParser = new GraphBasedParser(onlineClassifier, possibleLabels);

            System.out.println("*********************************************************");
            System.out.println("iteration: " + iter);
            int senCount = 0;
            for (Sentence sentence : trainSentences) {
                senCount++;
                if (senCount % 1000 == 0) {
                    System.out.print(senCount + "...");
                }
                Sentence parseTree = trainParser.eisner1stOrder(sentence, false,brownClusters);

                for (int ch = 1; ch < sentence.length(); ch++) {
                    numDep++;
                    // finding the best head
                    int goldHead = sentence.head(ch);
                    String goldLabel = sentence.label(ch);
                    int argmax = parseTree.head(ch);
                    String bestLabel = parseTree.label(ch);

                    if (argmax != goldHead || !bestLabel.equals(goldLabel)) {
                        ArrayList<String> predictedFeatures = FeatureExtractor.extract1stOrderFeatures(sentence, argmax, ch,brownClusters);
                        ArrayList<String> goldFeatures = FeatureExtractor.extract1stOrderFeatures(sentence, goldHead, ch,brownClusters);

                        if(argmax!=goldHead) {
                            for (String predicted : predictedFeatures) {
                                onlineClassifier.updateWeight(predicted, -1, bestLabel);
                                onlineClassifier.updateWeight(predicted, -1, "");
                                
                                int index= FeatureExtractor.retriveLangIdex(bestLabel,sentence.pos(ch),sentence.pos(argmax),sentence.getLanguageID());
                                if(index>=0){
                                    onlineClassifier.updateSharedWeight(predicted, -1, index);
                                    onlineClassifier.updateSharedWeight(predicted, -1, index + 12);
                                }
                            }
                            for (String gold : goldFeatures)  {
                                onlineClassifier.updateWeight(gold, 1,goldLabel);
                                onlineClassifier.updateWeight(gold, 1,"");

                                int index= FeatureExtractor.retriveLangIdex(goldLabel,sentence.pos(ch),sentence.pos(goldHead),sentence.getLanguageID());
                                if(index>=0){
                                    onlineClassifier.updateSharedWeight(gold, 1, index);
                                    onlineClassifier.updateSharedWeight(gold, 1, index + 12);
                                }
                            }
                        }
                    } else {
                        correct++;
                    }
                }
                onlineClassifier.incrementIteration();
            }
            System.out.println("");
            double accuracy = 100.0 * correct / numDep;
            System.out.println("accuracy : " + accuracy);

            System.out.print("\nsaving current model...");
            onlineClassifier.saveModel(modelPath + "_" + (iter+1), possibleLabels);
            System.out.println("done!");

            System.out.print("loading current model...");
            AveragedPerceptron avgPerceptron = onlineClassifier.loadModel(modelPath + "_" + (iter+1));
            System.out.println("done!");

            GraphBasedParser parser = new GraphBasedParser(avgPerceptron, possibleLabels);

            int labelCorrect = 0;
            int unlabelCorrect = 0;
            int allDeps = 0;

            System.out.print("\nParsing dev file with Eisner 1st order algorithm...");
            labelCorrect = 0;
            unlabelCorrect = 0;
            allDeps = 0;
            senCount = 0;
            long start = System.currentTimeMillis();
            for (Sentence sentence : devSentences) {
                Sentence parseTree = parser.eisner1stOrder(sentence, true,brownClusters);
                senCount++;
                if (senCount % 100 == 0) {
                    System.out.print(senCount + "...");
                }

                for (int ch = 1; ch < sentence.length(); ch++) {
                    if (sentence.hasHead(ch) && !sentence.getTags()[ch].equals(".")) {
                        allDeps++;
                        int goldHead = sentence.head(ch);
                        String goldLabel = sentence.label(ch);
                        int argmax = parseTree.head(ch);

                        String bestLabel = parseTree.label(ch);

                        if (argmax == goldHead) {
                            unlabelCorrect++;
                            if (bestLabel.equals(goldLabel))
                                labelCorrect++;
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            double timeSec = (1.0 * (end - start)) / devSentences.size();
            System.out.println("");
            System.out.println("time for each sentence: " + timeSec);


            double labeledAccuracy = 100.0 * labelCorrect / allDeps;
            double unlabeledAccuracy = 100.0 * unlabelCorrect / allDeps;
            System.out.println(String.format("unlabeled: %s labeled: %s", unlabeledAccuracy, labeledAccuracy));
            avgPerceptron = null;
        }
    }
}
