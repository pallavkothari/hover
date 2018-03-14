import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Expose a CLI to work with hover
 * Created by pallav.kothari on 5/4/17.
 */
@Slf4j
@Data
public class HoverCli {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Parameter(names = {"-u", "--username"})
    private String username;

    @Parameter(names = {"-p", "--password"}, password = true)
    private String password;

    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    public static void main(String[] args) {
        HoverCli cli = new HoverCli();

        ListDomainsCommand listDomainsCommand = new ListDomainsCommand();
        ListCnamesCommand listCnamesCommand = new ListCnamesCommand();
        ListCnameCommand listCnameCommand = new ListCnameCommand();
        AddCnameCommand addCnameCommand = new AddCnameCommand();
        DeleteCnameCommand deleteCnameCommand = new DeleteCnameCommand();
        UpdateCnameCommand updateCnameCommand = new UpdateCnameCommand();
        AddTxtCommand addTxtCommand = new AddTxtCommand();

        JCommander jc = new JCommander.Builder()
                .addObject(cli)
                .addCommand(ListDomainsCommand.LS_DOMAINS, listDomainsCommand)
                .addCommand(ListCnamesCommand.LS_CNAMES, listCnamesCommand)
                .addCommand(ListCnameCommand.LS_CNAME, listCnameCommand)
                .addCommand(AddCnameCommand.ADD_CNAME, addCnameCommand)
                .addCommand(DeleteCnameCommand.DELETE_CNAME, deleteCnameCommand)
                .addCommand(UpdateCnameCommand.UPDATE_CNAME, updateCnameCommand)
                .addCommand(AddTxtCommand.ADD_TXT, addTxtCommand)
                .build();
        try {
            jc.parse(args);
            if (cli.help) {
                jc.usage();
                return;
            }

            String username = Preconditions.checkNotNull(Optional.ofNullable(System.getenv("HOVER_USERNAME")).orElse(cli.getUsername()), "set HOVER_USERNAME env var or use --username CLI param") ;
            String password = Preconditions.checkNotNull(Optional.ofNullable(System.getenv("HOVER_PASSWORD")).orElse(cli.getPassword()), "set HOVER_PASSWORD env var or use --password CLI param") ;
            HoverApi api = new HoverApi(username, password).login();

            String command = Preconditions.checkNotNull(jc.getParsedCommand());

            switch (command) {
                case ListDomainsCommand.LS_DOMAINS:
                    HoverApi.Domains domains = api.getDomains();
                    System.out.println(GSON.toJson(domains.getDomains()));
                    break;
                case ListCnamesCommand.LS_CNAMES:
                    String domain = listCnamesCommand.getDomain();
                    HoverApi.Domains domainsWithDns = api.getDomainsWithDns(domain);
                    System.out.println(GSON.toJson(domainsWithDns.getDomains().get(0).getEntries()));
                    break;
                case ListCnameCommand.LS_CNAME:
                    Optional<HoverApi.DnsEntry> currentDnsEntry = api.getCurrentDnsEntry(listCnameCommand.getDomain(), listCnameCommand.getCname());
                    System.out.println(GSON.toJson(currentDnsEntry.orElse(null)));
                    break;
                case AddCnameCommand.ADD_CNAME:
                    HoverApi.DnsEntry dns = new HoverApi.DnsEntry();
                    dns.setType("CNAME");
                    dns.setName(addCnameCommand.getName());
                    dns.setDnsTarget(addCnameCommand.getDnsTarget());
                    addDnsEntry(api, dns, addCnameCommand.getDomain());
                    break;
                case DeleteCnameCommand.DELETE_CNAME:
                    System.out.println(api.deleteDnsEntry(deleteCnameCommand.getId()));
                    break;
                case UpdateCnameCommand.UPDATE_CNAME:
                    dns = new HoverApi.DnsEntry();
                    dns.setName(updateCnameCommand.getName());
                    dns.setDnsTarget(updateCnameCommand.getDnsTarget());
                    try {
                        System.out.println(api.updateDnsTarget(updateCnameCommand.getDomain(), dns));
                    } catch (IllegalStateException ise) {
                        System.err.println(ise.getMessage());
                        System.exit(1);
                    }
                    break;
                case AddTxtCommand.ADD_TXT:
                    dns = new HoverApi.DnsEntry();
                    dns.setType("TXT");
                    dns.setName(addTxtCommand.getName());
                    dns.setDnsTarget(addTxtCommand.getValue());
                    addDnsEntry(api, dns, addTxtCommand.getDomain());
                    break;
                default:
                    System.err.println("**** unrecognized command " + command);
                    throw new Error();
            }

        } catch (Exception e) {
            jc.usage();
            throw new RuntimeException(e);
        }
    }

    private static void addDnsEntry(HoverApi api, HoverApi.DnsEntry dns, String domain) {
        try {
            String resp = api.addDnsEntry(domain, dns);
            System.out.println(resp);
        } catch (IllegalStateException ise) {
            System.err.println(ise.getMessage());
            System.exit(1);
        }
    }

    @Parameters(commandDescription = "list domains")
    private static class ListDomainsCommand {
        static final String LS_DOMAINS = "ls:domains";
    }

    @Parameters(commandDescription = "list cnames") @Data
    private static class ListCnamesCommand {
        public static final String LS_CNAMES = "ls:cnames";

        @Parameter(names = {"--domain", "-d"}, description = "the domain for which you want cnames", required = true)
        private String domain;
    }


    @Parameters(commandDescription = "list cname") @Data
    private static class ListCnameCommand {
        public static final String LS_CNAME = "ls:cname";

        @Parameter(names = {"--domain", "-d"}, description = "the domain for which you want cnames", required = true)
        private String domain;

        @Parameter(names = {"--cname", "-c"}, description = "the exact cname to search for", required = true)
        private String cname;
    }

    @Parameters(commandDescription = "add cname record") @Data
    private static class AddCnameCommand {
        public static final String ADD_CNAME = "add:cname";

        @Parameter(names = {"--domain", "-d"}, description = "the domain for which you want to add this cname", required = true)
        private String domain;

        @Parameter(names = {"--subdomain", "-s"}, description = "the subdomain you're adding a cname record for", required = true)
        private String name;

        @Parameter(names = {"--target", "-t"}, description = "the DNS target for this cname record", required = true)
        private String dnsTarget;
    }

    @Parameters(commandDescription = "delete cname record") @Data
    private static final class DeleteCnameCommand {
        public static final String DELETE_CNAME = "rm:cname";
        @Parameter(names = {"--dns-id", "-id"}, description = "the dns id of the record you want to delete", required = true)
        private String id;
    }

    @Parameters(commandDescription = "Update cname record") @Data
    private static final class UpdateCnameCommand {
        public static final String UPDATE_CNAME = "update:cname";
        @Parameter(names = {"--domain", "-d"}, description = "the domain for which you want to add this cname", required = true)
        private String domain;

        @Parameter(names = {"--subdomain", "-s"}, description = "the subdomain you're adding a cname record for", required = true)
        private String name;

        @Parameter(names = {"--target", "-t"}, description = "the DNS target to update the cname record to", required = true)
        private String dnsTarget;
    }

    @Parameters(commandDescription = "Add TXT record") @Data
    public static final class AddTxtCommand {
        public static final String ADD_TXT = "add:txt";

        @Parameter(names = {"--domain", "-d"}, description = "the domain for which you want to add this TXT record", required = true)
        private String domain;

        @Parameter(names = {"--name", "-n"}, description = "the attribute name", required = true)
        private String name;

        @Parameter(names = {"--value", "-v"}, description = "the attribute value", required = true)
        private String value;
    }


}
