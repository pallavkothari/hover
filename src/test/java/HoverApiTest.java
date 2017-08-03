import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * Created by pallav.kothari on 5/3/17.
 */
public class HoverApiTest {

    public static final String USERNAME = System.getenv("HOVER_USERNAME");
    public static final String PASSWORD = System.getenv("HOVER_PASSWORD");
    public static final String ZEROLIGHTNING_COM = "zerolightning.com";
    public static final String TEST_CNAME = "foo";
    private final HoverApi api = new HoverApi(USERNAME, PASSWORD);

    @Before
    public void before() {
        api.login();
    }

    @After
    public void after() {
        for (HoverApi.Domain domain : api.getDomainsWithDns(ZEROLIGHTNING_COM).getDomains()) {
            for (HoverApi.DnsEntry dnsEntry : domain.getEntries()) {
                if (dnsEntry.getName().equals(TEST_CNAME)) {
                    api.deleteDnsEntry(dnsEntry.getId());
                }
            }
        }
    }

    @Test
    public void testAdd() {
        api.getDomains();
        api.getDomainsWithDns(ZEROLIGHTNING_COM);
        HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
        dns.setName(TEST_CNAME);
        dns.setDnsTarget("foo.herokuspace.com");
        api.addDnsEntry(ZEROLIGHTNING_COM, dns);

        try {
            api.addDnsEntry(ZEROLIGHTNING_COM, dns); // should fail
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testUpdate() {
        String oldTarget = "foo.herokuspace.com";
        String newTarget = "bar.herokuspace.com";
        HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
        dns.setName(TEST_CNAME);
        dns.setDnsTarget(oldTarget);
        api.addDnsEntry(ZEROLIGHTNING_COM, dns);

        HoverApi.DnsEntry dnsEntry = api.getDnsEntry(ZEROLIGHTNING_COM, TEST_CNAME);
        assertEquals(oldTarget, dnsEntry.getDnsTarget());

        dns.setDnsTarget("bar.herokuspace.com");
        api.updateDnsTarget(ZEROLIGHTNING_COM, dns);

        dnsEntry = api.getDnsEntry(ZEROLIGHTNING_COM, TEST_CNAME);
        assertEquals(newTarget, dnsEntry.getDnsTarget());
    }

    @Test
    public void checkIfCnameExists() {
        String cname = "*.zero-demo-shared.sites";
        HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
        dns.setName(cname);

        HoverApi.Domains domainsWithDns = api.getDomainsWithDns(ZEROLIGHTNING_COM);
        HoverApi.Domain domain = domainsWithDns.getDomains().get(0);
        assertTrue(api.exists(dns, domain));
    }

}
