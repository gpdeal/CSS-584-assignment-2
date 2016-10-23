/*
 * Assignment 1
 * 
 * Author: Galen Deal
 * Sample code provided by Professor Min Chen
 */

package assignment.pkg2;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.*;

// This class was initially provided by Professor Min Chen, and completed by Galen Deal
public class CBIR extends JFrame {

    private JLabel photographLabel = new JLabel();  //container to hold a large 
    private JButton[] button; //creates an array of JButtons
    private JCheckBox[] checkBoxes;
    private JPanel[] RFButtons; // panels contain buttons along with corresponging checkboxes
    private int[] buttonOrder = new int[101]; //creates an array to keep up with the image order
    private double[] imageSize = new double[101]; //keeps up with the image sizes
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout3;
    private GridLayout gridLayout4;
    private JPanel panelBottom1;
    private JPanel panelBottom2;
    private JPanel panelTop;
    private JPanel buttonPanel;
    private int[][] intensityMatrix = new int[100][26];
    private int[][] colorCodeMatrix = new int[100][65];
    double normalizedFeatureMatrix[][] = new double[100][90];
    int picNo = 0;
    int imageCount = 1; //keeps up with the number of images displayed since the first page.
    int pageNo = 1;
    boolean checkBoxesActive = true;

    // This method was written by Professor Chen as part of the provided sample code.
    public static void main(String args[]) {

        readImage ri = new readImage();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CBIR app = new CBIR();
                app.setVisible(true);
            }
        });
    }

    // This method was written by Professor Chen as part of the provided sample code.
    public CBIR() {
        //The following lines set up the interface including the layout of the buttons and JPanels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Icon Demo: Please Select an Image");
        panelBottom1 = new JPanel();
        panelBottom2 = new JPanel();
        panelTop = new JPanel();
        buttonPanel = new JPanel();
        gridLayout1 = new GridLayout(4, 5, 5, 5);
        gridLayout2 = new GridLayout(2, 1, 5, 5);
        gridLayout3 = new GridLayout(1, 2, 5, 5);
        gridLayout4 = new GridLayout(5, 2, 5, 5);
        setLayout(gridLayout2);
        panelBottom1.setLayout(gridLayout1);
        panelBottom2.setLayout(gridLayout1);
        panelTop.setLayout(gridLayout3);
        add(panelTop);
        add(panelBottom1);
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setLayout(gridLayout4);
        panelTop.add(photographLabel);

        panelTop.add(buttonPanel);
        JButton previousPage = new JButton("Previous Page");
        JButton nextPage = new JButton("Next Page");
        JButton intensity = new JButton("Intensity");
        JButton colorCode = new JButton("Color Code");
        JButton comboRF = new JButton("Intensity/Color Code with RF");
        buttonPanel.add(previousPage);
        buttonPanel.add(nextPage);
        buttonPanel.add(intensity);
        buttonPanel.add(colorCode);
        buttonPanel.add(comboRF);

        nextPage.addActionListener(new nextPageHandler());
        previousPage.addActionListener(new previousPageHandler());
        intensity.addActionListener(new intensityHandler());
        colorCode.addActionListener(new colorCodeHandler());
        comboRF.addActionListener(new comboRFHandler());
        setSize(1100, 800);
        // this centers the frame on the screen
        setLocationRelativeTo(null);

        button = new JButton[101];
        checkBoxes = new JCheckBox[101];
        RFButtons = new JPanel[101];
        GridLayout checkButtonLayout = new GridLayout(2, 1, 0, 0);
        /*This for loop goes through the images in the database and stores them as icons and adds
         * the images to JButtons and then to the JButton array
         */
        for (int i = 1; i < 101; i++) {
            ImageIcon icon;
            icon = new ImageIcon(getClass().getResource("images/" + i + ".jpg"));

            if (icon != null) {
                button[i] = new JButton(icon);
                //panelBottom1.add(button[i]);
                button[i].addActionListener(new IconButtonHandler(i, icon));
                
                // create checkboxes and panels to hold them with corresponding image
                checkBoxes[i] = new JCheckBox();
                checkBoxes[i].setText("" + i + ".jpg");
                RFButtons[i] = new JPanel(checkButtonLayout);
                RFButtons[i].add(button[i]);
                RFButtons[i].add(checkBoxes[i]);
                
                buttonOrder[i] = i;
            }
        }

        readIntensityFile();
        readColorCodeFile();
        readNormalizedFeatureMatrixFile();
        displayFirstPage();
    }

    /*This method opens the intensity text file containing the intensity matrix with the histogram bin values for each image.
     * The contents of the matrix are processed and stored in a two dimensional array called intensityMatrix.
     */
    // This method was originally provided by Professor Chen as part of the provided 
    // sample code and was completed by Galen Deal.
    public void readIntensityFile() {
        intensityMatrix = readIntMatrixFromFile("intensity.txt");
    }

    /*This method opens the color code text file containing the color code matrix with the histogram bin values for each image.
     * The contents of the matrix are processed and stored in a two dimensional array called colorCodeMatrix.
     */
    // This method was originally provided by Professor Chen as part of the provided 
    // sample code and was completed by Galen Deal.
    private void readColorCodeFile() {
        colorCodeMatrix = readIntMatrixFromFile("colorCode.txt");

    }
    
    
    private void readNormalizedFeatureMatrixFile() {
        normalizedFeatureMatrix = readDoubleMatrixFromFile("normalized_features.txt");
    }
    

    // Opens the file named by the passed String and reads and returns the 
    // int[][] matrix stored in that file. 
    private int[][] readIntMatrixFromFile(String filename) {
        int retrievedMatrix[][] = null;
        try {
            // Open file and read the matrix
            ObjectInputStream inputStream;
            inputStream = new ObjectInputStream(new FileInputStream(filename));
            retrievedMatrix = (int[][]) inputStream.readObject();
        } catch (FileNotFoundException EE) {
            System.out.println("The file " + filename + " does not exist");
        } catch (IOException IOE) {
            System.out.println("IOException when reading from " + filename);
        } catch (ClassNotFoundException CNFE) {
            System.out.println("ClassNotFoundException when reading from " + filename);
        }

        return retrievedMatrix;
    }
    
    // Opens the file named by the passed String and reads and returns the 
    // double[][] matrix stored in that file. 
    private double[][] readDoubleMatrixFromFile(String filename) {
        double retrievedMatrix[][] = null;
        try {
            // Open file and read the matrix
            ObjectInputStream inputStream;
            inputStream = new ObjectInputStream(new FileInputStream(filename));
            retrievedMatrix = (double[][]) inputStream.readObject();
        } catch (FileNotFoundException EE) {
            System.out.println("The file " + filename + " does not exist");
        } catch (IOException IOE) {
            System.out.println("IOException when reading from " + filename);
        } catch (ClassNotFoundException CNFE) {
            System.out.println("ClassNotFoundException when reading from " + filename);
        }

        return retrievedMatrix;
    }

    /*This method displays the first twenty images in the panelBottom.  The for loop starts at number one and gets the image
     * number stored in the buttonOrder array and assigns the value to imageButNo.  The button associated with the image is 
     * then added to panelBottom1.  The for loop continues this process until twenty images are displayed in the panelBottom1
     */
    // This method was written by Professor Chen as part of the provided sample code.
    private void displayFirstPage() {
        int imageButNo = 0;
        panelBottom1.removeAll();
        for (int i = 1; i < 21; i++) {
            //System.out.println(button[i]);
            imageButNo = buttonOrder[i];
            
            if (checkBoxesActive) {
                panelBottom1.add(RFButtons[imageButNo]);
            } else {
                panelBottom1.add(button[imageButNo]);
            }
            
            imageCount++;
        }
        panelBottom1.revalidate();
        panelBottom1.repaint();

    }
    
    
    private void clearCheckBoxes() {
        for (int i = 1; i < 101; i++) {
            checkBoxes[i].setSelected(false);
        }
    }

    /*This class implements an ActionListener for each iconButton.  When an icon button is clicked, the image on the 
     * the button is added to the photographLabel and the picNo is set to the image number selected and being displayed.
     */
    // This class was written by Professor Chen as part of the provided sample code.
    private class IconButtonHandler implements ActionListener {

        int pNo = 0;
        ImageIcon iconUsed;

        IconButtonHandler(int i, ImageIcon j) {
            pNo = i;
            iconUsed = j;  //sets the icon to the one used in the button
        }

        public void actionPerformed(ActionEvent e) {
            clearCheckBoxes();
            photographLabel.setIcon(iconUsed);
            picNo = pNo;
        }

    }

    /*This class implements an ActionListener for the nextPageButton.  The last image number to be displayed is set to the 
     * current image count plus 20.  If the endImage number equals 101, then the next page button does not display any new 
     * images because there are only 100 images to be displayed.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
     */
    // This class was written by Professor Chen as part of the provided sample code.
    private class nextPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int endImage = imageCount + 20;
            if (endImage <= 101) {
                panelBottom1.removeAll();
                for (int i = imageCount; i < endImage; i++) {
                    imageButNo = buttonOrder[i];
                    panelBottom1.add(button[imageButNo]);
                    imageCount++;

                }

                panelBottom1.revalidate();
                panelBottom1.repaint();
            }
        }

    }

    /*This class implements an ActionListener for the previousPageButton.  The last image number to be displayed is set to the 
     * current image count minus 40.  If the endImage number is less than 1, then the previous page button does not display any new 
     * images because the starting image is 1.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
     */
    // This class was written by Professor Chen as part of the provided sample code.
    private class previousPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int startImage = imageCount - 40;
            int endImage = imageCount - 20;
            if (startImage >= 1) {
                panelBottom1.removeAll();
                /*The for loop goes through the buttonOrder array starting with the startImage value
             * and retrieves the image at that place and then adds the button to the panelBottom1.
                 */
                for (int i = startImage; i < endImage; i++) {
                    imageButNo = buttonOrder[i];
                    panelBottom1.add(button[imageButNo]);
                    imageCount--;

                }

                panelBottom1.revalidate();
                panelBottom1.repaint();
            }
        }

    }

    /*This class implements an ActionListener when the user selects the intensityHandler button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one.
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
     * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */
    // This class was originally provided by Professor Chen as part of the provided 
    // sample code and was completed by Galen Deal.
    private class intensityHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            clearCheckBoxes();
            int pic = (picNo - 1);
            displayByDifference(intensityMatrix, pic);
        }

    }

    /*This class implements an ActionListener when the user selects the colorCode button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one. 
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
     * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */
    // This class was originally provided by Professor Chen as part of the provided 
    // sample code and was completed by Galen Deal.
    private class colorCodeHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            clearCheckBoxes();
            int pic = (picNo - 1);
            displayByDifference(colorCodeMatrix, pic);
        }
    }
    
    private class comboRFHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //clearCheckBoxes();
            int pic = (picNo - 1);
            displayByDifference(normalizedFeatureMatrix, pic);
        }
    }

    // This method accepts as arguments an int[][] matrix holding either the 
    // intensity histograms or the color code histograms for each image and an
    // int reperesenting the number of the selected image. It then calculates
    // the Manhattan distance between the selected image and each other image
    // using the passed histogram matrix. Once all the distances have been
    // calculated, the buttons in buttonOrder are sorted from lowest to highest
    // distance and re-displayed.
    private void displayByDifference(int[][] imageData, int pic) {

        int selectedPicIBins[] = imageData[pic];
        int selectedPicSize = selectedPicIBins[0];
        ButtonDistance distanceArray[] = new ButtonDistance[imageData.length];

        // for each image in the image set
        for (int i = 0; i < imageData.length; i++) {
            // retrieve the data for the image to compare
            int compPicIBins[] = imageData[i];
            int compPicSize = compPicIBins[0];

            // calculate the Manhattan distance between the two images
            double mDistance = 0;
            for (int bin = 1; bin < selectedPicIBins.length; bin++) {
                double binDistance = Math.abs(
                        ((double) selectedPicIBins[bin] / (double) selectedPicSize)
                        - ((double) compPicIBins[bin] / (double) compPicSize)
                );
                mDistance += binDistance;
            }

            // store the calculated distance along with the image number
            ButtonDistance buttonDistance = new ButtonDistance(i + 1, mDistance);
            distanceArray[i] = buttonDistance;
        }
        // arrange images by distance
        Arrays.sort(distanceArray);

        // reorder the buttons and redisplay first page
        for (int i = 1; i < 101; i++) {
            buttonOrder[i] = distanceArray[i - 1].getButtonNo();
        }

        imageCount = 1;
        displayFirstPage();
    }

    // This class associates a button number with a distance value. It exists so
    // that images can be sorted by their distance value without losing track of
    // their assigned number.
    private class ButtonDistance implements Comparable<ButtonDistance> {

        private int buttonNo;
        private double mDistance;

        public ButtonDistance(int button, double distance) {
            buttonNo = button;
            mDistance = distance;
        }

        public int getButtonNo() {
            return buttonNo;
        }

        public void setButtonNo(int number) {
            buttonNo = number;
        }

        public double getDistance() {
            return mDistance;
        }

        public void setDistance(double distance) {
            mDistance = distance;
        }

        // This method allows ButtonDistance objects to be compared by distance
        // values. This method returns -1 if this object's distance is less than
        // that of the object to which it is being compared, 1 if this object's
        // distance is greater than that of the object to which it is being
        // compared, and 0 if they have equal distance values.
        public int compareTo(ButtonDistance other) {
            double comparison = this.mDistance - other.getDistance();
            int returnVal = 0;
            if (comparison > 0) {
                returnVal = 1;
            } else if (comparison < 0) {
                returnVal = -1;
            }

            return returnVal;
        }
    }

}
