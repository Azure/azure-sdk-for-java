@JacksonXmlRootElement(localName = "File-SetHTTPHeaders-Headers")
public class CamelCaseTestData {
    public void errorHTTPMethod() { throw new RuntimeException("Error Messages."); }

    public void validHttpMethod() { throw new RuntimeException("Error Messages."); }

    public static void itIsAURLError() { throw new RuntimeException("Error Messages."); }

    protected void invalidXMLMethod() { throw new RuntimeException("Error Messages."); }

    private void shouldNotSearch() { throw new RuntimeException("Error Messages."); }
}
