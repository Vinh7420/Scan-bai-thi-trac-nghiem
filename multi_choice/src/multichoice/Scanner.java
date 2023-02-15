package multichoice;

import jdk.jshell.ImportSnippet;
import jdk.swing.interop.SwingInterOpUtils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static multichoice.Ulti.*;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.isInteger;
import static org.opencv.imgproc.Imgproc.*;

public class Scanner {
    private final Mat source;
    private final double[] ratio = new double[]{15, 10};
    private final double[] ratio_1 = new double[]{70,35};
    private final double[] ratio_2 = new double[]{15,30};
    private final String[] options = new String[]{"A", "B", "C", "D"};

    private final String[] numbers = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private Mat gray, dilated, thresh, blur, canny, adaptivethresh, hierarchy, closing, Gausblur, cannyGaus;
    private Rect roi, roi2, roi1;
    private List<MatOfPoint> contours, bubbles1, bubbles2, bubbles3;
    private List<Integer> answers;
    JTextArea textArea;

    public Scanner(Mat source, JTextArea textArea) {
        this.source = source;
        this.textArea = textArea;
        hierarchy = new Mat();
        contours = new ArrayList<>();
        answers = new ArrayList<>();
    }

    public void scan() throws Exception {
        dilated = new Mat(source.size(), CV_8UC1);
        dilate(source, dilated, getStructuringElement(MORPH_RECT, new Size(3,3)));
        //Imgcodecs.imwrite("process/dilated.png", dilated );

        gray = new Mat(source.size(), CV_8UC1);
        cvtColor(source, gray, COLOR_BGR2GRAY);
        //Imgcodecs.imwrite("process/gray.png", gray);

        thresh = new Mat(gray.rows(), gray.cols(), gray.type());
        threshold(gray, thresh, 150, 255, THRESH_BINARY+THRESH_OTSU);
        //Imgcodecs.imwrite("process/thresh.png", thresh);

        blur = new Mat(gray.size(), CV_8UC1);
        blur(gray, blur, new Size(5, 5));
        Imgcodecs.imwrite("process/blur.png", blur);

        Gausblur = new Mat(gray.size(), CV_8UC1);
        GaussianBlur(gray, Gausblur, new Size(15,15), 0);
        Imgcodecs.imwrite("process/gausblur.png",Gausblur);

//        Mat kernel = Mat.ones(5,5, CvType.CV_32F);
//        closing = new Mat(blur.size(), CV_8UC1);
//        morphologyEx(blur, closing, MORPH_CLOSE,kernel );


        canny = new Mat(blur.size(), CV_8UC1);
        //Canny(blur, canny, 160, 20);
        Canny(blur,canny, 150, 20);
        Imgcodecs.imwrite("process/canny.png", canny);

        cannyGaus = new Mat(Gausblur.size(), CV_8UC1);
        Canny(Gausblur, cannyGaus, 160, 20);
        Imgcodecs.imwrite("process/CannyGaus.png", cannyGaus);

        adaptivethresh = new Mat(canny.rows(), gray.cols(), gray.type());
        adaptiveThreshold(cannyGaus, adaptivethresh, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 9, 2);
        Imgcodecs.imwrite("process/adapthresh.png", adaptivethresh);

        findSquare();
        findInfo(roi);
        findInfo(roi1);
        findAnswers();
    }

