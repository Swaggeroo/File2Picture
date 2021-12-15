import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class File2Pic {
    String path;

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
        new File2Pic(args[0],Boolean.getBoolean(args[1]));
    }

    public void convertIntopicture(File file, File savePath){
        try {
            //Get Data
            byte[] data  = Files.readAllBytes(file.toPath());

            //Conv into int´s
            ArrayList<Integer> colorIntValues = new ArrayList<>();
            assembleIntColorValues(colorIntValues,file,data);

            //Conv into color´s
            ArrayList<Color> colors = new ArrayList<>();
            for (int i = 0; i < colorIntValues.size(); i = i+3) {
                if (i < colorIntValues.size()-2){
                    colors.add(new Color(colorIntValues.get(i),colorIntValues.get(i+1),colorIntValues.get(i+2)));
                }else if (i < colorIntValues.size()-1){
                    colors.add(new Color(colorIntValues.get(i),colorIntValues.get(i+1),0));
                }else {
                    colors.add(new Color(colorIntValues.get(i),0,0));
                }
            }

            //Save Image
            saveImage(colors,file,savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveImage(ArrayList<Color> colors, File file, File savePath) throws IOException {
        int dimensions = (int)Math.sqrt(colors.size())+1;
        final BufferedImage image = new BufferedImage ( dimensions, dimensions, BufferedImage.TYPE_INT_ARGB );
        final Graphics2D graphics2D = image.createGraphics ();
        int row = 0;
        for (int i = 0; i < colors.size(); i++) {
            graphics2D.setPaint(colors.get(i));
            if (i>=dimensions){
                row = (int) i/dimensions;
            }
            graphics2D.fillRect(i-(row*(dimensions)), row, 1, 1);
        }
        graphics2D.dispose ();
        ImageIO.write ( image, "png", new File (savePath + "\\" + file.getName() + ".png"));
    }

    public void assembleIntColorValues(ArrayList<Integer> colorIntValues, File file, byte[] data){
        for (char c : file.getName().toCharArray()){
            colorIntValues.add(((int)c) & 0xFF);
        }
        colorIntValues.add(255);
        for (char c : String.valueOf(data.length).toCharArray()){
            System.out.println(c);
            colorIntValues.add((Integer.parseInt(c+"")));
        }
        colorIntValues.add(255);
        for (byte d : data) {
            colorIntValues.add(d & 0xFF);
        }
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
