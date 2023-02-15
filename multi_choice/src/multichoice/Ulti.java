package multichoice;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import javax.swing.plaf.IconUIResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ulti {
    public static void sortTopLeft2BottomRight(List<MatOfPoint> points){
        Collections.sort(points, (e1, e2) -> {

            Point o1 = new Point(e1.get(0, 0));
            Point o2 = new Point(e2.get(0, 0));

            return o1.y > o2.y ? 1 : -1;
        });
    }

    public static void sortLeft2Right(List<MatOfPoint> points){
        // left to right sort
        Collections.sort(points, (e1, e2) -> {

            Point o1 = new Point(e1.get(0, 0));
            Point o2 = new Point(e2.get(0, 0));

            return o1.x > o2.x ? 1 : -1;
        });
    }

    public static void removeDupe(ArrayList<Point> points, ArrayList<Rect>rects){
        for(int i=0; i<points.size()-1; i++){
            double x = points.get(i).x;
            double y = points.get(i).y;
            for (int j = i+1;j< points.size(); j++){
                double X = points.get(j).x;
                double Y = points.get(j).y;
                double ratiox = x/X;
                double ratioy = y/Y;
                if(ratiox>0.9 && ratiox<1.1 && ratioy>0.9 && ratioy<1.1){
                    points.remove(j);
                    rects.remove(j);
                    j--;
                }

            }
        }
    }
    public static int findMinX(ArrayList<Rect> rects){
        int minx = rects.get(0).x;
        for (int i = 1; i < rects.size(); i++) {
            if (minx > rects.get(i).x)
                minx = rects.get(i).x;
        }
        //System.out.println("Min x:"+minx);
        return minx;
    }
    public static int findMinY(ArrayList<Rect> rects){
        int miny = rects.get(0).y;
        for (int i = 1; i < rects.size(); i++) {
            if (miny > rects.get(i).y)
                miny = rects.get(i).y;
        }
        //System.out.println("Min y:"+miny);
        return miny;
    }
    public static int findMaxX(ArrayList<Rect> rects){
        int maxX = rects.get(0).x;
        for (int i = 1; i < rects.size(); i++) {
            if (maxX < rects.get(i).x)
                maxX = rects.get(i).x;
        }
        //System.out.println("Max x:"+maxX);
        return maxX;
    }
    public static int findMaxY(ArrayList<Rect> rects){
        int maxY = rects.get(0).y;
        for (int i = 1; i < rects.size(); i++) {
            if (maxY < rects.get(i).y)
                maxY = rects.get(i).y;
        }
        //System.out.println("Max y:"+maxY);
        return maxY;
    }
    public static void removeDupe(List<Rect> rects, List<MatOfPoint> drafts){
        for (int i =0; i< drafts.size()-1; i++){
            double x = rects.get(i).x;
            double y = rects.get(i).y;
            for (int j = i+1;j< drafts.size(); j++){
                double X = rects.get(j).x;
                double Y = rects.get(j).y;
                double ratiocx = Math.max(x, X) / Math.min(x, X);
                double ratiocy = Math.max(y, Y) / Math.min(y, Y);
                    if (ratiocx>0.9 && ratiocx<1.1 && ratiocy>0.9 && ratiocy<1.1) {
                        rects.remove(j);
                        drafts.remove(j);
                        j --;
                }
            }
        }
    }
//    public static boolean isOverLap(Rect org, Rect cmp) {
//        boolean overlapY = false;
//        boolean overlapX = false;
//        if (org.y == cmp.y) {
//            overlapY = true;
//        } else {
//            // viền trên của 1 trong 2 nằm trong vùng của nhau
//            if ((org.y <= cmp.y && cmp.y <= (org.y + org.height))
//                    &&(cmp.y <= org.y && org.y <= (cmp.y + cmp.height))) {
//                overlapY = true;
//            } // viền dưới của 1 trong 2 nằm trong vùng của nhau
//      else if (((org.y + org.height) >= cmp.y
//                    && (org.y + org.height) <= (cmp.y + cmp.height))
//                    && ((cmp.y + cmp.height) >= org.y && (cmp.y + cmp.height) <= (org.y + org.height))) {
//                overlapY = true;
//            }
//
//        }
//
//        if (org.x == cmp.x) {
//            overlapX = true;
//        } else {
//            // viền phải của 1 trong 2 nằm trong vùng của nhau
//            if ((org.x <= cmp.x && cmp.x <= (org.x + org.width))
//            &&(cmp.x <= org.x && org.x <= (cmp.x + cmp.width))) {
//                overlapX = true;
//            } // viền trái của 1 trong 2 nằm trong vùng của nhau
//      else if (((org.x + org.width) >= cmp.x && (org.x + org.width) <= (cmp.x + cmp.width))
//            &&((cmp.x + cmp.width) >= org.x && (cmp.x + cmp.width) <= (org.x + org.width))) {
//                overlapX = true;
//            }
//
//        }
//        return overlapX && overlapY;
//    }
//    public static void  removeDupeRect(List<Rect> rects, List<MatOfPoint> matOfPoints){
//        for(int i=0; i<rects.size(); i++){
//            for (int j= i+1; j<rects.size(); j++){
//                if(isOverLap(rects.get(i), rects.get(j))){
//                    rects.remove(j);
//                    matOfPoints.remove(j);
//                    j --;
//                }
//            }
//        }
//    }

//    public static double findMin(List<Point>points){
//
//    }
}
