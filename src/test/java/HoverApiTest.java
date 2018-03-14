import com.google.common.base.Preconditions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 *
 * Created by pallav.kothari on 5/3/17.
 */
@Ignore
public class HoverApiTest {

    public static final String USERNAME = System.getenv("HOVER_USERNAME");
    public static final String PASSWORD = System.getenv("HOVER_PASSWORD");
    public static final String ZEROLIGHTNING_COM = "zerolightning.com";
    public static final String TEST_CNAME = "foo";
    public static final String TEST_TXT_NAME = "TXTFOO";
    private final HoverApi api = new HoverApi(USERNAME, PASSWORD);

    @Before
    public void before() {
        Preconditions.checkNotNull(USERNAME, "Set HOVER_USERNAME and HOVER_PASSWORD credentials as env vars for hover");
        Preconditions.checkNotNull(PASSWORD, "Set HOVER_USERNAME and HOVER_PASSWORD credentials as env vars for hover");
        api.login();
    }

    @After
    public void after() {
        for (HoverApi.Domain domain : api.getDomainsWithDns(ZEROLIGHTNING_COM).getDomains()) {
            for (HoverApi.DnsEntry dnsEntry : domain.getEntries()) {
                if (dnsEntry.getName().equals(TEST_CNAME) || dnsEntry.getName().equals(TEST_TXT_NAME)) {
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
    public void testAddTxtRecord() {
        HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
        dns.setType("TXT");
        dns.setName(TEST_TXT_NAME);
        dns.setDnsTarget("BAR");
        api.addDnsEntry(ZEROLIGHTNING_COM, dns);
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

    @Test
    public void getCurrent() {
        Optional<HoverApi.DnsEntry> currentDnsEntry = api.getCurrentDnsEntry(ZEROLIGHTNING_COM, "*.zero-demo-shared.sites");
        assertTrue(currentDnsEntry.isPresent());
        System.out.println("currentDnsEntry = " + currentDnsEntry.get());
    }

    @Test
    public void testDnsEntryType() {
        HoverApi.DnsEntry dnsEntry = new HoverApi.DnsEntry();
        assertThat(dnsEntry.getType(), is("CNAME"));
    }
}
