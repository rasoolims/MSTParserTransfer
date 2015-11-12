package Decoder;

import Classifier.AveragedPerceptron;
import Structures.Sentence;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 9/17/14
 * Time: 11:06 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */
public class GraphBasedParser {
    AveragedPerceptron classifier;
    ArrayList<String> labels;

    public GraphBasedParser(AveragedPerceptron perceptron,
                            ArrayList<String> labels) {
        this.classifier = perceptron;
        this.labels = labels;
    }

    public Sentence eisner1stOrder(Sentence sentence, boolean decode, HashMap<String, String[]> brownClusters) {
        int l = sentence.length();
        double[][] scores = new double[l][l];
        String[][] bestLabel = new String[l][l];

        int[] finalDeps = new int[l];
        finalDeps[0] = -1;

        // getting first-order attachment scores
        
            for (int i = 0; i < l; i++) {
                for (int j = i + 1; j < l; j++) {
                    scores[i][j] = Double.NEGATIVE_INFINITY;
                    scores[j][i] = Double.NEGATIVE_INFINITY;
                    ArrayList<String> ijFeatures = FeatureExtractor.extract1stOrderFeatures(sentence, i, j, brownClusters);
                    ArrayList<String> jiFeatures = FeatureExtractor.extract1stOrderFeatures(sentence, j, i, brownClusters);

                    double[] sijScores = classifier.score(ijFeatures, decode);
                    double[] sjiScores = classifier.score(jiFeatures, decode);
                    double[] sijSharedScores = classifier.sharedScores(ijFeatures, decode);
                    double[] sjiSharedScores = classifier.sharedScores(jiFeatures,decode);

                    for (int lab = 1; lab < labels.size(); lab++) {
                        
                        int sijIndex =  FeatureExtractor.retriveLangIdex( labels.get(lab),sentence.pos(j),sentence.pos(i),sentence.getLanguageID());
                        int sjiIndex =  FeatureExtractor.retriveLangIdex( labels.get(lab),sentence.pos(i),sentence.pos(j),sentence.getLanguageID());

                        double s1 = sijScores[lab] + sijScores[0];
                        if(sijIndex>=0){
                             s1+= sijSharedScores[sijIndex];
                        }
                        
                        if (s1 > scores[i][j]) {
                            scores[i][j] = s1;
                            bestLabel[i][j] = labels.get(lab);
                        }
                        
                        double s2 = sjiScores[lab] + sjiScores[0];
                        if(sjiIndex>=0){
                            s2+= sjiSharedScores[sjiIndex];
                        }
                        if (s2 > scores[j][i]) {
                            scores[j][i] = s2;
                            bestLabel[j][i] = labels.get(lab);
                        }
                    }
                }
            }

        /**
         direction: 0=right, 1=left
         completeness: 0=incomplete, 1=complete
         **/

        int right = 0;
        int left = 1;

        int complete = 1;
        int incomplete = 0;

        double[][][][] c = new double[l][l][2][2];
        // back pointer for dependencies
        int[][][][] bd = new int[l][l][2][2];
        // back pointer for dependency labels
        for (int s = 0; s < l; s++) {
            c[s][s][right][complete] = 0.0;
            c[s][s][left][complete] = 0.0;
        }

        for (int k = 1; k < l; k++) {
            for (int s = 0; s < l; s++) {
                int t = s + k;
                if (t >= l) break;


                // create incomplete items
                c[s][t][left][incomplete] = Double.NEGATIVE_INFINITY;
                c[s][t][right][incomplete] = Double.NEGATIVE_INFINITY;
                for (int r = s; r < t; r++) {
                    double bestRightScore = scores[s][t];
                    double bestLeftScore = scores[t][s];


                    double newLeftValue = c[s][r][right][complete] + c[r + 1][t][left][complete] + bestLeftScore;
                    if (newLeftValue > c[s][t][left][incomplete]) {
                        c[s][t][left][incomplete] = newLeftValue;
                        bd[s][t][left][incomplete] = r;
                    }

                    double newRightValue = c[s][r][right][complete] + c[r + 1][t][left][complete] + bestRightScore;
                    if (newRightValue > c[s][t][right][incomplete]) {
                        c[s][t][right][incomplete] = newRightValue;
                        bd[s][t][right][incomplete] = r;
                    }
                }

                // create complete spans
                c[s][t][left][complete] = Double.NEGATIVE_INFINITY;
                c[s][t][right][complete] = Double.NEGATIVE_INFINITY;
                for (int r = s; r <= t; r++) {
                    if (r < t) {
                        double newLeftScore = c[s][r][left][complete] + c[r][t][left][incomplete];
                        if (newLeftScore > c[s][t][left][complete]) {
                            c[s][t][left][complete] = newLeftScore;
                            bd[s][t][left][complete] = r;
                        }
                    }

                    if (r > s) {
                        double newRightScore = c[s][r][right][incomplete] + c[r][t][right][complete];
                        if (newRightScore > c[s][t][right][complete]) {
                            c[s][t][right][complete] = newRightScore;
                            bd[s][t][right][complete] = r;
                        }
                    }
                }
            }
        }

        retrieveDeps(bd, 0, l - 1, 0, 1, finalDeps);
        
        String[] labels = new String[finalDeps.length];
        for(int i=0;i<finalDeps.length;i++){
            if(finalDeps[i]==-1)
                labels[i]="";
            else{
                labels[i]= bestLabel[finalDeps[i]][i];
            }
        }

        return new Sentence(sentence.getWords(), sentence.getTags(), finalDeps, labels, sentence.getLanguageID());
    }


    public void retrieveDeps(int[][][][] bd, int s, int t, int direction,
                             int completeness, int[] finalDeps) {
        if (s == t)
            return;

        int r = bd[s][t][direction][completeness];
        if (completeness == 1) {
            if (direction == 0) {
                retrieveDeps(bd, s, r, 0, 0, finalDeps);
                retrieveDeps(bd, r, t, 0, 1, finalDeps);
            } else {
                retrieveDeps(bd, s, r, 1, 1, finalDeps);
                retrieveDeps(bd, r, t, 1, 0, finalDeps);
            }
        } else {
            if (direction == 0) {
                finalDeps[t] = s;
                retrieveDeps(bd, s, r, 0, 1, finalDeps);
                retrieveDeps(bd, r + 1, t, 1, 1, finalDeps);

            } else {
                finalDeps[s] = t;
                retrieveDeps(bd, s, r, 0, 1, finalDeps);
                retrieveDeps(bd, r + 1, t, 1, 1, finalDeps);
            }
        }
    }
}
