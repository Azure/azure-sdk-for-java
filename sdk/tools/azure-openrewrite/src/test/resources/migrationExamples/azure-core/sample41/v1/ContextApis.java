import com.azure.core.util.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ContextApis {
    public static void main(String... args) {
        Context context = Context.NONE;

        context.addData("key", "value");
        Object data = context.getData("key").get();

        Map<Object, Object> map = new HashMap<Object, Object>();
        Context mapContext = Context.of(map);

        Optional<Object> optional = context.getData("key");

        // getValues() method is not available in the clientcore library

    }
}
