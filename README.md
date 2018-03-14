[![Build Status](https://travis-ci.org/pallavkothari/hover.svg?branch=master)](https://travis-ci.org/pallavkothari/hover)

# Hover

A command line tool to talk to and manage your hover.com account 

### Install the CLI
```bash
git clone git@github.com:pallavkothari/hover.git hover && cd hover && mvn install && ln -sF $PWD/target/bin/hover /usr/local/bin/hover
```

### Usage

```bash
hover --help

hover -u username -p password ls:domains
hover -u username -p password ls:cnames
hover -u username -p password ls:cname -d mydomain.com -c cname
hover -u username -p password add:cname -d mydomain.com -s sub.domain -t target.herokuspace.com 
hover -u username -p password update:cname -d mydomain.com -s sub.domain -t target2.herokuspace.com 
hover -u username -p password rm:cname -id dnsId  # get this from ls:cnames
hover -u username -p password add:txt -d <mydomain.com> -n <name> -v <value>
# removing txt records works fine with the existing rm:cname command:
hover -u username -p password rm:cname -id $(hover -u username -p password ls:cname -d mydomain.com -c FOO | jq -r '.id')


or with jq: 
hover -u username -p password ls:domains | jq -r '.[].domain_name'

```

### Releasing
```bash
mvn release:prepare
mvn release:perform
```