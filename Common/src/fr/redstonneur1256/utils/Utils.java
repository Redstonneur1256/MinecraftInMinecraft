package fr.redstonneur1256.utils;

public class Utils {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

}