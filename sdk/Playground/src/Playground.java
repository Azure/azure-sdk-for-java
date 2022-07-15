import java.time.Duration;

public class Playground {

    public static void main(String[] args) {

        String name = null;

        Person p = new Person("Lojza", 16);

        p.setName(name);

        System.out.println(p.getName());
    }

}
