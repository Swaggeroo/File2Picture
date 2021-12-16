import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class File2Pic {
    //File Pasing
    int dataBuffer;
    int dataBufferCounter = -1;
    int[] colTmp = new int[3];

    //Picture Printing
    BufferedImage image;
    Graphics2D graphics2D;
    int dimensions;
    int row = 0;
    int drawCounter = 0;

    public File2Pic(String path) {
        File file = new File(path);
        if (file.isFile()){
            convertIntopicture(file,file.getParentFile());
        }else {
            try {
                for (File oFile : file.listFiles()){
                    if (oFile.isFile()){
                        convertIntopicture(oFile, new File(file.getAbsolutePath()+"\\outputPictures"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void convertIntopicture(File file, File savePath){
        System.out.println("Covert started: "+file.getAbsolutePath());
        try {
            //Calculate PixelCount
            long dataPoints = file.length();
            dataPoints += file.getName().length();
            dataPoints += String.valueOf(file.length()).length();
            dataPoints = dataPoints/3;

            //ini Picture
            dimensions = (int)Math.sqrt(dataPoints)+1;
            image = new BufferedImage ( dimensions, dimensions, BufferedImage.TYPE_INT_ARGB );
            graphics2D = image.createGraphics();

            //Conv into color´s
            FileInputStream in = new FileInputStream(file);

            //Assemble Name Headder
            for (char c : file.getName().toCharArray()){
                nextVal(((int)c) & 0xFF);
            }
            nextVal(255);

            System.out.println(file.length());
            //Assemble Size Headder
            for (char c : String.valueOf(file.length()).toCharArray()){
                nextVal(Integer.parseInt(c+""));
            }
            nextVal(255);

            //Assemble Data
            int i = 0;
            long dataLength = file.length();
            int p = -1;
            int lastP = -1;
            while ((dataBuffer = in.read()) != -1){
                //Print Percentage if Percentage is higher
                if ((p = (int)(((float)i/(float)dataLength)*100)) > lastP){
                    lastP = p;
                    System.out.println(p+"%"+" ("+i+"/"+dataLength+")");
                }
                i++;
                nextVal(dataBuffer);
            }
            in.close();

            //Print 100%
            System.out.println("100%"+" ("+dataLength+"/"+dataLength+")");

            //Clear Buffers and write Last Data
            paintNextColor(getColorFromBuffer(colTmp));
            Arrays.fill(colTmp,0);
            dataBufferCounter = -1;

            //Save Image
            saveImage(file,savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextVal(int val){
        dataBufferCounter++;
        if (dataBufferCounter >= 3) {
            paintNextColor(getColorFromBuffer(colTmp));
            dataBufferCounter = 0;
            Arrays.fill(colTmp,0);
        }
        colTmp[dataBufferCounter] = val;
    }

    public void paintNextColor(Color color){
        graphics2D.setPaint(color);
        if (drawCounter>=dimensions){
            row = (int) drawCounter/dimensions;
        }
        graphics2D.fillRect(drawCounter-(row*(dimensions)), row, 1, 1);
        drawCounter++;
    }

    public void saveImage(File file, File savePath) throws IOException {
        graphics2D.dispose ();
        if (!savePath.exists()){
            savePath.mkdir();
        }
        ImageIO.write (image, "png", new File(savePath + "\\" + file.getName() + ".png"));
        System.out.println("Saved: "+ savePath + "\\" + file.getName() + ".png\n");
    }

    public Color getColorFromBuffer(int [] buffer){
        return new Color(buffer[0],buffer[1],buffer[2]);
    }
}
