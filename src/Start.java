import java.util.Scanner;

public class Start {
    public static void main(String[] args) {
        //for Testing
        if (false){
            args[0] = ".\\testFiles\\Logo64.png.png";
            args[1] = "false";
            System.out.println("Klick");
            new Scanner(System.in).nextLine();
        }

        System.out.println("Started");
        if (Boolean.parseBoolean(args[1])){
            new File2Pic(args[0]);
        }else {
            new Pic2File(args[0]);
        }
    }
}
