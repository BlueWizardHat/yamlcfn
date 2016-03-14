YamlCfn
==========================================================================

This small project converts a security group description in yaml to a cloudformation template ready for usage in amazon aws. Some knowledge about Cloudformation and YAML is required to use this project.

## Intro

Cloudformation templates are very verbose, even creating just a few security groups easily have you writing several hundred lines of JSON. Having recently written a 1200 line JSON template with a bunch of security groups I got to thinking "there must be an easier way to describe this".

The aim of this project is to reduce the amount of handwritten code by allowing you to describe security groups in YAML and letting YamlCfn take care of all the details of the JSON template, including when to write ingress and egress rules as inline in a security group or add them later in the template.

## The YAML  format

Here is a very simple security group description:

	---
	  - description: "Security groups for Blah"

	  - param:
	      name: "vpc-id"
	      type: "string"
	      description: "vpc for the security groups"

	  - securitygroup:
	      name: "frontend"
	      description: "Frontend"
	      inbound:
	        - { ref: "world", type: "tcp", ports: "http,https" }
	      vpc: "{vpc-id}"

All this does is create a security group that allows incoming connections to port 80 (http) and 443 (https). But this short example already demonstrates some of the features of YamlCfn.

1. We define a parameter "vpc-id" and references it with "{vpc-id}".
2. We make use of some aliases, ie. "world", "http" and "https" (I will go into more details on aliases later, including how to define your own).
3. The basic syntax of the yaml format

Those 14 lines of YAML becomes no less than 40 lines in Cloudformation JSON

	{
	  "AWSTemplateFormatVersion" : "2010-09-09",
	  "Description" : "Security groups for Blah",

	  "Parameters" : {
	    "VpcId" : {
	      "Description" : "vpc for the security groups",
	      "Type" : "String"
	    }
	  },

	  "Resources" : {
	    "FrontendSecurityGroup" : {
	      "Type" : "AWS::EC2::SecurityGroup",
	      "Properties" : {
	        "GroupDescription" : "Frontend",
	        "SecurityGroupIngress" : [
	          {
	            "CidrIp" : "0.0.0.0/0",
	            "IpProtocol": "tcp",
	            "FromPort": 80,
	            "ToPort": 80
	          },
	          {
	            "CidrIp" : "0.0.0.0/0",
	            "IpProtocol": "tcp",
	            "FromPort": 443,
	            "ToPort": 443
	          }
	        ],
	        "VpcId" : { "Ref" : "VpcId" }
	      }
	    }
	  },

	  "Outputs" : {
	    "FrontendSecurityGroup" : {
	      "Value" : { "Ref" : "FrontendSecurityGroup" }
	    }
	  }
	}

You might notice that the parameter "vpc-id" is called "VpcId" in the cloudformation template and that "frontend" is now "FrontendSecurityGroup". Names containing "-" are not allowed in cloudformation so parameters and security groups have two names, one in the YAML templace and one in the cloudformation template. The cloudformation name is taken by uppercasing the first letter of the yaml name, removing all "-", ":" and "_" and uppercasing any letters following these, and also security groups have "SecurityGroup" appended to them.

Additionally every security group will also be added to cloudformations outputs.

### Quick word about the YAML format

In the above example you might notice that the "vpc-id" parameter was specified

	- param:
	    name: "vpc-id"
	    type: "string"
	    description: "vpc for the security groups"

And the inbound rule was:

	- { ref: "world", type: "tcp", ports: "http,https" }

In YAML these are objects and the YAML format allows us some freedom in how to write them, for example the parameter could just as well be written

	- param: { name: "vpc-id", type: "string", description: "vpc for the security groups" }

And the inbound rule could be

	- ref: "world"
	  type: "tcp"
	  ports: "http,https"

## Params and aliases

Before we get to the meat I think it's time to first take a quick look at parameters and aliases.

You have already seen an example of "param", and you saw how a "param" became a parameter in the cloudformation template. 

Params have a mandatory "name" and "type", and optional "description" and "default". "param"s can have the types "cidr", "securitygroup" or "string", these types determinte where you can use them. A param of type "string" can be used in places where a string is expected, where as "cidr" and "securitygroup" are for use in inbound and outbound rules. "cidr" is an ip/submask construction and "securitygroup" is assumed to be a string with a security group id in it.

Aliases are a way to assign logical names to cidr's, ports or security group. They are replaced with their values before the cloudformation template is written and only exist in the yaml template. Take for example this inbound rule to allow connections to a redis server

	inbound:
	  - { ref: "some-other-sg", type: "tcp", ports: "6379" }

