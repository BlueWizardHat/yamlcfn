/*
 * Copyright (C) 2016 BlueWizardHat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bluewizardhat.yamlcfn.sg.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnConnection;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnEndpoint;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnFile;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnParam;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnSecurityGroup;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnTag;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnValue;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnValue.JoinValue;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnValue.RefValue;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnValue.StringValue;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Alias;
import net.bluewizardhat.yamlcfn.sg.data.yaml.NamedElement;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Param;
import net.bluewizardhat.yamlcfn.sg.data.yaml.SecurityGroup;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Tag;
import net.bluewizardhat.yamlcfn.sg.data.yaml.UnresolvedConnection;
import net.bluewizardhat.yamlcfn.sg.data.yaml.UnresolvedConnection.RefWithPort;
import net.bluewizardhat.yamlcfn.sg.data.yaml.ValueHolder;
import net.bluewizardhat.yamlcfn.sg.data.yaml.YamlFile;

@Slf4j
public class Transformer {
	public static CfnFile transform(YamlFile yamlFile) {
		CfnFile cfnFile = new CfnFile();
		cfnFile.setDescription(yamlFile.getDescription());
		transformParams(yamlFile, cfnFile);
		transformSecurityGroups(yamlFile, cfnFile);
		return cfnFile;
	}

	private static void transformParams(YamlFile yamlFile, CfnFile cfnFile) {
		cfnFile.setParameters(
			yamlFile.getParameters().values().stream()
				.map(p ->
					new CfnParam()
						.setName(p.getCfnName())
						.setType("String")
						.setDescription(p.getDescription())
						.setDefaultValue(p.getDefaultValue())
				)
				.collect(Collectors.toList()));
	}

	private static void transformSecurityGroups(YamlFile yamlFile, CfnFile cfnFile) {
		List<CfnSecurityGroup> cfnSgs = new ArrayList<>();
		Set<String> transformed = new HashSet<>();
		yamlFile.getSecurityGroups().values().forEach(ySg -> {
			CfnSecurityGroup cfnSg = new CfnSecurityGroup()
				.setName(ySg.getCfnName())
				.setDescription(ySg.getDescription())
				.setTags(transformTags(yamlFile, cfnFile, ySg.getTags(), ySg))
				.setVpcId(transformString(ySg.getVpc(), yamlFile, ySg));
			transformInbound(yamlFile, cfnFile, transformed, ySg, cfnSg);
			transformOutbound(yamlFile, cfnFile, transformed, ySg, cfnSg);
			cfnSgs.add(cfnSg);
			transformed.add(ySg.getName());
			log.debug("Transformed security group: '{}'", cfnSg);
		});
		cfnFile.setSecuritygroups(cfnSgs);
	}

	private static void transformInbound(YamlFile yamlFile, CfnFile cfnFile, Set<String> transformed, SecurityGroup ySg, CfnSecurityGroup cfnSg) {
		Set<String> ingressNames = new HashSet<>();
		ySg.getInbound().forEach(unresolved -> {
			RefWithPort ref = (RefWithPort) unresolved;
			CfnConnection connection = new CfnConnection()
					.setFrom(resolveEndPoint(ref.getRef(), yamlFile, ySg, transformed))
					.setTo(new CfnEndpoint.SgEndpoint(new CfnValue.RefValue(cfnSg.getName()), true))
					.setProtocol(ref.getProtocol())
					.setFromPort(ref.getPortSpec().getFromPort())
					.setToPort(ref.getPortSpec().getToPort());

			if (ingressNames.contains(connection.getName())) {
				throw new IllegalArgumentException("Duplicate ingress rule '"+ connection.getName() +"' generated while transforming '" + ySg.getName() + "'");
			}
			ingressNames.add(connection.getName());

			if (connection.getFrom().isInline()) {
				cfnSg.getInbound().add(connection);
			} else {
				cfnFile.getIngress().add(connection);
			}
		});
	}

	private static void transformOutbound(YamlFile yamlFile, CfnFile cfnFile, Set<String> transformed, SecurityGroup ySg, CfnSecurityGroup cfnSg) {
		Set<String> egressNames = new HashSet<>();
		ySg.getOutbound().forEach(unresolved -> {
			List<RefWithPort> refs = resolveOutboundConnections(yamlFile, unresolved, ySg);
			for (RefWithPort ref : refs) {
				CfnConnection connection = new CfnConnection()
						.setFrom(new CfnEndpoint.SgEndpoint(new CfnValue.RefValue(cfnSg.getName()), true))
						.setTo(resolveEndPoint(ref.getRef(), yamlFile, ySg, transformed))
						.setProtocol(ref.getProtocol())
						.setFromPort(ref.getPortSpec().getFromPort())
						.setToPort(ref.getPortSpec().getToPort());

				if (egressNames.contains(connection.getName())) {
					throw new IllegalArgumentException("Duplicate egress rule '"+ connection.getName() +"' generated while transforming '" + ySg.getName() + "'");
				}
				egressNames.add(connection.getName());

				if (connection.getTo().isInline()) {
					cfnSg.getOutbound().add(connection);
				} else {
					cfnFile.getEgress().add(connection);
				}
			}
		});
	}

	private static List<RefWithPort> resolveOutboundConnections(YamlFile yamlFile, UnresolvedConnection unresolved, SecurityGroup self) {
		if (unresolved instanceof RefWithPort) {
			List<RefWithPort> resolved = new ArrayList<>(1);
			resolved.add((RefWithPort) unresolved);
			return resolved;
		}

		log.trace("Attempting to resolve outbound connection '{}' to '{}'", self.getName(), unresolved.getRef());
		SecurityGroup target = "self".equals(unresolved.getRef()) ? self : yamlFile.getSecurityGroups().get(unresolved.getRef());
		if (target != null) {
			List<RefWithPort> resolved = target.getInbound().stream()
				.filter(e -> self.getName().equals(e.getRef()) || ("self".equals(e.getRef()) && self.getName().equals(target.getName())))
				.map(e -> (RefWithPort) e)
				.map(res -> new RefWithPort(unresolved.getRef(), res.getProtocol(), res.getPortSpec()))
				.collect(Collectors.toList());
			if (!resolved.isEmpty()) {
				log.trace("Found incoming connections on '{}' from '{}' -> {}", target.getName(), self.getName(), resolved);
				return resolved;
			}
			throw new IllegalArgumentException("Missing inbound rule on security group: '" + unresolved.getRef() + "'.'" + self.getName() + "'");
		}
		throw new IllegalArgumentException("Security group: '" + unresolved.getRef() + "' not defined");
	}

	private static CfnEndpoint resolveEndPoint(String ref, YamlFile yamlFile, SecurityGroup self, Set<String> transformed) {
		Alias alias = yamlFile.getAliases().get(ref);
		if (alias != null) {
			switch (alias.getType()) {
			case CIDR:
				return new CfnEndpoint.CidrEndpoint(new StringValue(alias.getValue()));
			case SECURITYGROUP:
				return new CfnEndpoint.SgEndpoint(new StringValue(alias.getValue()), true);
			default:
				throw new IllegalArgumentException("Alias of type '" + alias.getType() + "' cannot be used for connections");
			}
		}
		Matcher matcher = paramPattern.matcher(ref);
		if (matcher.matches()) {
			Param param = yamlFile.getParameters().get(matcher.group(1));
			if (param != null) {
				switch (param.getType()) {
				case CIDR:
					return new CfnEndpoint.CidrEndpoint(new RefValue(param.getCfnName()));
				case SECURITYGROUP:
					return new CfnEndpoint.SgEndpoint(new RefValue(param.getCfnName()), true);
				default:
					throw new IllegalArgumentException("Parameter of type '" + param.getType() + "' cannot be used for connections");
				}
			}
			throw new IllegalArgumentException("Cannot resolve '" + ref + "'");
		}
		if ("self".equals(ref)) {
			return new CfnEndpoint.SgEndpoint(new RefValue(self.getCfnName()), false);
		}
		SecurityGroup source = yamlFile.getSecurityGroups().get(ref);
		if (source != null) {
			return new CfnEndpoint.SgEndpoint(new RefValue(source.getCfnName()), transformed.contains(source.getName()));
		}
		throw new IllegalArgumentException("Cannot resolve '" + ref + "'");
	}

	private static List<CfnTag> transformTags(YamlFile yamlFile, CfnFile cfnFile, List<Tag> ytags, NamedElement self) {
		List<CfnTag> cfntags = ytags.stream()
				.map(ytag -> new CfnTag(ytag.getKey(), transformValueHolder(ytag.getValue(), yamlFile, self)))
				.collect(Collectors.toList());
		return cfntags;
	}

	private static final Pattern paramPattern = Pattern.compile("^\\{(.+)\\}$");
	private static final Pattern selfPattern = Pattern.compile("^self(:((cfn-?)?name))?$");
	private static CfnValue transformString(String value, YamlFile yamlFile, NamedElement self) {
		Matcher matcher = paramPattern.matcher(value);
		if (matcher.matches()) {
			String ref = matcher.group(1);
			Param param = yamlFile.getParameters().get(ref);
			if (param != null) {
				return new RefValue(param.getCfnName());
			}
			matcher = selfPattern.matcher(ref);
			if (self != null && matcher.matches()) {
				String selfRef = matcher.group(2);
				if (selfRef != null) {
					switch (selfRef) {
					case "name":
						return new StringValue(self.getName());
					default:
						return new StringValue(self.getCfnName());
					}
				}
				return new RefValue(self.getCfnName());
			}
			throw new IllegalArgumentException("could not resolve parameter '" + value + "'");
		}
		return new StringValue(value);
	}

	private static CfnValue transformValueHolder(ValueHolder value, YamlFile yamlFile, NamedElement self) {
		if (value instanceof ValueHolder.StringValue) {
			return transformString(((ValueHolder.StringValue) value).getValue(), yamlFile, self);
		} else if (value instanceof ValueHolder.Join) {
			ValueHolder.Join joinValue = (ValueHolder.Join) value;
			List<CfnValue> elements = joinValue.getValues()
					.stream()
					.map(e -> transformString(e, yamlFile, self))
					.collect(Collectors.toList());
			return new JoinValue(joinValue.getDelimiter(), elements);
		}
		throw new IllegalArgumentException("could not transform value '" + value + "'");
	}
}
