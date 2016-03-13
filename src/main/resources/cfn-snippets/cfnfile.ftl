<#--
  Copyright (C) 2016 BlueWizardHat
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<#setting number_format="0">
{
  "AWSTemplateFormatVersion" : "2010-09-09",
  <#if description??>"Description" : "${description}",</#if>

  "Parameters" : {
<#list parameters as parameter>
    "${parameter.name}" : {<#if (parameter.description)??>
      "Description" : "${parameter.description}",</#if>
      "Type" : "${parameter.type}"<#if (parameter.defaultValue)??>,
      "Default" : "${parameter.defaultValue}"</#if>
    }<#sep>,</#sep>
</#list>
  },

  "Resources" : {
<#list securitygroups as sg>
    "${sg.name}" : {
      "Type" : "AWS::EC2::SecurityGroup",
      "Properties" : {<#if (sg.description)??>
        "GroupDescription" : "${sg.description}",</#if>
<#if sg.inbound?has_content>
        "SecurityGroupIngress" : [
<#list sg.inbound as connection>
          {
            <@showsrc src=connection.from />,
            "IpProtocol": "${connection.protocol?lower_case}",
            "FromPort": ${connection.fromPort},
            "ToPort": ${connection.toPort}
          }<#sep>,</#sep>
</#list>
        ],
</#if>
<#if sg.outbound?has_content>
        "SecurityGroupEgress" : [
<#list sg.outbound as connection>
          {
            <@showdest dest=connection.to />,
            "IpProtocol": "${connection.protocol?lower_case}",
            "FromPort": ${connection.fromPort},
            "ToPort": ${connection.toPort}
          }<#sep>,</#sep>
</#list>
        ],
</#if>
        "VpcId" : <@showval value=sg.vpcId /><#if sg.tags?has_content>,</#if>
<#if sg.tags?has_content>
        "Tags" : [
<#list sg.tags as tag>
          {
            "Key"   : "${tag.key}",
            "Value" : <@showval value=tag.value />
          }<#sep>,</#sep>
</#list>
        ]
</#if>
      }
    }<#if sg?has_next || ingress?has_content || egress?has_content>,</#if>
</#list>
<#if ingress?has_content>

<#list ingress as connection>
    "${connection.name}Ingress": {
      "Type": "AWS::EC2::SecurityGroupIngress",
      "Properties" : {
        "GroupId" : <@showval connection.to.value />,
        <@showsrc src=connection.from />,
        "IpProtocol" : "${connection.protocol?lower_case}",
        "FromPort" : ${connection.fromPort},
        "ToPort" : ${connection.toPort}
      }
    }<#if connection?has_next || egress?has_content>,</#if>
</#list>
</#if>
<#if egress?has_content>

<#list egress as connection>
    "${connection.name}Egress": {
      "Type": "AWS::EC2::SecurityGroupEgress",
      "Properties" : {
        "GroupId" : <@showval connection.from.value />,
        <@showdest dest=connection.to />,
        "IpProtocol" : "${connection.protocol?lower_case}",
        "FromPort" : ${connection.fromPort},
        "ToPort" : ${connection.toPort}
      }
    }<#sep>,</#sep>
</#list>
</#if>
  },

  "Outputs" : {
<#list securitygroups as sg>
    "${sg.name}" : {
      "Value" : { "Ref" : "${sg.name}" }
    }<#sep>,</#sep>
</#list>
  }
}
<#macro showval value>
	<#switch value.type>
		<#case "STRING">"${value.value}"<#break>
		<#case "REF">{ "Ref" : "${value.value}" }<#break>
		<#case "JOIN">{ "Fn::Join" : [ "${value.delimiter}", [ <#list value.elements as e><@showval value=e /><#sep>, </#sep></#list> ] ] }<#break>
	</#switch>
</#macro>
<#macro showsrc src>
	<#switch src.type>
		<#case "SG">"SourceSecurityGroupId" : <@showval value=src.value /><#break>
		<#case "CIDR">"CidrIp" : <@showval value=src.value /><#break>
	</#switch>
</#macro>
<#macro showdest dest>
	<#switch dest.type>
		<#case "SG">"DestinationSecurityGroupId" : <@showval value=dest.value /><#break>
		<#case "CIDR">"CidrIp" : <@showval value=dest.value /><#break>
	</#switch>
</#macro>
