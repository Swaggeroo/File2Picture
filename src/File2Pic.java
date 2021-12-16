import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class File2Pic {
    String path;

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

    public File2Pic(String path, boolean toPic) {
        this.path = path;
        File file = new File(path);
        if (file.isFile()){
            if (toPic){
                convertIntopicture(file,file.getParentFile());
            }else {
                convertIntoFile(file,file.getParentFile());
            }
        }else {
            for (File oFile : file.listFiles()){
                if (oFile.isFile()){
                    if (toPic){
                        convertIntopicture(oFile, new File(file.getAbsolutePath()+"\\outputPictures"));
                    }else {
                        convertIntoFile(oFile, new File(file.getAbsolutePath()+"\\outputFiles"));
                    }
                }
            }
        }
    }

    public void convertIntopicture(File file, File savePath){
        System.out.println("Covert started: "+file.getAbsolutePath());
        try {

            //ini Picture
            dimensions = (int)Math.sqrt(file.length())+1;
            image = new BufferedImage ( dimensions, dimensions, BufferedImage.TYPE_INT_ARGB );
            graphics2D = image.createGraphics();

            //Conv into color´s
            FileInputStream in = new FileInputStream(file);

            //Assemble Name Headder
            for (char c : file.getName().toCharArray()){
                nextVal(((int)c) & 0xFF);
            }
            nextVal(255);

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

            //Print 100%
            System.out.println("100%"+" ("+dataLength+"/"+dataLength+")");

            //Clear Buffers and write Last Data
            paintNextColor(addColor(colTmp));
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
            paintNextColor(addColor(colTmp));
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

    public Color addColor(int [] buffer){
        return new Color(buffer[0],buffer[1],buffer[2]);
    }

    public void convertIntoFile(File file, File savePath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int dimensions = image.getHeight();

        ArrayList<Integer> values = new ArrayList<>();
        for (int y = 0; y<dimensions; y++){
            for (int x = 0; x<dimensions; x++){
                int clr = image.getRGB(x, y);
                values.add((clr & 0x00ff0000) >> 16);
                values.add((clr & 0x0000ff00) >> 8);
                values.add(clr & 0x000000ff);
            }
        }

        try {
            saveFile(savePath, values);
        } catch (IOException e) {
            System.out.println("Save ERROR");
            e.printStackTrace();
        }
    }

    public void saveFile(File savePath, ArrayList<Integer> values) throws IOException {
        if (!savePath.exists()){
            savePath.mkdir();
        }
        File f = new File(savePath+"\\"+getFileName(values));
        System.out.println(f);
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(savePath+"\\"+getFileName(values));
        fos.write(getData(values, getDataLength(values)));
        fos.close();
    }

    public String getFileName(ArrayList<Integer> data) {
        int val = data.get(0);
        int w = 0;
        String s = "";
        while (val!=255){
            s += (char)val;
            w++;
            val = data.get(w);
        }
        return s;
    }

    public int getDataLength(ArrayList<Integer> data){
        int val = data.get(0);
        int w = 0;
        while (val!=255){
            w++;
            val = data.get(w);
        }
        val = 0;
        String s = "";
        while (val!=255){
            s += val;
            w++;
            val = data.get(w);
        }
        return Integer.parseInt(s);
    }

    public byte[] getData(ArrayList<Integer> data, int dataLength){
        byte[] dat = new byte[dataLength];
        int val = data.get(0);
        int w = 0;
        while (val!=255){
            w++;
            val = data.get(w);
        }
        val = 0;
        while (val!=255){
            w++;
            val = data.get(w);
        }
        w++;
        for (int i = 0; i < dataLength; i++){
            val = data.get(w);
            dat[i] = (byte) val;
            w++;
        }
        return dat;
    }


    public static void main(String[] args) {
        System.out.println("Klick");
        new Scanner(System.in).nextLine();
        System.out.println("Started");
        if (Boolean.parseBoolean(args[2])){
            new File2Pic(".\\testFiles\\Logo64.png",true);
        }else {
            new File2Pic(args[0],Boolean.parseBoolean(args[1]));
        }
    }
}
