import io.clientcore.core.utils.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ContextApis {
    public static void main(String... args) {
        Context context = Context.none();

        context.put("key", "value");
        Object data = context.get("key");

        Map<Object, Object> map = new HashMap<Object, Object>();
        Context mapContext = Context.of(map);

        Optional<Object> optional = Optional.ofNullable(context.get("key"));

        // getValues() method is not available in the clientcore library

    }
}
