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
    long dataLengthOld = 0;

    //Percentage
    int p = -1;
    int lastP = -1;

    UI ui = null;

    boolean error = false;
    File currentFile;

    public Pic2File(String[] paths) {
        start(paths);
    }

    public Pic2File(String[] paths, UI ui){
        this.ui = ui;
        start(paths);
    }

    public void start(String[] paths){
        for (String path : paths) {
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
        if (ui != null){
            ui.finished();
        }
    }

    public void convertIntoFile(File file) {
        logMessage("Started: "+file.getAbsolutePath());
        currentFile = file;
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int dimensions = 0;
        try {
            dimensions = image.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
            logMessage("ERROR converting File\nAre you sure that this is a picture:\n"+file.getPath()+"\n");
            finishedItem(file,false);
            return;
        }

        for (int y = 0; y<dimensions; y++){
            for (int x = 0; x<dimensions; x++){
                int clr = image.getRGB(x, y);
                //logMessage("Got RGB Values: "+clr+"\n");
                if (state < 3){
                    nextByte((clr & 0x00ff0000) >> 16);
                }
                if (state < 3){
                    nextByte((clr & 0x0000ff00) >> 8);
                }
                if (state < 3){
                    nextByte(clr & 0x000000ff);
                }
                if (error){
                    break;
                }
            }
            if (error){
                break;
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
            case 3:
                error = true;
                logMessage("ERROR Converting to File are you sure that this is a FilePicture");
                break;
            default:
                logMessage("State Error");
                break;
        }
    }

    public void nexFileNameByte(int val){
        if (val != 255 && val != 256){
            fileName += (char)val;
            if (fileName.length() > 300){
                nexFileNameByte(256);
            }
        }else if (val == 256){
            state = 3;
        }else{
            try {
                if (!savePath.exists()){
                    savePath.mkdir();
                }
                File f = new File(savePath+"\\"+fileName);
                logMessage(f.toString());
                out = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                logMessage("Fatal Save Error");
                state = 3;
                e.printStackTrace();
            }
            state++;
        }
    }

    public void nextDataLengthByte(int val){
        if (val != 255){
            dataLengthBuffer += val;
        }else {
            dataLength = Long.parseLong(dataLengthBuffer);
            dataLengthOld = dataLength;
            dataLengthBuffer = "";
            state++;
        }
    }

    public void writeData(int val){
        if (dataLength > 0){
            try {
                out.write(val);
                out.flush();
                if ((p = 100-(int)(((float)dataLength/(float)dataLengthOld)*100)) > lastP){
                    lastP = p;
                    logMessage(p+"%"+" ("+dataLength+" left)");
                }
            } catch (IOException e) {
                logMessage("Fatal Save Error - Couldn't write byte");
                state = 3;
                e.printStackTrace();
            }
            dataLength--;
        }else {
            logMessage("Finished: "+ fileName+"\n");
            finishedItem(currentFile,true);
            state++;
        }
    }

    public void cleanUp() {
        if (error){
            finishedItem(currentFile,false);
        }
        try {
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        state = 0;
        fileName = "";
        dataLength = 0;
        dataLengthBuffer = "";
        dataLengthOld = 0;
        p = -1;
        lastP = -1;
        error = false;
    }

    public void logMessage(String message){
        if (ui != null){
            ui.addLog(message+"\n");
        }
        System.out.println(message);
    }

    public void finishedItem(File file, boolean successful){
        if (ui != null){
            ui.finishedFile(file,successful);
        }
    }
}