    private void findSquare() throws Exception {
        double width = source.width();
        double height = source.height();
        double threshold = 0;
        double _w = width/this.ratio_1[0];
        double _h = height/this.ratio_1[1];
        double minThreshold = Math.floor(Math.min(_w, _h)) - threshold;
        double maxThreshold = Math.ceil(Math.max(_w, _h)) + threshold;
        System.out.println("minThreshold: " + minThreshold + ", maxThreshold: " + maxThreshold);
        System.out.println(source.size());
        //ArrayList<Rect> arr = new ArrayList<Rect>();
        findContours(adaptivethresh.clone(), contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);

//        double maxVal = 0;
//        int maxValIdx = 0;
//        Rect rectsub = null;
//        int contourId = 0;
//        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
//        {
//            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
//            double ratioL = contourArea/(width*height);
//            if(ratioL>1.1 || ratioL<0.9) {
//                if (maxVal < contourArea) {
//                    maxVal = contourArea;
//                    maxValIdx = contourIdx;
//                    contourId = contourIdx;
//                }
//            }
//        }
//
//        Imgproc.drawContours(source, contours, maxValIdx, new Scalar(0,255,0), 5);
//        Imgcodecs.imwrite("process/largest.png",source);
//
//        Imgcodecs.imwrite("process/sub.png", source.submat(rectsub));

        Rect rects = null;
        ArrayList<MatOfPoint> squares = new ArrayList<>();
        ArrayList<Rect> square = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approxCurve = new MatOfPoint2f(contour.toArray());
            approxPolyDP(approxCurve, approxCurve, 0.02 * arcLength(approxCurve, true), true);
            if (approxCurve.toArray().length == 4) {
                rects = Imgproc.boundingRect(contour);
                double w = rects.width;
                double h = rects.height;
                double ratio = Math.max(w, h) / Math.min(w, h);
                if (ratio >= 0.9 && ratio <= 1.1 && Math.max(w, h) < maxThreshold && Math.min(w, h) >= minThreshold) {
                    squares.add(contour);
                    //System.out.println(rects);
                    square.add(rects);
                    //points.add(new Point(rects.x+rects.width,rects.y+rects.height));
                }
//                Imgproc.drawContours(source, squares, -1, new Scalar(255, 0, 0), -1);
//                Imgcodecs.imwrite("process/draw.png", source);
            }
        }
        System.out.println("check 1");
        System.out.println(square.size());
        //removeDupe(points, square);

        for (int i = 0; i<square.size(); i++){
            points.add(new Point(square.get(i).x+ square.get(i).width,square.get(i).y +square.get(i).height ));
        }
        removeDupe(points, square);

