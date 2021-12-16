import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Pic2File {
    public Pic2File(String path) {
        File file = new File(path);
        if (file.isFile()){
            convertIntoFile(file,file.getParentFile());
        }else {
            try {
                for (File oFile : file.listFiles()){
                    if (oFile.isFile()){
                        convertIntoFile(oFile, new File(file.getAbsolutePath()+"\\outputFiles"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