When writing this maybe you have no problems remembering that 6379 is the port that redis listens on, but what about in 6 months.. Or if someone else has to maintain the file. Well with an alias you can use a logical name instead of the actual port number, in the top of the file have

	- alias: { name: "redis-port", type: "ports", value: "6379" }

and your inbound rule can now be written as

	inbound:
	  - { ref: "some-other-sg", type: "tcp", ports: "redis-port" }

Aliases can have type "cidr", "ports" and "securitygroup". Like with params, "cidr" is an ip/submask construction and "securitygroup" is a security group id. "ports" is a commaseparated list of port ranges, for example "80,443" or "1-65535" or even a mix like "80,443,500-510".

There are a few predefined aliases:

* "world" is the cidr "0.0.0.0/0"
* "ssh" is port 22
* "smtp" is port 25
* "http" is port 80
* "https" is port 443
* "mysql" is port 3306
* "postgresql" is port 5432
* "echo-request" is port range 8 to -1
* "echo-reply" is port range 0 to -1
* "self" is a special security group that always references the same security group is referenced from

Note that in tags "self" can also be referenced using the parameter syntax, and with special properties.

* "{self:name}" returns the name of the security group it is referenced from.
* "{self:cfnname}" or "self:cfn-name" returns the cloudformation name of the security group it is referenced from.

For example

	tags:
	  - { key: "Name", value: "{self:name}" }

## Security groups

The structure of a security group with all elements could look like this

	- securitygroup:
	    name: "frontend-app"
	    description: "Frontend application"
	    inbound:
	      - { ref: "world", type: "tcp", ports: "http,https" }
	    outbound:
	      - { ref: "world", type: "tcp", ports: "http,https" }
	    vpc: "{vpc-id}"
	    tags:
          - { key: "Name", value: "frontend-app-sg" }

Notice that the indentation matters in YAML.

"name" and "description" are mandatory and must be simple strings. "vpc" is also mandatory and is a string that is subject to alias and parameter parsing. "inbound", "outbound" and "tags" are lists that are optional.

### Inbound rules

Inbound rules always must specify "ref", "type" and "ports". "ref" is either the name a security group in the same file, an alias (type "cidr" or "security group") or a parameter (also type "cidr" or "securitygroup") or "self". "type" is "tcp", "udp" or "icmp". And "ports" is a commaseparated list of aliases or port ranges.

For example

	- securitygroup:
	    name: "frontend-app"
	    inbound:
	      - { ref: "world", type: "tcp", ports: "http,https" }

	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "frontend-app", type: "tcp", ports: "http,https" }

Ignoring for a moment that we are missing the mandatory fields "description" and "vpc", this would create the security group "frontend-app" that allows connections port 80 and 443 from anywhere, using the aliases "world", "http" and "https". And "backend-app" would allow connections only from machines in the "frontend-app" security group, using the same ports.

Self references can be created using either the name of the security group or "self". For example for a hazelcast cluster you would probably do something like this

	- alias: { name: "hz-port", type: "ports", value: "5701" }
	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "self", type: "tcp", ports: "hz-port" }
	      - { ref: "self", type: "icmp", ports: "echo-request,echo-reply" }

Note that in that example using a "ref" of "backend-app" instead of "self" would accomplish the same thing.

Self references are also an example of the descriptive nature of the yaml format, as it is much more complicated to accomplish the same in a cloudformation JSON template. In a cloudformation template self references are actually invalid to put directly in the security group definition. To accomplish a self reference you need to first create a security group and then add separate ingress rules later in the template. YamlCfn will automatically do this for you.

One limitation of inbound rules in YamlCfn is that you cannot use a CIDR as a normal string, in other words this will not work

	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "10.0.0.0/8", type: "tcp", ports: "123" }

To accomplish this you will need to create an alias such as

	- alias: { name: "localnet", type: "cidr", value: "10.0.0.0/8" }
	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "localnet", type: "tcp", ports: "123" }


## Outbound rules and auto-matching

Outbound rules follow the same rules as inbound rules, so to lock down the above hazelcast cluster and make the backend app talk to only itself, you can write

	- alias: { name: "hz-port", type: "ports", value: "5701" }
	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "self", type: "tcp", ports: "hz-port" }
	      - { ref: "self", type: "icmp", ports: "echo-request,echo-reply" }
	    outbound:
	      - { ref: "self", type: "tcp", ports: "hz-port" }
	      - { ref: "self", type: "icmp", ports: "echo-request,echo-reply" }

