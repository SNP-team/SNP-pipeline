/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package analysis;

/**
 *
 * This is an attribute selection script that leverages Bayesian networks to select attributes
 * It uses the att_ev_SingleBayesROCAttributeEval class to evaluate each attribute
 *
 * @author Aaron
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.bayes.net.search.local.K2;
import weka.classifiers.functions.pace.ChisqMixture;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author Aaron
 * This is an attribute selection class that leverages Bayesian networks to select attributes
 *
 */
public class BayesAttSel {


    String learnFileName;
    String holdFileName;

    String learnPostSelectionFileName;
    String holdPostSelectionFileName;

    Instances learn;
    Instances hold;

    Instances learnFinal =null;
    Instances holdFinal =null;

    final int removeIterations = 3;
    final int CVfoldNum = 3;


    /* Getters and Setters */
    public Instances getHold() {
        return hold;
    }

    public void setHold(Instances hold) {
        this.hold = hold;
    }

    public String getHoldFileName() {
        return holdFileName;
    }

    public void setHoldFileName(String holdFileName) {
        this.holdFileName = holdFileName;
    }

    public Instances getHoldFinal() {
        return holdFinal;
    }

    public void setHoldFinal(Instances holdFinal) {
        this.holdFinal = holdFinal;
    }

    public String getHoldPostSelectionFileName() {
        return holdPostSelectionFileName;
    }

    public void setHoldPostSelectionFileName(String holdPostSelectionFileName) {
        this.holdPostSelectionFileName = holdPostSelectionFileName;
    }

    public Instances getLearn() {
        return learn;
    }

    public void setLearn(Instances learn) {
        this.learn = learn;
    }

    public String getLearnFileName() {
        return learnFileName;
    }

    public void setLearnFileName(String learnFileName) {
        this.learnFileName = learnFileName;
    }

    public Instances getLearnFinal() {
        return learnFinal;
    }

    public void setLearnFinal(Instances learnFinal) {
        this.learnFinal = learnFinal;
    }

    public String getLearnPostSelectionFileName() {
        return learnPostSelectionFileName;
    }

    public void setLearnPostSelectionFileName(String learnPostSelectionFileName) {
        this.learnPostSelectionFileName = learnPostSelectionFileName;
    }



    public BayesAttSel(){
        this.learn = null;
        this.hold = null;
        this.learnFileName = null;
        this.holdFileName = null;
        this.learnPostSelectionFileName = null;
        this.holdPostSelectionFileName = null;

    }

    public BayesAttSel(String learnFileName, String holdFileName, String learnPostSelectionFileName, String holdPostSelectionFileName) throws Exception {
        this.learnFileName = learnFileName;
        this.holdFileName = holdFileName;
        this.learn = new Instances(new BufferedReader(new FileReader(learnFileName)));
        this.hold = new Instances(new BufferedReader(new FileReader(holdFileName)));
        this.learnPostSelectionFileName = learnPostSelectionFileName;
        this.holdPostSelectionFileName = holdPostSelectionFileName;
    }
    public BayesAttSel(String learnFileName, String holdFileName) throws Exception {
        this.learnFileName = learnFileName;
        this.holdFileName = holdFileName;
        this.learn = new Instances(new BufferedReader(new FileReader(learnFileName)));
        System.out.println("new learn Instance generated");  // for debug
        this.hold = new Instances(new BufferedReader(new FileReader(holdFileName)));
        System.out.println("new validation Instance generated");
        this.learnPostSelectionFileName = learnFileName.substring(0,learnFileName.lastIndexOf(".")).concat("_PostBayesAttSel.arff");
        System.out.println("learn postselection filename:\n" + this.learnPostSelectionFileName);
        this.holdPostSelectionFileName = holdFileName.substring(0,holdFileName.lastIndexOf(".")).concat("_PostBayesAttSel.arff");
        System.out.println("validation postselection filename\n"+this.holdPostSelectionFileName);

    }
    public BayesAttSel(String learnFileName) throws Exception {
        this.learnFileName = learnFileName;
        this.holdFileName = null;
        this.learn = new Instances(new BufferedReader(new FileReader(learnFileName)));
        this.hold = null;
        this.learnPostSelectionFileName = learnFileName.substring(0,learnFileName.lastIndexOf(".")).concat("_PostBayesAttSel.arff");
        this.holdPostSelectionFileName = null;
    }

