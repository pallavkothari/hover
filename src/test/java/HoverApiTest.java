import org.junit.Before;
import org.junit.Test;

/**
 *
 * Created by pallav.kothari on 5/3/17.
 */
public class HoverApiTest {

    public static final String USERNAME = "your_username";
    public static final String PASSWORD = "your_password";
    private final HoverApi api = new HoverApi(USERNAME, PASSWORD);

    @Before
    public void before() {
        api.login();
    }

    @Test
    public void test() {
        api.getDomains();
        api.getDomainsWithDns("zerolightning.com");
        HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
        dns.setName("bar");
        dns.setContent("bar.herokuspace.com");
        api.addDnsEntry("zerolightning.com", dns);
    }

    @Test
    public void deleteDns() {
        HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
        dns.setId("dns14446480");
        api.deleteDnsEntry(dns);
    }
}
