import com.azure.core.util.Context;
import com.azure.core.util.ContextManager;
import com.azure.core.util.ThreadLocalContext;

import static com.azure.core.util.ThreadLocalContext.getThreadLocalContext;

public class ThreadLocalTest {

    public static void main(String[] args) {
        ThreadLocalTest testApp = new ThreadLocalTest();
        testApp.performTask();
    }

    public void performTask() {
        System.out.println("Context before try block: " + getThreadLocalContext());

        System.out.println("NOW STARTING TRY BLOCK");
        try (ThreadLocalContext ctx = ContextManager.newContext()) {
            ctx.addData("Name", "Jonathan")
               .addData("Location", "NZ");
            clientLibraryMethod();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("NOW EXITED TRY BLOCK");

        System.out.println("Context after try block: " + getThreadLocalContext());
    }

    private void clientLibraryMethod() {
        Context ctx = getThreadLocalContext();
        System.out.println("In client library method, we have a context: " + ctx);
    }
}
