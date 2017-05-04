import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Talk to the unofficial hover api over rest
 *
 * Created by pallav.kothari on 5/3/17.
 */
@Slf4j
public class HoverApi {
    private final OkHttpClient client;
    private final CookieManager cookieManager = new CookieManager();
    private final Gson GSON = new Gson();
    private final String username;
    private final String password;

    public HoverApi(String username, String password) {
        this.username = username;
        this.password = password;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .addInterceptor(logging)
                .build();
    }

    @SneakyThrows
    public HoverApi login() {
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, GSON.toJson(new Payload(username, password)));
        Request request = new Request.Builder()
                .url("https://www.hover.com/signin/auth.json")
                .post(body)
                .addHeader("content-type", "application/json; charset=UTF-8")
                .build();

        Response response = client.newCall(request).execute();
        try (ResponseBody body1 = response.body()) {
            Preconditions.checkState(response.isSuccessful(), "check your login credentials");
            List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
            log.info("cookies = " + cookies);
        }

        return this;
    }

    @SneakyThrows
    public Domains getDomains() {
        Request request = new Request.Builder()
                .url("https://www.hover.com/api/domains")
                .get()
                .build();

        return getDomains(request);
    }

    @SneakyThrows
    public Domains getDomainsWithDns(String domain) {
        Domain myDomain = getDomain(domain);

        Request request = new Request.Builder()
            .url(String.format("https://www.hover.com/api/domains/%s/dns", myDomain.getId()))
            .get()
            .build();

        return getDomains(request);
    }

    private Domain getDomain(String domain) {
        Domains domains = getDomains();
        List<Domain> myDomains = domains.getDomains().stream().filter(d -> d.getDomainName().equals(domain)).collect(Collectors.toList());
        Preconditions.checkArgument(myDomains.size() == 1);
        Domain myDomain = Preconditions.checkNotNull(Iterables.getFirst(myDomains, null));
        log.info("myDomain = " + myDomain);
        return myDomain;
    }

    @SneakyThrows
    public String addDnsEntry(String domain, DnsEntry dns) {
        Domain myDomain = getDomain(domain);

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String content = String.format("name=%s&type=CNAME&content=%s", dns.getName(), dns.getContent());
        RequestBody requestBody = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url(String.format("https://www.hover.com/api/domains/%s/dns", myDomain.getId()))
                .post(requestBody)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        Response response = client.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            Preconditions.checkState(response.isSuccessful());
            return body.string();
        }
    }

    private Domains getDomains(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            Preconditions.checkState(response.isSuccessful());
            Domains domains = GSON.fromJson(body.string(), Domains.class);
            log.info("domains = " + domains);
            return domains;
        }
    }

    @SneakyThrows
    public String deleteDnsEntry(DnsEntry dns) {
        Request request = new Request.Builder()
                .url(String.format("https://www.hover.com/api/dns/%s", dns.getId()))
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        try (ResponseBody body = response.body()) {
            Preconditions.checkState(response.isSuccessful());
            return body.string();
        }
    }

    @Data
    private static final class Payload {
        private final String username, password;
    }

    @Data
    public static final class Domains {
        private List<Domain> domains;
    }

    @Data
    public static final class Domain {
        private String id;
        @SerializedName("domain_name") private String domainName;
        private List<DnsEntry> entries;
    }

    @Data
    public static final class DnsEntry {
        private String id, name, type, content;
    }
}
