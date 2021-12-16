import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class File2Pic {
    String path;

    //File Pasing
    int dataBuffer;
    int dataBufferCounter = -1;
    int[] colTmp = new int[3];

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

    public static void main(String[] args) {
        System.out.println("Klick");
        new Scanner(System.in).nextLine();
        System.out.println("Started");
        if (Boolean.parseBoolean(args[2])){
            new File2Pic(".\\testFiles\\",true);
        }else {
            new File2Pic(args[0],Boolean.parseBoolean(args[1]));
        }
    }

    public void convertIntopicture(File file, File savePath){
        System.out.println("Covert started: "+file.getAbsolutePath());
        try {
            //Conv into color´s
            ArrayList<Color> colors = new ArrayList<>();
            FileInputStream in = new FileInputStream(file);

            //Assemble Name Headder
            for (char c : file.getName().toCharArray()){
                nextVal(colors,((int)c) & 0xFF);
            }
            nextVal(colors,255);

            //Assemble Size Headder
            for (char c : String.valueOf(file.length()).toCharArray()){
                nextVal(colors,Integer.parseInt(c+""));
            }
            nextVal(colors,255);

            //Assemble Data
            int i = 0;
            long dataLength = file.length();
            int p = -1;
            int lastP = -1;
            while ((dataBuffer = in.read()) != -1){
                if ((p = (int)(((float)i/(float)dataLength)*100)) > lastP){
                    lastP = p;
                    System.out.println("Collecting: "+p+"%"+"("+i+"/"+dataLength+")");
                }
                i++;
                nextVal(colors, dataBuffer);
            }

            //Clear Buffers and write Last Data
            colors.add(addColor(colTmp));
            Arrays.fill(colTmp,0);
            dataBufferCounter = -1;

            //Save Image
            saveImage(colors,file,savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextVal(ArrayList<Color> colors, int val){
        dataBufferCounter++;
        if (dataBufferCounter >= 3) {
            colors.add(addColor(colTmp));
            dataBufferCounter = 0;
            Arrays.fill(colTmp,0);
        }
        colTmp[dataBufferCounter] = val;
    }

    public void saveImage(ArrayList<Color> colors, File file, File savePath) throws IOException {
        int dimensions = (int)Math.sqrt(colors.size())+1;
        final BufferedImage image = new BufferedImage ( dimensions, dimensions, BufferedImage.TYPE_INT_ARGB );
        final Graphics2D graphics2D = image.createGraphics ();
        int row = 0;
        int u = 0;
        long dataLength = colors.size();
        int p = -1;
        int lastP = -1;
        for (int i = 0; i < colors.size(); i++) {
            graphics2D.setPaint(colors.get(i));
            if (i>=dimensions){
                row = (int) i/dimensions;
            }
            if ((p = (int)(((float)u/(float)dataLength)*100)) > lastP){
                lastP = p;
                System.out.println("Drawing: "+p+"%"+"("+u+"/"+dataLength+")");
            }
            u++;
            graphics2D.fillRect(i-(row*(dimensions)), row, 1, 1);
        }
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

}
