[![Build Status](https://travis-ci.org/pallavkothari/hover.svg?branch=master)](https://travis-ci.org/pallavkothari/hover)

# Hover

A command line tool to talk to and manage your hover.com account. Enjoy!

### Install the CLI
```bash
git clone git@github.com:pallavkothari/hover.git hover && cd hover && mvn install && ln -sF $PWD/target/bin/hover /usr/local/bin/hover
```

### Install with gradle
```bash
repositories {
    maven {
        url  "https://dl.bintray.com/pallavkothari/hover" 
    }
}
dependencies {
    compile 'link.pallav:hover:0.4'
}
```

### Usage

Pass credentials by setting the `HOVER_USERNAME` and `HOVER_PASSWORD` environment variables. 
Alternatively, use the `--username`|`--password` CLI options (discouraged). 

```bash
hover --help

hover ls:domains
hover ls:cnames -d mydomain.com
hover ls:cname -d mydomain.com -c cname
hover add:cname -d mydomain.com -s sub.domain -t target.herokuspace.com 
hover update:cname -d mydomain.com -s sub.domain -t target2.herokuspace.com 
hover rm:cname -id dnsId  # get this from ls:cnames
hover add:txt -d <mydomain.com> -n <name> -v <value>
# removing txt records works fine with the existing rm:cname command:
hover rm:cname -id $(hover ls:cname -d mydomain.com -c FOO | jq -r '.id')


or with jq: 
hover  ls:domains | jq -r '.[].domain_name'

```

### Releasing
Follow [these instructions](https://blog.bintray.com/2015/09/17/publishing-your-maven-project-to-bintray/)
```bash
mvn release:prepare
mvn release:perform
```
