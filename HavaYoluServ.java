import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.security.spec.ECGenParameterSpec;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class HavaYoluServ {
    public static volatile Boolean Info =false;
    public static ArrayList<String>InfoList;
    public static ServerSocket srv;
    public static volatile Queue<Socket> requests;
    public static volatile ReentrantLock lock;

    public static void main (String args[]){
        try{
            srv = new ServerSocket(5050);
            requests = new LinkedList<>();
            InfoList = new ArrayList<>();
            Thread lth = new Thread(new HavaYoluClientListener());
            lth.start();
            lock = new ReentrantLock();
            while (true) {
                if(requests.peek()!=null){
                    Thread thread = new Thread(new HavaYoluCLientHandler(requests.remove()));
                    thread.start();
                } 
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }  
}

class HavaYoluClientListener implements Runnable{

    public void run(){
        try{
            while (true) {
                Socket socket = HavaYoluServ.srv.accept();
                 if(socket!=null){
                     HavaYoluServ.requests.add(socket);
                 }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}


class HavaYoluCLientHandler implements Runnable{
    private Socket socket;

    public HavaYoluCLientHandler(Socket socket){
        this.socket=socket;
    }

    public void run(){
        try{
            ObjectOutputStream bw0 = new ObjectOutputStream(socket.getOutputStream());
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Date date = new Date();
            String name = bf.readLine();
            bw0.writeObject((Object)"1:GetInf 2:makeReservation 3:cancelReservation");
            bw0.flush();
            while (true) {
            String choice=bf.readLine();
           if(choice.equals("1")){
               while(HavaYoluServ.lock.isLocked());
                ArrayList<String> list = null;
                System.out.print("\n\n- - - - - - - - - - -\n\n");
                System.out.println(date);
                System.out.println(name+" looks for available seats:\nState of the seats are :");
                list = VeriTabani.QueryReservation();
                for(int i=0;i<list.size();i++){
                    System.out.println(list.get(i));
                    System.out.print("\n");
                }
                bw0.writeObject((Object)list);
                bw0.flush();
                System.out.print("\n\n- - - - - - - - - - -\n\n");
                
            }else if(choice.equals("2")){
                String prm[] = bf.readLine().split(" ");
                boolean result = VeriTabani.makeReservation(name,prm[0],prm[1]);
                String message = null;
                if(result){
                    message = "rezerve edildi";
                }
                else{
                    message = "rezerve edilemedi";
                }
                bw0.writeObject((Object)message);
                bw0.flush();;
            }else if(choice.equals("3")){
                String prm[] = bf.readLine().split(" ");
                boolean result = VeriTabani.cancelReservation(name, prm[0],prm[1]);
            String message = null;
            if(result)
                message = "Iptal edildi";
            else
                message = "Iptal edilemedi";
                
               bw0.writeObject((Object)message);
               bw0.flush();
            }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    
       
    }
}


class VeriTabani{
    
    public static ArrayList<String> QueryReservation(){
        ArrayList<String> list = new ArrayList<>();
        String line;
        try{
            BufferedReader bf = new BufferedReader(new FileReader("HavaYoluServer.txt"));
            while((line=bf.readLine())!=null){
                list.add(line);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return list;
    }

    public static boolean makeReservation(String name,String id, String seat){
        HavaYoluServ.lock.lock();
        System.out.println(name + " Locked");
        System.out.println("-----------------");
        System.out.println();
        Date date = new Date();
        System.out.println(date);
        System.out.println(name+" tries to book the "+seat);
        System.out.println();
        System.out.println("-----------------");
        ArrayList<String> list = new ArrayList<>();
        try{
            Thread.sleep(10000);
        }catch(Exception e){
            HavaYoluServ.lock.unlock();
            e.printStackTrace();
        }
        
        try{
            BufferedReader bf = new BufferedReader(new FileReader("HavaYoluServer.txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("deneme.txt"));
            String line;
            while ((line=bf.readLine())!=null) {
                if((line.charAt(0)+"").equals(id)){
                    String flinf[] = line.split(" ");
                    for(int i=0;i<flinf.length;i++){
                        if(flinf[i].equals(seat) && flinf[i+1].equals("bos")){
                            flinf[i+1]="dolu";
                            line = String.join(" ",flinf);
                           bw.write(line);
                           bw.newLine();
                           BufferedWriter writer = new BufferedWriter(new FileWriter("bkdSeats.txt",true));
                           writer.write(name +" " + id +" " + seat+"\n");
                           writer.close();
                           list.add("rezerve edildi");
                        }else if(flinf[i].equals(seat) && flinf[i+1].equals("dolu")){
                            bw.write(line);
                            bw.newLine();
                        }

                    }
                    
                }else{
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.close();
            degistir("deneme.txt","HavaYoluServer.txt");
            
        }catch(Exception e){
            e.printStackTrace();
            HavaYoluServ.lock.unlock();
        }
        if(list.size()==1){
            Date date1 = new Date();
            System.out.println(date1);
            System.out.println(name + " booked "+seat +" succesfully");
            System.out.println();
            HavaYoluServ.lock.unlock();
            return true;
        }
        Date date1 = new Date();
        System.out.println(date1);
        System.out.println(name + " could not booked "+seat+ " since it has been already booked");
        System.out.println();
        HavaYoluServ.lock.unlock();
        return false;


    }

    public static boolean cancelReservation(String name,String id,String seat){
       HavaYoluServ.lock.lock();
       System.out.println(name + " Locked");
        System.out.println("-----------------");
        System.out.println();
        Date date = new Date();
        System.out.println(date);
        System.out.println(name + " tries to cancel" +seat);
        System.out.println();
        System.out.println("--------------");
        ArrayList<String> list = new ArrayList<>();
        try{
            Thread.sleep(10000);
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            BufferedReader bf = new BufferedReader(new FileReader("HavaYoluServer.txt"));
            BufferedReader reader = new BufferedReader(new FileReader("bkdSeats.txt"));
            String line;
            BufferedWriter bw = new BufferedWriter(new FileWriter("deneme.txt"));
            boolean a=false;
            while ((line=reader.readLine())!=null) {
                String param[] = line.split(" ");
                if(param[0].equals(name) && param[1].equals(id) && param[2].equals(seat)){
                    while ((line=bf.readLine())!=null) {
                        if((line.charAt(0)+"").equals(id)){
                            String flinf[] = line.split(" ");
                            for(int i=0; i<flinf.length;i++){
                                if(flinf[i].equals(seat) && flinf[i+1].equals("dolu")){
                                    flinf[i+1]="bos";
                                    line = String.join(" ",flinf);
                                   bw.write(line);
                                   bw.newLine();
                                   BkdDegisiklik(name, id, seat);
                                    a=true;
                                    list.add("iptal edildi");
                                }else if(flinf[i].equals(seat) && flinf[i+1].equals("bos")){
                                    bw.write(line);
                                    bw.newLine();
                                }
                            }

                        }else{
                           bw.write(line);
                           bw.newLine();

                        }
                }
                
            }
        }
        bw.close();
        if(a){
            degistir("deneme.txt","HavaYoluServer.txt");
            
        }
    }catch(Exception e){
        e.printStackTrace();
    HavaYoluServ.lock.unlock();
    return false;
    }
    if(list.size()==1){
        Date date1 = new Date();
        System.out.println(date1);
        System.out.println(name + " cancelled "+seat);
        System.out.println();
        HavaYoluServ.lock.unlock();
        return true;
    }
        Date date1 = new Date();
        System.out.println(date1);
        System.out.println(name+ " could not cancelled "+seat);
        System.out.println();
        HavaYoluServ.lock.unlock();
        return false;
    }
    
    public static void degistir(String path1,String path2){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path1));
            String line;
            BufferedWriter writer = new BufferedWriter(new FileWriter(path2));
            while((line=reader.readLine())!=null){
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            reader.close();
    
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void BkdDegisiklik(String name,String id,String seat){
        try{
            String line;
            BufferedReader bf = new BufferedReader(new FileReader("bkdSeats.txt"));
            BufferedWriter bw = new BufferedWriter(new FileWriter("deneme1.txt"));
            while ((line=bf.readLine())!=null) {
                String param[] = line.split(" ");
                if(param[0].equals(name) && param[1].equals(id) && param[2].equals(seat)){
                    
                }else{
                    bw.write(line);
                    bw.newLine();
                }
            }
            bw.close();
            degistir("deneme1.txt","bkdSeats.txt");

        }catch(Exception e){
            e.printStackTrace();
        }


    }
}


