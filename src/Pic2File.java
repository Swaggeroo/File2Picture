import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Pic2File {
    File savePath;
    FileOutputStream out;

    //State
    int state = 0;
    String fileName = "";
    String dataLengthBuffer = "";
    long dataLength = 0;

    public Pic2File(String path) {
        File file = new File(path);
        if (file.isFile()){
            savePath = file.getParentFile();
            convertIntoFile(file);
        }else {
            try {
                for (File oFile : file.listFiles()){
                    if (oFile.isFile()){
                        savePath = new File(file.getAbsolutePath()+"\\outputFiles");
                        convertIntoFile(oFile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void convertIntoFile(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int dimensions = image.getHeight();
        for (int y = 0; y<dimensions; y++){
            for (int x = 0; x<dimensions; x++){
                int clr = image.getRGB(x, y);
                if (state < 3){
                    nextByte((clr & 0x00ff0000) >> 16);
                }
                if (state < 3){
                    nextByte((clr & 0x0000ff00) >> 8);
                }
                if (state < 3){
                    nextByte(clr & 0x000000ff);
                }
            }
        }

        cleanUp();
    }

    public void nextByte(int val){
        switch (state) {
            case 0:
                nexFileNameByte(val);
                break;
            case 1:
                nextDataLengthByte(val);
                break;
            case 2:
                writeData(val);
                break;
            default:
                System.out.println("State Error");
                break;
        }
    }

    public void nexFileNameByte(int val){
        if (val != 255){
            fileName += (char)val;
        }else{
            try {
                if (!savePath.exists()){
                    savePath.mkdir();
                }
                File f = new File(savePath+"\\"+fileName);
                System.out.println(f);
                out = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                System.out.println("Fatal Save Error");
                e.printStackTrace();
                System.exit(5);
            }
            state++;
        }
    }

    public void nextDataLengthByte(int val){
        if (val != 255){
            dataLengthBuffer += val;
        }else {
            dataLength = Long.parseLong(dataLengthBuffer);
            dataLengthBuffer = "";
            System.out.println(dataLength);
            state++;
        }
    }

    public void writeData(int val){
        if (dataLength > 0){
            try {
                out.write(val);
                out.flush();
            } catch (IOException e) {
                System.out.println("Fatal Save Error - Couldn't write byte");
                e.printStackTrace();
                System.exit(5);
            }
            dataLength--;
        }else {
            state++;
        }
    }

    public void cleanUp() {
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        state = 0;
        fileName = "";
        dataLength = 0;
        dataLengthBuffer = "";
    }
}