However outbound rules have an additional feature in that if the outbound rule points to "self" or another security group defined in the same file, then an outbound rule can automatically match inbound rules from the target so in the above example the outbound rule can be shortened and we will get this:

	- alias: { name: "hz-port", type: "ports", value: "5701" }
	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "self", type: "tcp", ports: "hz-port" }
	      - { ref: "self", type: "icmp", ports: "echo-request,echo-reply" }
	    outbound:
	      - "self"

That single outbound "self" reference automatically matches both of the inbound "self" references. That's the way auto matching works, a single outbound rule is enough to match several inbound rules.

Now to combine our frontend and backend hazelcast cluster and throw in a mysql database as well and we almost have a complete although simple application:

	- alias: { name: "hz-port", type: "ports", value: "5701" }

	- securitygroup:
	    name: "frontend-app"
	    inbound:
	      - { ref: "world", type: "tcp", ports: "http,https" }
	    outbound:
	      - "backend-app"

	- securitygroup:
	    name: "backend-app"
	    inbound:
	      - { ref: "frontend-app", type: "tcp", ports: "http,https" }
	      - { ref: "self", type: "tcp", ports: "hz-port" }
	      - { ref: "self", type: "icmp", ports: "echo-request,echo-reply" }
	    outbound:
	      - "self"
	      - "backend-db"

	- securitygroup:
	    name: "backend-db"
	    inbound:
	      - { ref: "backend-app", type: "tcp", ports: "mysql" }

Ofcourse as already said the auto matching feature only works for security groups defined in the same file and for "self", if you need to write an outbound rule to an alias or param then you will still need to specify it with "ref", "type" and "ports".

### Tags

Tags consist of a "key" and a "value". The "key" is a simple string, the value is either a simple string, like:

	tags:
	  - { key: "Name", value: "frontend-app-" }

a string with parameter replacement, like:

	tags:
	  - { key: "Name", value: "{self:name}" }

or the function "join", like:

	tags:
	  - key: "Name"
	    value:
	      fn: "join"
	      delimiter: "-"
	      values: [ "{self:name}", "sg" ]

Every value in the values array of a join are strings or parameter strings. This can be used for example to prefix or postfix something to a security groups name tag. Say for example you want prefix all security groups in the production environment with "prod" and all security group in the staging environment with "staging" and also postfix them all with "sg", you could take the prefix as an argument like this:

	- param:
	    name: "env-prefix"
	    type: "string"
	    description: "environment prefix"

Now you can put this tag on all your security groups

	tags:
	  - key: "Name"
	    value:
	      fn: "join"
	      delimiter: "-"
	      values: [ "{env-prefix}", "{self:name}", "sg" ]


## Defaults

Now we come to the last feature of YamlCfn, "defaults". "defaults" acts just like a normal security group except that it doesn't have a name, no elements are mandatory and it isn't written to the cloudformation template. Instead everything put into "defaults" is automatically copied to all security groups defined below "defaults" in the yaml file.

For example the element "vpc" is probably going to be the same for all security groups in your file to instead of repeating it you can put it in defaults. Also in the tags above we ended up with a join that you  could copy/paste into every single security group definition, or you can put it in defaults.

So let's again define the parameters "env-prefix" and "vpc-id":

	- param:
	    name: "env-prefix"
	    type: "string"
	    description: "environment prefix"
	- param:
	    name: "vpc-id"
	    type: "string"
	    description: "VPC for all security groups"

We can now create a "defaults" section that gives all security groups the vpc and name tag from above:

	- defaults:
	    vpc: "{vpc-id}"
	    tags:
	      - key: "Name"
	        value:
	          fn: "join"
	          delimiter: "-"
	          values: [ "{env-prefix}", "{self:name}", "sg" ]

And the security groups now automatically have the vpc and name tag defined in defaults, you wont have to copy/paste the same definition into all security groups.

Should you want to "defaults" can also take "description" and both inbound and outbound rules. You probably don't want to do that!

Lastly you should know that while defining a security group "description" and "vpc" would override the values from "defaults", however inbound and outbound rules and tags are added to what comes from "defaults". So if you use the above "defaults" example defining a security group with like so

	- securitygroup:
	    name: "something"
	    tags:
	      - { key: "SomeKey", value: "Some value" }

will give this security group both the name tag from "defaults" and the "SomeKey" tag.

Also YamlCfn will not check if you create multiple tags with the same key so 

	- securitygroup:
	    name: "something"
	    tags:
	      - { key: "Name", value: "Some value" }

and you get a security group with two name tags in the cloudformation template.
