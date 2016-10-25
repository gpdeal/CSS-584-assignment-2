/*
 * Assignment 1
 * 
 * Author: Galen Deal
 * Sample code provided by Dr. Min Chen
 */
package assignment.pkg2;

import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

import java.awt.Color;

// This class was initially provided by Dr. Min Chen, and completed by Galen Deal
public class readImage {

    int imageCount = 1;
    double intensityBins[] = new double[26];
    double intensityMatrix[][] = new double[100][26];
    double colorCodeBins[] = new double[65];
    double colorCodeMatrix[][] = new double[100][65];
    double originalFeatureMatrix[][] = new double[100][90];
    double normalizedFeatureMatrix[][] = new double[100][90];

    // This method was part of the sample code provided by Dr. Chen and was 
    // completed by Galen Deal
    /* Each image is retrieved from the file.  The height and width are found for the image and the getIntensity and
     * getColorCode methods are called.
     */
    public readImage() {
        while (imageCount < 101) {

            try {

                // Read the next image file
                File file;
                file = new File("images/" + imageCount + ".jpg");
                if (file.exists()) {
                    BufferedImage image = ImageIO.read(file);

                    int height = image.getHeight();
                    int width = image.getWidth();

                    getIntensity(image, height, width);
                    getColorCode(image, height, width);
                } else {
                    System.err.println("File images/" + imageCount + ".jpg does not exist.");
                }

            } catch (Exception e) {
                System.out.println("Error occurred when reading or processing image " + imageCount + ".");
                //failedReads++;
            }
            ++imageCount;
        }

        writeIntensity();
        writeColorCode();
        writeNormalizedMatrix();

    }

