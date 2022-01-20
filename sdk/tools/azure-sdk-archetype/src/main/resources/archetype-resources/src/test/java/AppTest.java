package ${package};

#if( ${junitVersion} == 4 )
import org.junit.Test;
#end

#if( ${junitVersion} == 5)
import org.junit.jupiter.api.Test;
#end

public class AppTest {

    @Test
    public void testApp() {
        // hello world
    }
}