        System.out.println(square.size());
        Collections.sort(square, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                if(o1.y<o2.y){
                    return -1;
                }else if(o1.y>o2.y){
                    return 1;
                }else
                    return 0;
            }
        });
        System.out.println(square);
        ArrayList<Rect> square1 = new ArrayList<>(square.subList(2,square.size()-2));
        System.out.println(square1);
        ArrayList<Rect> square2 = new ArrayList<>(square1.subList(0,4));
        System.out.println(square2);
        ArrayList<Rect> square3 = new ArrayList<>(square1.subList(4,square1.size()-1));
        System.out.println(square3);
        ArrayList<Rect> square4 = new ArrayList<>(square3.subList(0,5));
        Collections.sort(square4, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                if(o1.x<o2.x){
                    return -1;
                }else if (o1.x>o1.y){
                    return 1;
                }else
                    return 0;
            }
        });
        System.out.println(square4);

        int x = findMinX(square2)+20;
        int y = findMinY(square2)+20;
        int widthR = Math.abs(findMaxX(square2)-x);
        int heightR = Math.abs(findMaxY(square2)-y);
        System.out.println("x: "+ x +"; y:"+y +"; w: "+widthR +"; h:"+heightR);
        roi = new Rect(x, y, widthR, heightR);
        Imgcodecs.imwrite("process/roi.png", source.submat(roi));

        int x1 = x + widthR+20;
        int y1 = y+20;
        roi1 = new Rect(x1, y1, widthR, heightR);
        System.out.println("x1: "+ x1 +"; y1:"+y1 +"; w: "+widthR +"; h:"+heightR);
        Imgcodecs.imwrite("process/roi1.png", source.submat(roi1));

        int x2= square4.get(0).x +30;
        int y2= square4.get(0).y +30;
        int widthR2 = Math.abs(findMaxX(square3) - x2)/5;
        int heightR2 = Math.abs(findMaxY(square3) - y2)+10;
        System.out.println("x2: "+ x2 +"; y2:"+y2 +"; w2: "+widthR2 +"; h2:"+heightR2);
        roi2 = new Rect(x2, y2, widthR2, heightR2);
        Imgcodecs.imwrite("process/roi2.png", source.submat(roi2));
    }

        private void findInfo(Rect rect) throws Exception {
        contours.clear();
        Mat subInfo = canny.submat(rect);
        findContours(subInfo, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        double threshold = 0;
        double _w = rect.width / this.ratio[0];
        double _h = rect.height / this.ratio[1];
        double minThreshold1 = Math.floor(Math.min(_w, _h)) - threshold;
        double maxThreshold1 = Math.ceil(Math.max(_w, _h)) + threshold;

        System.out.println("findBubbles > ideal circle size > minThreshold: " + minThreshold1 + ", maxThreshold: " + maxThreshold1);

        List<MatOfPoint> drafts = new ArrayList<>();
        List<Rect> rects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect _rect = boundingRect(contour);
            int w = _rect.width;
            int h = _rect.height;
            double ratio = Math.max(w, h) / Math.min(w, h);
            if (ratio >= 0.9 && ratio <= 1.1) {
                if (Math.max(w, h) < maxThreshold1 && Math.min(w, h) > minThreshold1){
                    drafts.add(contour);
                    rects.add(_rect);
                    //System.out.println("findBubbles > founded circle > w: " + w + ", h: " + h);
                }
            }
        }
        System.out.println("check 2");
        System.out.println(rects.size());
        removeDupe(rects, drafts);

        //Imgproc.drawContours(subInfo, drafts, -1, new Scalar(255, 0, 0), -1);
        //Imgcodecs.imwrite("process/bubbles.png", subInfo);
        System.out.println("findBubbles > bubbles.size: " + drafts.size());

        sortLeft2Right(drafts);

        bubbles1 = new ArrayList<>();

            for (int j = 0; j < drafts.size(); j += numbers.length) {

                List<MatOfPoint> column1 = drafts.subList(j, j + numbers.length);
                sortTopLeft2BottomRight(column1);
                bubbles1.addAll(column1);
            }
            recognizeAnswers(numbers, rect, bubbles1);
            bubbles1.clear();
            answers.clear();
    }

    private void findAnswers() throws Exception {
        contours.clear();
        Mat subInfo = canny.submat(roi2);
        findContours(subInfo, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        double threshold = 0;
        double _w = roi2.width / this.ratio_2[0];
        double _h = roi2.height / this.ratio_2[1];
        double minThreshold2 = Math.floor(Math.min(_w, _h)) - threshold;
        double maxThreshold2 = Math.ceil(Math.max(_w, _h)) + threshold;

        //System.out.println("findBubbles > ideal circle size > minThreshold: " + minThreshold2 + ", maxThreshold: " + maxThreshold2);

        List<MatOfPoint> drafts1 = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect _rect = boundingRect(contour);
            int w = _rect.width;
            int h = _rect.height;
            double ratio = Math.max(w,h) / Math.min(w,h);
            if (ratio >= 0.9 && ratio <= 1.1) {
                if (Math.max(w, h) < maxThreshold2 && Math.min(w, h) >= minThreshold2) {
                    drafts1.add(contour);
                    //System.out.println("findBubbles > founded circle > w: " + w + ", h: " + h);
                }
            }
        }
        System.out.println("check 3");
//        Imgproc.drawContours(subInfo, drfts1, -1, new Scalar(255, 0, 0), -1);
//        Imgcodecs.imwrite("process/bubbles2.png", subInfo);
//        for (int i =0; i<drafts.size();i++){
//            System.out.println(drafts.get(i));
//        }
        System.out.println("findBubbles > bubbles.size: " + drafts1.size());
        sortTopLeft2BottomRight(drafts1);
        bubbles2 = new ArrayList<>();

        for (int j = 0; j < drafts1.size(); j += options.length) {

            List<MatOfPoint> row = drafts1.subList(j, j + options.length);

            sortLeft2Right(row);
            bubbles2.addAll(row);
        }
        recognizeAnswers(options, roi2, bubbles2);
        answers.clear();
    }

    private void recognizeAnswers(String [] array, Rect rect, List<MatOfPoint> bubbles) {
        for (int i = 0; i < bubbles.size(); i += array.length) {
            List<MatOfPoint> column = bubbles.subList(i, i + array.length);
            int[][] filled = new int[column.size()][array.length];

            for (int j = 0; j < column.size(); j++) {

                MatOfPoint col = column.get(j);

                List<MatOfPoint> list = Arrays.asList(col);

                Mat mask = new Mat(thresh.size(), CvType.CV_8UC1);
                drawContours(mask.submat(rect), list, -1, new Scalar(255, 0, 0), -1);

                Mat conjuction = new Mat(thresh.size(), CvType.CV_8UC1);
                Core.bitwise_and(thresh, mask, conjuction);

                //Imgcodecs.imwrite("process/mask"+ a + "_" + i + "_" + j + ".png", mask);

                //Imgcodecs.imwrite("process/conjuction"+ a + "_" + i + "_" + j + ".png", conjuction);

                int countNonZero = Core.countNonZero(conjuction);

                //System.out.println("recognizeAnswers > " + i + ":" + j + " > countNonZero: " + countNonZero);

                filled[j] = new int[]{countNonZero, i, j};
            }

            int[] selection = chooseFilledCircle(filled, array);
            //System.out.println("recognizeAnswers > selection is " + (selection == null ? "empty/invalid" : selection[2]));

//            if(selection != null){
//
//                drawContours(source.submat(rect), Arrays.asList(column.get(selection[2])), -1, new Scalar(0, 255, 0), 3);
//                Imgcodecs.imwrite("process/drawanswers.png", source);
//            }

            answers.add(selection == null ? null : selection[2]);
        }

        if(array == options){
            System.out.println("The answers is .....");
            textArea.append("The answers is ....." + "\n");
            for(int index = 0; index < answers.size(); index++){
                Integer optionIndex = answers.get(index);
                //System.out.println((index +1) + ". " + (optionIndex == null ? "" : array[optionIndex]));
                textArea.append((index +1) + ". " + (optionIndex == null ? "" : array[optionIndex]) + "\n");
            }
        }else if (array == numbers){
            //System.out.println("check 4");
            //System.out.println("Ma sinh vien:");
            if(rect == roi){
                textArea.append("Ma sinh vien:");
            } else if (rect == roi1) {
                textArea.append("Ma de:");
            }
            String option = "";
            for(int index = 0; index < answers.size(); index++){
                Integer optionIndex = answers.get(index);
                //System.out.println((optionIndex == null ? "" : options[optionIndex]));
                if(optionIndex== null){
                    option = option + "?";
                }else {
                    option = option + array[optionIndex];
                }
            }
            System.out.println(option + "\n");
            textArea.append(option+ "\n");
        }
    }
    private int[] chooseFilledCircle(int[][] rows, String [] array){
        double mean = 0;
        for(int i = 0; i < rows.length; i++){
            mean += rows[i][0];
        }
        mean = 0.9d * mean / array.length;

        int anomalouses = 0;
        for(int i = 0; i < rows.length; i++){
            if(rows[i][0] > mean) anomalouses++;
        }

        if(anomalouses == array.length - 1){

            int[] lower = null;
            for(int i = 0; i < rows.length; i++){
                if(lower == null || lower[0] > rows[i][0]){
                    lower = rows[i];
                }
            }

            return lower;

        } else {
            return null;
        }
    }
}

