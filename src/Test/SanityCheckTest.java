package Test;

import Accessories.TreebankReader;
import Classifier.AveragedPerceptron;
import Decoder.GraphBasedParser;
import Structures.Sentence;
import Trainer.PartialTreeTrainer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * User: Mohammad Sadegh Rasooli
 * Date: 9/16/14
 * Time: 11:12 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class SanityCheckTest {
    public static void main(String[] args) throws Exception {
        String trainPath = "/Users/msr/Documents/phd_work/data_tools/universal_treebanks_v2.0/std/es/es-universal-test.conll";
        String devPath = "/Users/msr/Documents/phd_work/data_tools/universal_treebanks_v2.0/std/es/es-universal-test.conll";
        String modelPath = "/tmp/model";
        boolean train = true;
        String inputPath = "";
        String outputPath = "";
        String clusterPath = "";

        if(args.length>3){
            if (args[0].equals("train")){
                trainPath = args[1];
                devPath = args[2];
                modelPath = args[3];
                if(args.length>4)
                    clusterPath = args[4];
                train = true;
            } else if(args[0].equals("parse")){
                inputPath = args[1];
                outputPath = args[2];
                modelPath = args[3]; 
                train = false;
            }
        } else{
            System.out.println(" train/parse train_file/input_file dev_file(can_be_empty with -)/output_file model_file");
            System.exit(0);
        }

        if(train) {
            ArrayList<Sentence> devData = new ArrayList<Sentence>();

            if (devPath.length() > 1) {
                devData = TreebankReader.readConllSentences(devPath);
            }
            ArrayList<String> possibleLabels = new ArrayList<String>();
            possibleLabels.add("");

            ArrayList<Sentence> trainData = TreebankReader.readConllSentences(trainPath);
            for (Sentence sentence : trainData) {
                for (int i = 1; i < sentence.length(); i++) {
                    if (sentence.hasHead(i)) {
                        String label = sentence.label(i);
                        if (!label.equals("") && !possibleLabels.contains(label))
                            possibleLabels.add(label);
                    }
                }
            }

            AveragedPerceptron onlineClassifier = new AveragedPerceptron(possibleLabels);

            if(!clusterPath.equals(""))
                onlineClassifier.readBrownClusters(clusterPath);

            System.out.println("num of brown cluster words from file: "+ clusterPath+" ->"+ onlineClassifier.getBrownClusters().size());

            PartialTreeTrainer.standardTrain(trainData, devData, possibleLabels, onlineClassifier, modelPath, 30);
        }  else {
            AveragedPerceptron averagedPerceptron =  AveragedPerceptron.loadModel(modelPath);
            ArrayList<Sentence> inputData = TreebankReader.readConllSentences(inputPath);
            GraphBasedParser parser = new GraphBasedParser(averagedPerceptron, averagedPerceptron.getPossibleLabels());
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            HashMap<String,String[]> brownClusters = averagedPerceptron.getBrownClusters();
            long start = System.currentTimeMillis();
            int count =0;
            for(Sentence data: inputData){
                count++;
                 Sentence parsed = parser.eisner1stOrder(data,true,brownClusters);
                writer.write(parsed.toString());
                if(count%100==0)
                    System.out.print(count+"...");
            }
            System.out.print(count+"\n");
            long end = System.currentTimeMillis();
            double timeSec = (1.0 * (end - start)) / count;
            System.out.println("");
            System.out.println("time for each sentence: " + timeSec);
            writer.close();
            
        }
    }
}
