package json;
import java.util.UUID;

public class MakeUUID {
    public static void main(String[] args) 
    {
        UUID randomID = UUID.randomUUID();
        System.out.println("Random UUID: " + randomID);
    }
}

