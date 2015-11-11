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
    public static ArrayList<String> extract2ndOrderFeatures(Sentence sentence, int headIndex, int childIndex, int secondChildIndex) {
        ArrayList<String> features = new ArrayList<String>();
       // if(true)
       //    return features;

        String cw1 = "_";
        String cp1 = "_";
        if (childIndex > 0) {
            cw1 = sentence.word(childIndex);
            cp1 = sentence.pos(childIndex);
        }

        String cw2 = sentence.word(secondChildIndex);
        String cp2 = sentence.pos(secondChildIndex);

        String hp = sentence.pos(headIndex);
        String direction = "l";
        int distance = 0;
        if (childIndex > 0)
            distance=Math.abs(secondChildIndex - childIndex);
        if(distance>=10)
            distance=10;
        else if (distance>=5)
            distance=5;
        if (secondChildIndex > headIndex)
            direction = "r";

        String tempFeat = "";

        // second order features
        tempFeat = "2p_ijk:" + hp + "|" + cp1 + "|" + cp2;
        features.add(tempFeat);
     //   tempFeat = "p_ijk_dr:" + hp + "|" + cp1 + "|" + cp2 + "|" + direction;
     //   features.add(tempFeat);
        tempFeat = "2p_ijk_d:" + hp + "|" + cp1 + "|" + cp2 + "|" + distance+"|"+direction;
        features.add(tempFeat);

        tempFeat = "2pp_jk:" + cp1 + "|" + cp2;
        features.add(tempFeat);
        tempFeat = "2pp_jk_d:" + cp1 + "|" + cp2 + "|" + distance+"|"+direction;
        features.add(tempFeat);
      //  tempFeat = "pp_jk_dr:" + cp1 + "|" + cp2 + "|" + direction;
      //  features.add(tempFeat);

        tempFeat = "2pw_jk:" + cp1 + "|" + cw2;
        features.add(tempFeat);
        tempFeat = "2pw_jk_d:" + cp1 + "|" + cw2 + "|" + distance+"|"+direction;
        features.add(tempFeat);
       // tempFeat = "pw_jk_dr:" + cp1 + "|" + cw2 + "|" + direction;
      //  features.add(tempFeat);

        tempFeat = "2wp_jk:" + cw1 + "|" + cp2;
        features.add(tempFeat);
        tempFeat = "2wp_jk_d:" + cw1 + "|" + cp2 + "|" + distance+"|"+direction;
        features.add(tempFeat);
       // tempFeat = "wp_jk_dr:" + cw1 + "|" + cp2 + "|" + direction;
       // features.add(tempFeat);

        tempFeat = "2ww_jk:" + cw1 + "|" + cw2;
        features.add(tempFeat);
        tempFeat = "2ww_jk_d:" + cw1 + "|" + cw2 + "|" + distance+"|"+direction;
        features.add(tempFeat);
    //    tempFeat = "ww_jk_dr:" + cw1 + "|" + cw2 + "|" + direction;
     //   features.add(tempFeat);

        return features;
    }

    public static  ArrayList<String> extract1stOrderFeatures(Sentence sentence, int headIndex, int childIndex ){
        ArrayList<String> features = new ArrayList<String>(50);
        String cw = sentence.word(childIndex);
        String cp = sentence.pos(childIndex);
        String hw = sentence.word(headIndex);
        String hp = sentence.pos(headIndex);


        String direction = "l";
        int distance = Math.abs(headIndex - childIndex);
        if(distance>=10)
            distance=10;
        else if (distance>=5)
            distance=5;
        if (childIndex > headIndex)
            direction = "r";

        /**
         * From Table 1(a) in the paper
         */
        String tempFeat = "";

        tempFeat = "hp:" + hp;
        features.add(tempFeat);

        tempFeat = "hp_d:" + hp + "|" + distance +"|"+direction;
        features.add(tempFeat);


        tempFeat = "cp:" + cp;
        features.add(tempFeat);

        tempFeat = "cp_d:" + cp + "|" + distance +"|"+direction;
        features.add(tempFeat);


        tempFeat = "hp_cp:" + hp + "|" + cp;
        features.add(tempFeat);

        tempFeat = "hp_cp_d:" + hp + "|" + cp + "|" + distance +"|"+direction;
        features.add(tempFeat);

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

        tempFeat ="hncp:"+ hp + "|" + hNextP + "|" + cPrevP + "|" + cp;
        features.add(tempFeat);
        tempFeat ="hpcp:"+ hPrevP + "|" + hp + "|" + cPrevP + "|" + cp;
        features.add(tempFeat);
        tempFeat = "hncn:"+hp + "|" + hNextP + "|" + cp + "|" + cNextP;
        features.add(tempFeat);
        tempFeat ="hpcn:"+ hPrevP + "|" + hp + "|" + cp + "|" + cNextP;
        features.add(tempFeat);

        return features;
        
    }
    
    public static ArrayList<String> extractExtraLabelFeatures(ArrayList<String> features, String label) {
        ArrayList<String> labelFeatures = new ArrayList<String>(features.size());

        int fl = features.size();
        for (int f = 0; f < fl; f++) {
            labelFeatures.add("L:" + label + "--" + features.get(f));
        }
        return labelFeatures;
    }
}
