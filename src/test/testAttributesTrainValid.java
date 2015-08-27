package test;

import analysis.BayesAttSel;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by feiyicheng on 8/26/15.
 */
public class testAttributesTrainValid {

    static void test() throws Exception{
        Instances learn = new Instances(new BufferedReader(new FileReader("/Users/feiyicheng/Desktop/SNP/data/sample/temp/training_01.arff")));
        Instances valid = new Instances(new BufferedReader(new FileReader("/Users/feiyicheng/Desktop/SNP/data/sample/temp/validation_01.arff")));
        System.out.print(Boolean.toString(BayesAttSel.attributesEqual(learn, valid)));
    }
}
