import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MathsTools {

    @Tool(name = "add", value = "Adds two numbers together")
    public Integer add(@P("a") Integer a, @P("b") Integer b) {
        return a + b;
    }

    @Tool(name = "subtract", value = "Subtracts b from a")
    public Integer subtract(@P("a") Integer a, @P("b") Integer b) {
        return a - b;
    }

    @Tool(name = "multiply", value = "Multiplies two numbers together")
    public Integer multiply(@P("a") Integer a, @P("b") Integer b) {
        return a * b;
    }

    // Something the LLM can't answer itself, this makes sure the LLM uses the tool
    @Tool(name = "hash", value = "Applies the # operator to a number")
    public String hash(@P("a") Integer a) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(a.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

