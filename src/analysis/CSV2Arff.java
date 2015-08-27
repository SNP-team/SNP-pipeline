/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package analysis;

/**
 *
 * @author Aaron
 */
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

import javax.xml.crypto.Data;
import java.io.File;




public class CSV2Arff {
/**
   * takes 2 arguments:
   * - CSV input file
    * - ARFF output file
    */
    public static void main(String[] args) throws Exception {
      if (args.length != 2) {
        System.out.println("\nUsage: CSV2Arff <input.csv> <output.arff>\n");
        System.exit(1);
      }

      // load CSV
      CSVLoader loader = new CSVLoader();
      loader.setSource(new File(args[0]));
      Instances data = loader.getDataSet();

      // save ARFF
      ArffSaver saver = new ArffSaver();
      saver.setInstances(data);
      saver.setFile(new File(args[1]));
      //saver.setDestination(new File(args[1]));
    saver.writeBatch();
  }

  public static boolean convertCSVtoARFF(String input, String output) throws  Exception{
//
//    // load CSV
//    CSVLoader loader = new CSVLoader();
//    System.out.println("1");
//    System.out.println(input);
//    System.out.println(output);
//    loader.setSource(new File(input));
//
//    Instances data = loader.getDataSet();
//    System.out.println("3");
//    // save ARFF
//    ArffSaver saver = new ArffSaver();
//    System.out.println("4");
//    saver.setInstances(data);
//    System.out.println("5");
//    saver.setFile(new File(output));
//    //saver.setDestination(new File(output));
//    saver.writeBatch();
//    return true;

    /* --Yicheng Fei
    the original script is not working and it throws out a GC error
    * so i write a python script to do this work
    * WARNING: this script only works on SNP data(specific format)
    * */
    String command = "python "+ Settings.CSV2ARFFPY_PATH +" "+input+" "+output;
      System.out.println("command: " + command);
    Runtime rt = Runtime.getRuntime();
    Process pr = rt.exec(command);
    pr.waitFor();
    return true;
  }
}