    public void saveFile() throws Exception{
        System.out.print("SAVING FILE");

        ArffSaver saverLearn = new ArffSaver();
            saverLearn.setInstances(learnFinal);
            saverLearn.setFile(new File(learnPostSelectionFileName));
            saverLearn.writeBatch();

            if (hold!=null){
                ArffSaver saverHold = new ArffSaver();
                saverHold.setInstances(holdFinal);
                saverHold.setFile(new File(holdPostSelectionFileName));
                saverHold.writeBatch();
            }
    }

    public void saveFile(Instances le, String leFileName, Instances ho, String hoFileName) throws Exception{
            System.out.println("starting double save");
            System.out.println("le.numAttributes: " + le.numAttributes());
            System.out.println("le.filename: " + leFileName);
            System.out.println("ho.numAttributes: " + ho.numAttributes());
            System.out.println("ho.filename: " + hoFileName);

            ArffSaver saverLearn = new ArffSaver();
            saverLearn.setInstances(le);
            saverLearn.setFile(new File(leFileName));
            saverLearn.writeBatch();
            System.out.println("learn batch written!");

            if (ho != null){
                BayesAttSel.saveFileByHand(ho, hoFileName);
            }

//            if (ho != null){
//                ArffSaver saverHold = new ArffSaver();
//                saverHold.setInstances(ho);
//                saverHold.setFile(new File(hoFileName));
//                saverHold.writeBatch();
//            }
    }


    public void saveFile(Instances le, String leFileName) throws Exception{
            System.out.println("starting save");
            ArffSaver saverLearn = new ArffSaver();
            saverLearn.setInstances(le);
            saverLearn.setFile(new File(leFileName));
            saverLearn.writeBatch();
    }

    private static String getExtension(String s) {

        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }


