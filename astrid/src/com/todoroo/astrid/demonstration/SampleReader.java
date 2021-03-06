//package com.android.example.spinner;
package com.todoroo.astrid.demonstration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/* Sample code to read in a file generated by the Spinner app and print out the demonstration. */
public class SampleReader {
  public static void main(String[] args) {
    String file = "/home/av/Desktop/demonstration.ser";
    Demonstration demonstration = null;
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(file)));
      demonstration = (Demonstration) in.readObject();
      in.close();
    } catch(Exception e) {
      System.err.println("Way to fuck it up: " + e.toString());
    }
    System.out.println(demonstration.toString());
    return;
  }
}
