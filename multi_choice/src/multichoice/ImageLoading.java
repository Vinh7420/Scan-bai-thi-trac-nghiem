package multichoice;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.Image;


public class ImageLoading extends JFrame{
    JButton button;
   String path;
    JLabel label;
    JTextArea textArea;
    JButton button1;
    public ImageLoading(String path){
        this.path = path;
    }
  public void ImageLoading(){
        button = new JButton("Broswe");
        button.setBounds(100, 650, 150, 50);

        label = new JLabel();
        label.setBounds(10, 10 , 500, 600);
        Border border = BorderFactory.createLineBorder(Color.BLACK, 5);
        label.setBorder(border);

        textArea = new JTextArea();
        textArea.setBounds(600,10, 550,600);
        Border border1 = BorderFactory.createLineBorder(Color.BLACK, 3);
        textArea.setBorder(border1 );
        Font font = new Font("Verdana", Font.BOLD, 20);
        textArea.setFont(font);

        button1 = new JButton("Get answer");
        button1.setBounds(800,650,150,50);

        add(button);
        add(button1);
        add(label);
        add(textArea);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
                JFileChooser file = new JFileChooser();
                file.setCurrentDirectory(new File(System.getProperty("user.home")));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg","gif","png");
                file.addChoosableFileFilter(filter);
                int result = file.showSaveDialog(null);
                if(result == JFileChooser.APPROVE_OPTION){
                    File selectedFile = file.getSelectedFile();
                    path = selectedFile.getAbsolutePath();
                    label.setIcon(ResizeImage(path));
                }
                else if(result == JFileChooser.CANCEL_OPTION){
                    System.out.println(" No Image Select");
                }
            }
        });

      button1.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              Mat source = Imgcodecs.imread(path);
              Scanner scanner = new Scanner(source, textArea);
              try {
                  scanner.scan();
              } catch (Exception ex) {
                  throw new RuntimeException(ex);
              }
          }
      });
      setLayout(null);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(1200,800);
      setVisible(true);
    }
    public ImageIcon ResizeImage(String ImagePath){
        ImageIcon Image = new ImageIcon(ImagePath);
        Image img = Image.getImage();
        Image resizeImage = img.getScaledInstance(label.getWidth(), label.getHeight(), java.awt.Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(resizeImage);
        return image;
    }

}