    /* this method runs attribute selection
    * Main entrance
    * */
    public void execute(int chi2toSel, int numToSel) throws Exception{
        // remove subjects-------------------------------------------------
        learn = this.rmSubjects(learn);
        if (hold!=null){hold = this.rmSubjects(hold);}

        // do chi-squared attribute selection-------------------------------------------------
        learn = this.applyChi2(learn, chi2toSel);

        System.out.println("saving intemediate: " + learnFileName.substring(0, learnFileName.lastIndexOf(".")).concat("_SeletectedByChi2.arff"));
        saveFile(learn, learnFileName.substring(0, learnFileName.lastIndexOf(".")).concat("_SeletectedByChi2.arff"));

        if (hold!=null){
            hold = this.applyChi2(hold, chi2toSel);
            System.out.println("saving intemediate " + holdFileName.substring(0, holdFileName.lastIndexOf(".")).concat("_SeletectedByChi2.arff"));
            saveFile(hold, holdFileName.substring(0, holdFileName.lastIndexOf(".")).concat("_SeletectedByChi2.arff"));
        }

        System.out.println("Chi2 eliminated uninformative features");
        System.out.println("features remaining(including Phenotype): " + learn.numAttributes());


        // do second attributes selection, evaluated by ROC ----------------------------------------

        System.out.println("Applying Sort");

        Instances learnPostSel = this.applyROCSelection(learn, numToSel);
        Instances holdPostSel = null;
        if (hold != null) {
            holdPostSel = applyROCSelection(hold, numToSel);
            System.out.println("learn attirbutes: " + learnPostSel.numAttributes());
            System.out.println("hold attirbutes: " + holdPostSel.numAttributes());
            saveFile(learnPostSel, learnFileName.substring(0, learnFileName.lastIndexOf(".")).concat("_sortedByROC_2.arff"),
                    holdPostSel, holdFileName.substring(0, holdFileName.lastIndexOf(".")).concat("_sortedByROC_2.arff"));
            System.out.println("ROC sorted file saved");
        } else {
            saveFile(learnPostSel, learnFileName.substring(0, learnFileName.lastIndexOf(".")).concat("_sortedByROC_2.arff"));
            System.out.println("ROC sorted file saved");
        }


        // remove-naive bayes loop
//        System.out.println("Removing Subject Names");
//        Remove subjNumRem = new Remove();
//        subjNumRem.setInputFormat(learnPostSel);
//        learnPostSel = Filter.useFilter(learnPostSel, subjNumRem);
//        if (hold!=null){holdPostSel = Filter.useFilter(holdPostSel, subjNumRem);}
//
//        subjNumRem.setAttributeIndices("1");



        /* build Bayes network----------------------------------------------------- */
        Remove rm = new Remove();

        Classifier bn = new BayesNet();
//        Classifier bnCopy;
        Classifier bnCopyEv;

        Evaluation ev;
        K2 xK2 = new K2();
        xK2.setInitAsNaiveBayes(true);
        xK2.setMaxNrOfParents(1);
        ((BayesNet) bn).setSearchAlgorithm(xK2);

        int winningNaiveRemove = -1;
        double[] accuracyRes = new double[learnPostSel.numAttributes()];
        double[] ROCRes = new double[learnPostSel.numAttributes()];

        System.out.println("Starting optimal network algorithm");
        for (int removeNaive_counter = learnPostSel.numAttributes()-2;removeNaive_counter > 1 ; removeNaive_counter--){
//            System.out.println(removeNaive_counter);
            StringBuilder sb = new StringBuilder("");
            sb.append(removeNaive_counter).append("-").append(learnPostSel.numAttributes()-1);
            rm.setAttributeIndices(sb.toString());
            rm.setInputFormat(learnPostSel);
            Instances learnPostSelNaive = rm.useFilter(learnPostSel, rm);

            // TODO: make a chart here.

//            bnCopy = Classifier.makeCopy(bn);
//            bnCopyEv = Classifier.makeCopy(bn);
            ev = new Evaluation(learnPostSelNaive);
            ev.crossValidateModel(Classifier.makeCopy(bn), learnPostSelNaive,Math.min(CVfoldNum,learn.numInstances()),new Random(removeNaive_counter));
//            bnCopy.buildClassifier(learnPostSelNaive);   //this only gives the bayes score.. is that interesting?

            accuracyRes[removeNaive_counter] = ev.pctCorrect();
            ROCRes[removeNaive_counter] = ev.weightedAreaUnderROC();

            System.out.println("loops remaining: " + Integer.toString(removeNaive_counter));
        }

//        wrep.BGS_multivariate_ROC_ACC = new double[2][learn.numAttributes()-3];

//        for (int mv_counter = 0; mv_counter<accuracyRes.length-2; mv_counter++){
//            System.arraycopy(ROCRes, 2, wrep.BGS_multivariate_ROC_ACC[0], 0, learn.numAttributes()-3);
//            System.arraycopy(accuracyRes, 2, wrep.BGS_multivariate_ROC_ACC[1], 0, learn.numAttributes()-3);
//            wrep.BGS_multivariate_ROC_ACC[1] = accuracyRes;
//        }

        winningNaiveRemove = pickWinner(accuracyRes,ROCRes);
        System.out.println("The winner is: " + winningNaiveRemove);
        StringBuilder sb = new StringBuilder("");
        sb.append(winningNaiveRemove).append("-").append(learnPostSel.numAttributes()-1);

        rm.setAttributeIndices(sb.toString());
        rm.setInputFormat(learnPostSel);
        learnFinal = Filter.useFilter(learnPostSel, rm);


        /*
//        if (hold!=null){holdFinal = Filter.useFilter(holdPostSel, rm);}

//            ev = new Evaluation(learnPreRandRemove);
//            ev.crossValidateModel(Classifier.makeCopy(bn), learnPreRandRemove,Math.min(CVfoldNum,learn.numInstances()),new Random(winningNaiveRemove));


//        // random removal loop
//            int numCats = Math.min(10, learnPreRandRemove.numAttributes()-1);
//            int[] numToRem = new int[numCats];
//            numToRem[0]= 1;
//
//            for (int setup_counter = 1 ; setup_counter<numCats; setup_counter++){
//                numToRem[setup_counter] = Math.round(learnPreRandRemove.numAttributes()/numCats)*setup_counter;
//            }
//
//            double[][] accPostRandRemove = new double[numCats][removeIterations];
//            double[][] rocPostRandRemove = new double[numCats][removeIterations];
//
//
//
//                                Classifier yBN = new BayesNet();
//                    K2 yK2 = new K2();
//                    yK2.setMaxNrOfParents(1);
//                    yK2.setInitAsNaiveBayes(true);
//                    SimpleEstimator ySE = new SimpleEstimator();
//    //                ySE.setAlpha(5);
//                    ((BayesNet) yBN).setSearchAlgorithm(yK2);
//                    ((BayesNet) yBN).setEstimator(ySE);
//
//
//            for (int numToRem_counter = 0; numToRem_counter<numToRem.length;numToRem_counter++){
//                for (int iteration_counter = 0; iteration_counter < removeIterations; iteration_counter++){
////                System.out.println(numToRem_counter);
//                    Instances learnPostRandRemove = new Instances(learnPreRandRemove);
////                    Instances holdPostRandRemove = new Instances(holdPreRandRemove);
//
//                     Random rnd = new Random(1+numToRem_counter*10000+iteration_counter*10);
////                     StringBuilder remString = new StringBuilder("");
//
//
//                     for (int rmv_counter = 0 ; rmv_counter < numToRem[numToRem_counter]; rmv_counter++){
//
//                        String theInt = String.valueOf(1+rnd.nextInt(learnPreRandRemove.numAttributes()-1-rmv_counter));
//                        Remove rmv = new Remove();
////                        System.out.println(1+numToRem_counter*10000+iteration_counter*10);
////                        System.out.println(theInt);
////                        remString.append(theInt).append(";");
//                        rmv.setAttributeIndices(theInt);
//
//                        try{
//                            rmv.setInputFormat(learnPostRandRemove);
//                            learnPostRandRemove = rmv.useFilter(learnPostRandRemove, rmv);
////                            holdPostRandRemove = rmv.useFilter(holdPostRandRemove, rmv);
//                        } catch (Exception ex) {System.out.println(ex.toString());}
//                    } // rmv_counter
//
//                     // now the postRandRemove instances are configured
//
//
//
//                    Classifier yBNcopy = Classifier.makeCopy(yBN);
//
//                    Evaluation evPostRand = new Evaluation(learnPostRandRemove);
//                    evPostRand.crossValidateModel(yBNcopy, learnPostRandRemove,Math.min(CVfoldNum,learn.numInstances()),new Random(winningNaiveRemove));
//
//                    accPostRandRemove[numToRem_counter][iteration_counter] = evPostRand.pctCorrect();
//                    rocPostRandRemove[numToRem_counter][iteration_counter] = evPostRand.weightedAreaUnderROC();
////                    if ((numToRem_counter % 100)==0){System.out.print(numToRem_counter);System.out.println("RandomRemovalLoop: ".concat(String.valueOf(evPostRand.weightedAreaUnderROC())));}
//                }
//            }
//
//            //pick a winner
//
////            wrep.BGS_removes_ROC_HI_LO_AVG = new double[3][numCats];
//
////              for (int numToRem_counter = 0; numToRem_counter<numToRem.length;numToRem_counter++){
////                  Arrays.sort(rocPostRandRemove[numToRem_counter]);
//
////                  wrep.BGS_removes_ROC_HI_LO_AVG[0][numToRem_counter] = rocPostRandRemove[numToRem_counter][rocPostRandRemove[numToRem_counter].length-1];
////                  wrep.BGS_removes_ROC_HI_LO_AVG[1][numToRem_counter] = rocPostRandRemove[numToRem_counter][0];
////                  wrep.BGS_removes_ROC_HI_LO_AVG[2][numToRem_counter] = rocPostRandRemove[numToRem_counter][Math.round((float) Math.ceil((rocPostRandRemove[numToRem_counter].length-1)/2))];
////              }
//
//
//        int[] postRandWinnerArray = new int[numCats];
//        for (int numToRem_counter = 0; numToRem_counter<numToRem.length;numToRem_counter++){
//            postRandWinnerArray[numToRem_counter] = pickWinner(accPostRandRemove[numToRem_counter], rocPostRandRemove[numToRem_counter]);
//        }
//
//        // TODO: make a chart here.
//
//
//
//        double[] accWinners = new double[numCats];
//        double[] rocWinners = new double[numCats];
//        for (int numToRem_counter = 0; numToRem_counter<numToRem.length;numToRem_counter++){
//            accWinners[numToRem_counter] = accPostRandRemove[numToRem_counter][postRandWinnerArray[numToRem_counter]];
//            rocWinners[numToRem_counter] = rocPostRandRemove[numToRem_counter][postRandWinnerArray[numToRem_counter]];
//        }
//
//
//
//
//
//        int winningPostRandRemove = pickWinner(accWinners, rocWinners);
//
//            double[] accFinalRound = new double[removeIterations*10];
//            double[] rocFinalRound = new double[removeIterations*10];
//
//
//
//            double accFinal = -1;
//            double rocFinal = -1;
//
//
//                    Classifier zBN = new BayesNet();
//                    K2 zK2 = new K2();
//                    zK2.setMaxNrOfParents(1);
//                    zK2.setInitAsNaiveBayes(true);
//                    SimpleEstimator zSE = new SimpleEstimator();
//    //                ySE.setAlpha(5);
//                    ((BayesNet) zBN).setSearchAlgorithm(zK2);
//                    ((BayesNet) zBN).setEstimator(zSE);
//
//                for (int iteration_counter = 0; iteration_counter < removeIterations; iteration_counter++){
////                    System.out.println(iteration_counter);
//                    Instances learnFinalRound = new Instances(learnPreRandRemove);
//                    Instances holdFinalRound = new Instances(holdPreRandRemove);
//
//                     rndd.setSeed(iteration_counter);
//                     StringBuilder remString = new StringBuilder("");
//
//
//                     for (int rmv_counter = 0 ; rmv_counter < numToRem[winningPostRandRemove]; rmv_counter++){
//
//                        String theInt = String.valueOf(1+rndd.nextInt(learnPreRandRemove.numAttributes()-1-rmv_counter));
//                        Remove rmv = new Remove();
//                        remString.append(theInt).append(";");
//                        rmv.setAttributeIndices(theInt);
//
//                        try{
//                            rmv.setInputFormat(learnFinalRound);
//                            learnFinalRound = rmv.useFilter(learnFinalRound, rmv);
//                            holdFinalRound = rmv.useFilter(holdFinalRound, rmv);
//                        } catch (Exception ex) {System.out.println(ex.toString());}
//                    } // rmv_counter
//
//                     // now the postRandRemove instances are configured
//
//
//
//                    Classifier zBNcopy = Classifier.makeCopy(zBN);
//
//                    Evaluation evFinalRound = new Evaluation(learnFinalRound);
//                    evFinalRound.crossValidateModel(zBNcopy, learnFinalRound,Math.min(CVfoldNum,learn.numInstances()),new Random(winningNaiveRemove));
//
//                    accFinalRound[winningPostRandRemove] = evFinalRound.pctCorrect();
//                    rocFinalRound[winningPostRandRemove] = evFinalRound.weightedAreaUnderROC();
//
//                    if (evFinalRound.weightedAreaUnderROC() > rocFinal){
//                        learnFinal = learnFinalRound;
//                        holdFinal = holdFinalRound;
//                        rocFinal = evFinalRound.weightedAreaUnderROC();
//                        accFinal = evFinalRound.pctCorrect();
////                        wrep.BGS_postAttributeACC = accFinal;
////                        wrep.BGS_postAttributeROC = rocFinal;
//                    } else if (evFinalRound.weightedAreaUnderROC() == rocFinal){
//                        if (evFinalRound.pctCorrect() > accFinal){
//                            learnFinal = learnFinalRound;
//                            holdFinal = holdFinalRound;
//                            rocFinal = evFinalRound.weightedAreaUnderROC();
//                            accFinal = evFinalRound.pctCorrect();
////                            wrep.BGS_postAttributeACC = accFinal;
////                            wrep.BGS_postAttributeROC = rocFinal;
//                        }
//                    }
//                }
//
//                    if (midAttROC > rocFinal){
//                        learnFinal = learnPreRandRemove;
//                        holdFinal = holdPreRandRemove;
////                        wrep.BGS_postAttributeACC = wrep.BGS_midAttributeACC;
////                        wrep.BGS_postAttributeROC = wrep.BGS_midAttributeROC;
//                    }
**/
        System.out.println("saving winner");
        saveFile();
    }

