package multichoice;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;


public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception{
        System.out.println("...started");
        String path= "";
        ImageLoading imageLoading = new ImageLoading(path);
        imageLoading.ImageLoading();
        while(path == ""){
            path = imageLoading.path;
        }
        System.out.println("finished");

//        System.out.println("...started");
//        String path = "source\\sheet-4.jpg";
//        Mat source = Imgcodecs.imread(path);
//        Scanner scanner = new Scanner(source);
//        scanner.scan();
//        System.out.println("finished");
    }

}
