package Accessories;

import Structures.Sentence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 9/16/14
 * Time: 3:27 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class TreebankReader {
    public static ArrayList<Sentence> readMSTSentences(String path, boolean keepEmptyTrees) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = null;
        Pattern numPat = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");

        int num_dep = 0;
        ArrayList<Sentence> sentences = new ArrayList<Sentence>();

        int sen_num = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() < 1)
                continue;

            String[] words = line.split("\t");
           String posline = reader.readLine();
            String[] posTags = posline.split("\t");

           String labelLine  = reader.readLine();
            String[] labels = labelLine.split("\t");

            String headLine = reader.readLine();
            String[] heads = headLine.split("\t");


            int length = words.length + 1;
            String[] sWords = new String[length];
            double[] confidence = new double[length];
            String[] sTags = new String[length];
            int[] sHead = new int[length];
            String[] sLabels = new String[length];
            sWords[0] = "ROOT";
            sTags[0] = "ROOT";
            sHead[0] = -1;
            sLabels[0] = "";
            confidence[0]=0.0;

            boolean hasDep=false;
            for (int i = 1; i < length; i++) {
                try {
                    sWords[i] = words[i - 1];

                    if (sWords[i].equals("-LRB-"))
                        sWords[i] = "(";
                    if (sWords[i].equals("-RRB-"))
                        sWords[i] = ")";
                    //  Matcher matcher = numPat.matcher( sWords[i]);
                    // if (matcher.matches())
                    //  sWords[i] = "<num>";
                    sTags[i] = posTags[i - 1];

                    confidence[i] = 1.0;
                    int head = Integer.parseInt(heads[i - 1]);
                    if (head >= 0)
                        hasDep = true;

                    sHead[i] = head;
                    sLabels[i] = labels[i - 1];
                    if (sLabels[i].equals("_"))
                        sLabels[i] = "";
                    if (head >= 0)
                        num_dep++;
                }catch (Exception ex){
                    System.err.println(line);
                    System.err.println(posline);
                    System.err.println(labelLine);
                    System.err.println(headLine);
                    ex.printStackTrace()      ;
                    System.exit(1);
                }
            }

            if(hasDep || keepEmptyTrees) {
                Sentence sentence = new Sentence(sWords, sTags, sHead, sLabels,"");
                sentences.add(sentence);
            }
            sen_num++;
            //if(sen_num>3000)
            //    break;
            if (sen_num % 10000 == 0) {
                System.err.print(sen_num + "...");
            }
        }
        System.err.print("\nretrieved " +sentences.size() +" sentences with "+ num_dep + " dependencies\n");

        return sentences;
    }

    public static ArrayList<Sentence> readRawSentences(String path) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = null;

        ArrayList<Sentence> sentences = new ArrayList<Sentence>();

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() < 1)
                continue;

            String[] words = line.split("\t");
            line = reader.readLine();
            String[] posTags = line.split("\t");

            int length = words.length + 1;
            String[] sWords = new String[length];
            String[] sTags = new String[length];
            sWords[0] = "ROOT";
            sTags[0] = "ROOT";

            for (int i = 1; i < length; i++) {
                sWords[i] = words[i - 1];
                sTags[i] = posTags[i - 1];
            }

            Sentence sentence = new Sentence(sWords, sTags,"");
            sentences.add(sentence);
        }
        return sentences;
    }

    public static  ArrayList<Sentence> readConllSentences(String path) throws  Exception{
        ArrayList<Sentence> sentences = new ArrayList<Sentence>();

        ArrayList<String> words = new ArrayList<String>();
        ArrayList<String> tags = new ArrayList<String>();
        ArrayList<Integer> heads = new ArrayList<Integer>();
        ArrayList<String> labels = new ArrayList<String>();
        String languageId = "";
        
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while((line = reader.readLine())!=null){
            if (line.trim().length()>0){
                String[] split = line.trim().split("\t");
                words.add(split[1]);
                tags.add(split[3]);
                heads.add(Integer.parseInt(split[6]));
                labels.add(split[7]);
                languageId =  split[5];
            }   else{
                if (words.size()>0) {
                    String[] sWords = new String[words.size() + 1];
                    String[] sTags = new String[words.size() + 1];
                    int[] sHeads = new int[words.size() + 1];
                    String[] sLabels = new String[words.size() + 1];

                    sWords[0] = "ROOT";
                    sTags[0] = "ROOT";
                    sHeads[0] = -1;
                    sLabels[0] = "";

                    for (int i = 0; i < words.size(); i++) {
                        sWords[i + 1] = words.get(i);
                        sTags[i + 1] = tags.get(i);
                        sHeads[i + 1] = heads.get(i);
                        sLabels[i + 1] = labels.get(i);
                    }
                    sentences.add(new Sentence(sWords, sTags, sHeads, sLabels, languageId));
                    words = new ArrayList<String>();
                    tags = new ArrayList<String>();
                    heads = new ArrayList<Integer>();
                    labels = new ArrayList<String>();
                    languageId = "";
                }
            }
        }
          return sentences;
    }
}