    private int pickWinner(double[] ac, double[] ro) {
        int winner = -1;
        double highestROC = -1;
        double highestACC = -1;

        //loop thru all rocs


        for (int cou = 0 ; cou < ro.length; cou++)  // for each # of attributes
            if (ro[cou] > highestROC){     // if ROC is standalone highest, it wins
                winner = cou;
                highestACC = ac[cou];
                highestROC = ro[cou];
            } else if (ro[cou] == highestROC){  // if ROC is tied, compare ACC
                if (ac[cou] > highestACC){  // if ACC is standalone highest, it wins
                    winner = cou;
                    highestACC = ac[cou];
                    highestROC = ro[cou];
                } else if (ac [cou] == highestACC) // if ACC is also tied, pick smaller # of attributes
                    if (cou < winner) {
                        winner = cou;
                        highestACC = ac[cou];
                        highestROC = ro[cou];
                    }
            }

        return winner;
    }

    public static boolean attributesEqual(Instances ins1,Instances ins2){
        int num1 = ins1.numAttributes();
        int num2 = ins2.numAttributes();
        if (num1!=num2) return false;

        // num1 == num2
        for (int i=1;i<num1;i++){ // why 1 -> to ignore subjects
            if (!ins1.attribute(i).equals(ins2.attribute(i))){
                System.out.print("different at "+ Integer.toString(i) + "\n");
                return false;
            }
        }
        return true;
    }

