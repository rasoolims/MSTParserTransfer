package Decoder;

import Structures.Sentence;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 9/16/14
 * Time: 4:34 PM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class FeatureExtractor {
    public static  ArrayList<String> extract1stOrderFeatures(Sentence sentence, int headIndex, int childIndex , HashMap<String,String[]> brownClusters) {
        ArrayList<String> features = new ArrayList<String>(50);
        String cw = sentence.word(childIndex);
        String cp = sentence.pos(childIndex);
        String hw = sentence.word(headIndex);
        String hp = sentence.pos(headIndex);

        String[] cbc = new String[3];
        String[] hbc = new String[3];
        boolean hasChildBrownCluster = false;
        boolean hasHeadBrownCluster = false;

        if (brownClusters.size() > 0) {
            if (brownClusters.containsKey(cw)) {
                hasChildBrownCluster = true;
                cbc = brownClusters.get(cw);
            }
            if (brownClusters.containsKey(hw)) {
                hasHeadBrownCluster = true;
                hbc = brownClusters.get(hw);
            }
        }


        String direction = "l";
        int distance = Math.abs(headIndex - childIndex);
        if (distance >= 10)
            distance = 10;
        else if (distance >= 5)
            distance = 5;
        if (childIndex > headIndex)
            direction = "r";

        /**
         * From Table 1(a) in the paper
         */
        String tempFeat = "";

        tempFeat = "hp:" + hp;
        features.add(tempFeat);

        if (hasHeadBrownCluster) {
            tempFeat = "hbc0:" + hbc[0];
            features.add(tempFeat);

            tempFeat = "hbc1:" + hbc[1];
            features.add(tempFeat);

            tempFeat = "hbc2:" + hbc[2];
            features.add(tempFeat);
        }

        tempFeat = "hp_d:" + hp + "|" + distance + "|" + direction;
        features.add(tempFeat);

        if (hasHeadBrownCluster) {
            tempFeat = "hbc_d0:" + hbc[0] + "|" + distance + "|" + direction;
            features.add(tempFeat);

            tempFeat = "hbc_d1:" + hbc[1] + "|" + distance + "|" + direction;
            features.add(tempFeat);

            tempFeat = "hbc_d2:" + hbc[2] + "|" + distance + "|" + direction;
            features.add(tempFeat);
        }

        tempFeat = "cp:" + cp;
        features.add(tempFeat);

        if (hasChildBrownCluster) {
            tempFeat = "cbc0:" + cbc[0];
            features.add(tempFeat);

            tempFeat = "cbc1:" + cbc[1];
            features.add(tempFeat);

            tempFeat = "cbc2:" + cbc[2];
            features.add(tempFeat);
        }

        tempFeat = "cp_d:" + cp + "|" + distance + "|" + direction;
        features.add(tempFeat);
        if (hasChildBrownCluster) {
            tempFeat = "cbc_d0:" + cbc[0] + "|" + distance + "|" + direction;
            features.add(tempFeat);

            tempFeat = "cbc_d1:" + cbc[1] + "|" + distance + "|" + direction;
            features.add(tempFeat);

            tempFeat = "cbc_d2:" + cbc[2] + "|" + distance + "|" + direction;
            features.add(tempFeat);
        }

        tempFeat = "hp_cp:" + hp + "|" + cp;
        features.add(tempFeat);

        if (hasChildBrownCluster && hasHeadBrownCluster) {
            tempFeat = "hbc_cbc00:" + hbc[0] + "|" + cbc[0];
            features.add(tempFeat);

            tempFeat = "hbc_cbc11:" + hbc[1] + "|" + cbc[1];
            features.add(tempFeat);

            tempFeat = "hbc_cbc22:" + hbc[2] + "|" + cbc[2];
            features.add(tempFeat);

            /*
            tempFeat = "hbc_cbc01:" + hbc[0] + "|" + cbc[1];
            features.add(tempFeat);

            tempFeat = "hbc_cbc12:" + hbc[1] + "|" + cbc[2];
            features.add(tempFeat);

            tempFeat = "hbc_cbc02:" + hbc[0] + "|" + cbc[2];
            features.add(tempFeat);

            tempFeat = "hbc_cbc10:" + hbc[1] + "|" + cbc[0];
            features.add(tempFeat);

            tempFeat = "hbc_cbc20:" + hbc[2] + "|" + cbc[0];
            features.add(tempFeat);

            tempFeat = "hbc_cbc21:" + hbc[2] + "|" + cbc[1];
            features.add(tempFeat);
            */
        }

        tempFeat = "hp_cp_d:" + hp + "|" + cp + "|" + distance + "|" + direction;
        features.add(tempFeat);

        if (hasChildBrownCluster && hasHeadBrownCluster) {


            tempFeat = "hbc_cbc_d00:" + hbc[0] + "|" + cbc[0] + "|" + distance + "|" + direction;
            features.add(tempFeat);

            tempFeat = "hbc_cbc_d11:" + hbc[1] + "|" + cbc[1] + "|" + distance + "|" + direction;
            features.add(tempFeat);

            tempFeat = "hbc_cbc_d22:" + hbc[2] + "|" + cbc[2] + "|" + distance + "|" + direction;
            features.add(tempFeat);
            
            /*
            tempFeat = "hbc_cbc_d01:" + hbc[0] + "|" + cbc[1]+ "|" + distance +"|"+direction;
            features.add(tempFeat);
            
            tempFeat = "hbc_cbc_d02:" + hbc[0] + "|" + cbc[2]+ "|" + distance +"|"+direction;
            features.add(tempFeat);

            tempFeat = "hbc_cbc_d10:" + hbc[1] + "|" + cbc[0]+ "|" + distance +"|"+direction;
            features.add(tempFeat);


            tempFeat = "hbc_cbc_d12:" + hbc[1] + "|" + cbc[2]+ "|" + distance +"|"+direction;
            features.add(tempFeat);

            tempFeat = "hbc_cbc_d20:" + hbc[2] + "|" + cbc[0]+ "|" + distance +"|"+direction;
            features.add(tempFeat);

            tempFeat = "hbc_cbc_d21:" + hbc[2] + "|" + cbc[1]+ "|" + distance +"|"+direction;
            features.add(tempFeat);
              */

        }

        /**
         * From Table 1(c) in the paper
         */
        for (int i = Math.min(headIndex, childIndex) + 1; i < Math.max(headIndex, childIndex); i++) {
            String bp = sentence.pos(i);
            String mixFeat = "ib:" + hp + "|" + bp + "|" + cp;
            features.add(mixFeat);
        }

        String hNextP = "";
        if (headIndex < sentence.length() - 1)
            hNextP = sentence.pos(headIndex + 1);
        String hPrevP = "";
        if (headIndex > 0)
            hPrevP = sentence.pos(headIndex - 1);
        String cNextP = "";
        if (childIndex < sentence.length() - 1)
            cNextP = sentence.pos(childIndex + 1);
        String cPrevP = "";
        if (childIndex > 1)
            cPrevP = sentence.pos(childIndex - 1);

        tempFeat = "hncp:" + hp + "|" + hNextP + "|" + cPrevP + "|" + cp;
        features.add(tempFeat);
        tempFeat = "hpcp:" + hPrevP + "|" + hp + "|" + cPrevP + "|" + cp;
        features.add(tempFeat);
        tempFeat = "hncn:" + hp + "|" + hNextP + "|" + cp + "|" + cNextP;
        features.add(tempFeat);
        tempFeat = "hpcn:" + hPrevP + "|" + hp + "|" + cp + "|" + cNextP;
        features.add(tempFeat);

        return features;

    }

}
