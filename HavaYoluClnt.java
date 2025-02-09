import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.security.spec.ECFieldF2m;
import java.util.ArrayList;
import java.util.Scanner;

public class HavaYoluClnt {
    public static void main(String args[]){
        try{
               Socket socket = new Socket("localhost",5050);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                ObjectInputStream readerOB = new ObjectInputStream(socket.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in);
                System.out.println("Type name");
                writer.write(scanner.nextLine());
                writer.newLine();
                writer.flush();
                System.out.println((String)readerOB.readObject());
                while(true){
                
                
                String choice= scanner.nextLine();
                writer.write(choice);
                writer.newLine();
                writer.flush();
                if(choice.equals("1")){
                    ArrayList<String> list = (ArrayList)readerOB.readObject();
                    for(int i=0;i<list.size();i++){
                        System.out.println(list.get(i));
                        System.out.println();
                    }
                }else if(choice.equals("2")){
                    System.out.println("Id ve seat giriniz:");
                    String idSeat = scanner.nextLine() +" "+ scanner.nextLine();
                    writer.write(idSeat);
                    writer.newLine();
                    writer.flush();
                    String list = (String)readerOB.readObject();
                    System.out.println(list);
                }else if(choice.equals("3")){
                    System.out.println("Id ve seat giriniz:");
                    String idSeat = scanner.nextLine() +" "+ scanner.nextLine();
                    writer.write(idSeat);
                    writer.newLine();
                    writer.flush();
                    String list = (String)readerOB.readObject();
                    System.out.println(list);
                }
                System.out.println("yeni islem icin sayi gir:");

            }

        }catch(Exception e){
            e.printStackTrace();
        }


    }
    
}