    private Instances rmSubjects(Instances origin) throws Exception{
        Remove rmSub = new Remove();
        rmSub.setAttributeIndices("first");
        rmSub.setInputFormat(origin);
        return Filter.useFilter(origin, rmSub);
    }

    public static void saveFileByHand(Instances le, String filename) throws Exception{
        Instances header = new Instances(le,0);
        PrintWriter out = new PrintWriter(filename);
        out.print(header.toString());

        for (int i = 0; i < le.numInstances(); i++) {
            if (i%1000==0){
                out.flush();
            }
            try{
                out.println(le.instance(i));
            }catch (Exception e){
                System.out.println("error at instance index: " + Integer.toString(i) + "  " + e.toString() );
            }
        }
        out.flush();
        out.close();
        out = null;

    }

    private Instances applyChi2(Instances ins, int chi2sel) throws Exception{
        AttributeSelection chi2 = new AttributeSelection();
        Ranker ranker = new Ranker();
        ChiSquaredAttributeEval eva = new ChiSquaredAttributeEval();
        if (chi2sel == -1) {ranker.setThreshold(0.01);}
            else{ranker.setNumToSelect(chi2sel);}

        chi2.setEvaluator(eva);
        chi2.setSearch(ranker);

        chi2.setInputFormat(ins);
        return Filter.useFilter(ins, chi2);

    }

    private Instances applyROCSelection(Instances ins, int num2sel) throws Exception{
        AttributeSelection roc = new AttributeSelection();
        Ranker ranker = new Ranker();
        ROCAttributeEval eva = new ROCAttributeEval();
        ranker.setNumToSelect(num2sel);
        roc.setEvaluator(eva);
        roc.setSearch(ranker);
        roc.setInputFormat(ins);

        return Filter.useFilter(ins, roc);
    }

}
