# Hover

Install the CLI
```bash
git clone git@github.com:pallavkothari/hover.git hover && cd hover && mvn install && ln -sF $PWD/target/bin/hover /usr/local/bin/hover
```

## Usage

```bash
hover --help

hover -u username -p password ls:domains
hover -u username -p password ls:cnames
hover -u username -p password add:cname -d mydomain.com -s sub.domain -t target.herokuspace.com 
hover -u username -p password update:cname -d mydomain.com -s sub.domain -t target2.herokuspace.com 
hover -u username -p password rm:cname -id dnsId  # get this from ls:cnames

or with jq: 
hover -u username -p password ls:domains | jq -r '.[].domain_name'

```