    // This method examines each pixel of the passed image, calculates the 
    // intensity value of that pixel, determines the histogram bin into which
    // that value falls, and increments that bin. Once the entire histogram
    // has been calculated, its values are added to the intensityMatrix.
    public void getIntensity(BufferedImage image, int height, int width) {

        // save the total number of pixels in the image into the first bin
        intensityBins[0] = height * width;

        // For each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // retrieve RGB values of current pixel
                int rgbValues[] = extractRGB(image, x, y);

                // calculate intensity value
                int intensity = (int) ((0.299 * rgbValues[0]) // red
                        + (0.587 * rgbValues[1]) // green
                        + (0.114) * rgbValues[2] // blue
                        );

                // increment the bin correspinding to the pixel's I-value
                int bin = Math.min((intensity / 10) + 1, intensityBins.length - 1);
                intensityBins[bin]++;
            }
        }

        // add contents of intensityBins to intensityMatrix and clear the
        // contents of intensityBins
        for (int i = 0; i < intensityBins.length; ++i) {
            intensityMatrix[imageCount - 1][i] = intensityBins[i];
            intensityBins[i] = 0;
        }
    }

    // This method examines each pixel of the passed image, calculates the 
    // color code of that pixel, determines the histogram bin into which
    // that value falls, and increments that bin. Once the entire histogram
    // has been calculated, its values are added to the colorCodeMatrix.
    public void getColorCode(BufferedImage image, int height, int width) {
        // save the total number of pixels in the image into the first bin
        colorCodeBins[0] = height * width;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // retrieve RGB values of current pixel
                int rgbValues[] = extractRGB(image, x, y);

                // extract the two most significant bits from each of the RGB
                // values
                int colorCodeBits[] = new int[6];
                colorCodeBits[0] = rgbValues[0] >> 7 & 1;
                colorCodeBits[1] = rgbValues[0] >> 6 & 1;
                colorCodeBits[2] = rgbValues[1] >> 7 & 1;
                colorCodeBits[3] = rgbValues[1] >> 6 & 1;
                colorCodeBits[4] = rgbValues[2] >> 7 & 1;
                colorCodeBits[5] = rgbValues[2] >> 6 & 1;

                // construct color code
                byte colorCode = 0;
                for (int i = 0; i < 6; i++) {
                    colorCode <<= 1;
                    colorCode |= colorCodeBits[i];
                }

                colorCodeBins[colorCode + 1]++;
            }
        }

        // add contents of colorCodeBins to colorCodeMatrix and clear the
        // contents of colorCodeBins
        for (int i = 0; i < colorCodeBins.length; ++i) {
            colorCodeMatrix[imageCount - 1][i] = colorCodeBins[i];
            colorCodeBins[i] = 0;
        }

    }

    // This method takes as arguments an image and x and y values specifying a
    // single pixel in that image. It then retrieves the RGB values of that
    // pixel and returns them in a 3-cell array with the red value in the first
    // cell, the green value in the second cell, and the blue value in the
    // third cell.
    private int[] extractRGB(BufferedImage image, int x, int y) {
        int rgbValues[] = new int[3];

        int pixel = image.getRGB(x, y);
        Color color = new Color(pixel);
        rgbValues[0] = color.getRed();
        rgbValues[1] = color.getGreen();
        rgbValues[2] = color.getBlue();

        return rgbValues;
    }

    //This method writes the contents of the colorCode matrix to a file named colorCode.txt.
    private void writeColorCode() {
        writeMatrixFile(colorCodeMatrix, "colorCode.txt");
    }

    //This method writes the contents of the intensity matrix to a file called intensity.txt
    private void writeIntensity() {
        writeMatrixFile(intensityMatrix, "intensity.txt");
    }
    
    
    
    private double[] calculateMeanStdDev(double feature[]) {
        int n = feature.length; // number of elements

        // calculate mean
        double total = 0;
        for (int i = 0; i < n; i++) {
            total += feature[i];
        }
        double mean = (double)total / n;
        
        // calculate variance
        double sum = 0;
        for (int i = 0; i < n; i++) {
            double temp = feature[i] - mean;
            temp *= temp;
            sum += temp;
        }
        double variance = sum / (n - 1);
        
        // calculate standard deviation
        double stdDev = Math.sqrt(variance);
        
        double meanStdDev[] = new double[2];
        meanStdDev[0] = mean;
        meanStdDev[1] = stdDev;
        
        return meanStdDev;
    }
   
    
    // this method calculates the normalized features of intensity and color
    // code and concatinates them into a single matrix, then writes that matrix
    // to normalized_features.txt
    //
    // THIS REALLY SHOULD BE SPLIT INTO MULTIPLE METHODS
    private void writeNormalizedMatrix() {
        double normParameters[][] = new double[90][2];

        // for each feature, put all values of that feature into a single array
        // of doubles
        for (int feature = 1; feature < 90; feature++) {
            // determine current matrix and index
            double matrix[][];
            int index = feature;
            if (feature <= 25) { // first 25 features are intensity
                matrix = intensityMatrix;
            } else {
                matrix = colorCodeMatrix;
                index = feature - 25;
            }
            
            double currentFeature[] = new double[100];
            // for each image's value for the current feature, divide that value
            // by the image's size and store the result in the currentFeature array
            for (int image = 0; image < matrix.length; image++) {
                double rawValue = matrix[image][index];
                double temp = rawValue / matrix[image][0];
                currentFeature[image] = temp;
            }
            
            // for debugging
            //System.out.println("featureValues: " + Arrays.toString(currentFeature));
            //System.out.println("value of first image's current feature: " + matrix[0][index]);
            
            double meanStdDev[] = calculateMeanStdDev(currentFeature);
             // this line is feature - 1 because intensity and color code
             // features begin at index 1 in their matrices (indices 0 store the
             // image size)
            normParameters[feature - 1] = meanStdDev;
        }
        
        
        // go through intensity and color code matrices, normalize their values,
        // and save them to normalized feature matrix
        for (int i = 0; i < 100; i++) { // images
            for (int j = 1; j < 90; j++) { // features
                // determine current matrix and index
                double matrix[][];
                int index = j;
                if (j <= 25) { // first 25 features are intensity
                    matrix = intensityMatrix;
                } else {
                    matrix = colorCodeMatrix;
                    index = j - 25;
                }
                
                // j-1 used here because intensityMatrix indices start at 1
                double featureMean = normParameters[j - 1][0];
                double featureStdDev = normParameters[j - 1][1];
                
                // check for stdDev of 0 - if so, set normalized value to 0
                if (featureStdDev == 0) {
                    normalizedFeatureMatrix[i][j] = 0;
                } else {
                    double featureValue = matrix[i][index] / matrix[i][0];
                    double normalizedValue = (featureValue - featureMean) / featureStdDev;
                    normalizedFeatureMatrix[i][j] = normalizedValue;
                }
            }
        }
        
        // write image size to 0th cell in each image's feature set
        for (int i = 0; i < 100; i++) {
            normalizedFeatureMatrix[i][0] = intensityMatrix[i][0];
        }
        
        
        writeMatrixFile(normalizedFeatureMatrix, "normalized_features.txt");
        
        // for debugging
        try {
            PrintWriter writer = new PrintWriter("normalized_features_log.txt", "UTF-8");
            
            for (int i = 0; i < normalizedFeatureMatrix.length; i++) {
                writer.println("Image " + i + " normalized features: " + Arrays.toString(normalizedFeatureMatrix[i]));
            }
            
            writer.close();
        } catch (Exception e) {
            System.out.println("Error occurred when writing to file normalized_features_log.txt");
        }
        
        
        // for debugging
        //testNormMatrix(normParameters);
    }
    
    
    // for debugging
    private void testNormMatrix(double normParameters[][]) {
        System.out.println("normParameters:");
        for (int i = 0; i < normParameters.length; i++) {
            System.out.println("Feature " + i + ": " + Arrays.toString(normParameters[i]));
        }
        System.out.println();
        System.out.println();
        
        System.out.println("Normalized features of first 5 images\n");
        for (int i = 0; i < 5; i++) { // image
            System.out.println("image 1: " + Arrays.toString(normalizedFeatureMatrix[i]));
        }
        
    }

    // This method writes the passed int[][] matrix to a file of the passed 
    // filename.
    private void writeMatrixFile(int matrix[][], String filename) {
        try {
            ObjectOutputStream outputStream;
            outputStream = new ObjectOutputStream(new FileOutputStream(filename));
            outputStream.writeObject(matrix);
        } catch (Exception e) {
            System.out.println("Error occurred when writing to file " + filename);
        }
    }
    
    // This method writes the passed double[][] matrix to a file of the passed 
    // filename.
    private void writeMatrixFile(double matrix[][], String filename) {
        try {
            ObjectOutputStream outputStream;
            outputStream = new ObjectOutputStream(new FileOutputStream(filename));
            outputStream.writeObject(matrix);
        } catch (Exception e) {
            System.out.println("Error occurred when writing to file " + filename);
        }
    }

    public static void main(String[] args) {
        new readImage();
    }

}
