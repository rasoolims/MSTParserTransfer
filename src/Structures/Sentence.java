package Structures;

/**
 * Created by Mohammad Sadegh Rasooli.
 * ML-NLP Lab, Department of Computer Science, Columbia University
 * Date Created: 9/16/14
 * Time: 11:36 AM
 * To report any bugs or problems contact rasooli@cs.columbia.edu
 */

public class Sentence {
    String[] words;
    String[] tags;
    int[] heads;
    String[] labels;
    String languageID;

    public Sentence(String[] words, String[] tags, String languageID){
        this.words=words;
        this.tags = tags;
        heads=new int[words.length];
        labels=new String[words.length];

        heads[0]=-1;
        labels[0]="";
        for(int i=1;i<words.length;i++){
           heads[i]=-1;
            labels[i]="";
        }
        this.languageID = languageID;
    }

    public Sentence(String[] words, String[] tags,int[] heads,String languageID){
        this.words=words;
        this.tags = tags;
        this.heads=heads;
        labels=new String[words.length];
        labels[0]="";
        for(int i=1;i<words.length;i++){
            labels[i]="";
        }
        this.languageID = languageID;
    }

    public Sentence(String[] words, String[] tags,int[] heads,String[] labels,String languageID){
        this.words=words;
        this.tags = tags;
        this.heads=heads;
        this.labels=labels;
        this.languageID = languageID;
    }


    public int length(){
        return words.length;
    }

    public String word(int index){
        return words[index];
    }

    public String pos(int index){
        return tags[index];
    }

    public int head(int index){
        return heads[index];
    }

    public String label(int index){
        return labels[index];
    }

    public boolean hasHead(int index){
        return heads[index]>=0;
    }

    public String[] getWords() {
        return words;
    }

    public String[] getTags() {
        return tags;
    }

    public void setHeads(int[] heads) {
        this.heads = heads;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public int[] getHeads() {
        return heads;
    }

    public String[] getLabels() {
        return labels;
    }

    public String getLanguageID() {
        return languageID;
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();

        for(int i=1;i<words.length;i++){
            builder.append(i+"\t"+words[i]+"\t_\t"+tags[i]+"\t_\t"+languageID+"\t"+heads[i]+"\t"+labels[i]+"\t_\t_\n");
        }
        return builder.toString().trim()+"\n\n";
    }
}
