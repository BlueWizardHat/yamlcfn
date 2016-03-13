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

package net.bluewizardhat.yamlcfn.sg;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.extern.slf4j.Slf4j;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Alias;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Alias.AliasType;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Param;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Param.ParamType;
import net.bluewizardhat.yamlcfn.sg.data.yaml.PortSpec;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Protocol;
import net.bluewizardhat.yamlcfn.sg.data.yaml.SecurityGroup;
import net.bluewizardhat.yamlcfn.sg.data.yaml.Tag;
import net.bluewizardhat.yamlcfn.sg.data.yaml.UnresolvedConnection;
import net.bluewizardhat.yamlcfn.sg.data.yaml.ValueHolder;
import net.bluewizardhat.yamlcfn.sg.data.yaml.YamlFile;

@Slf4j
public class YamlParser {
	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	private YamlFile yamlFile = new YamlFile();

	@SuppressWarnings("unchecked")
	public YamlFile parse(InputStream in) throws IOException {
		List<Map<String, Map<String, ?>>> list = mapper.readValue(in, List.class);

		list.forEach(line -> {
			Entry<String, ?> entry = line.entrySet().iterator().next();
			switch (entry.getKey()) {
			case "description":
				parseDescription(entry.getValue());
				break;
			case "alias":
				parseAlias((Map<String, ?>) entry.getValue());
				break;
			case "param":
				parseParam((Map<String, ?>) entry.getValue());
				break;
			case "defaults":
				parseDefaults((Map<String, ?>) entry.getValue());
				break;
			case "securitygroup":
				parseSecurityGroup((Map<String, ?>) entry.getValue());
				break;
			default:
				throw new IllegalArgumentException("Unknown entry '" + entry.getKey() + "'");
			}
		});
		return yamlFile;
	}

	private void parseDescription(Object values) {
		log.trace("Parsing 'description' : {}", values);
		if (values != null) {
			yamlFile.setDescription(String.valueOf(values));
		}
		log.debug("Description set to: \"{}\"", yamlFile.getDescription());
	}

	private void parseAlias(Map<String, ?> values) {
		log.trace("Parsing 'alias' : {}", values);
		String name = String.valueOf(values.get("name"));
		String type = String.valueOf(values.get("type"));
		String value = String.valueOf(values.get("value"));
		yamlFile.checkExists(name);
		Alias alias = new Alias(name, AliasType.from(type), value);
		yamlFile.getAliases().put(name, alias);
		log.debug("Added alias '{}' : {}", name, alias);
	}

	private void parseParam(Map<String, ?> values) {
		log.trace("Parsing 'param' : {}", values);
		String name = String.valueOf(values.get("name"));
		String type = String.valueOf(values.get("type"));

		String description = values.containsKey("description") ? String.valueOf(values.get("description")) : null;
		String defaultValue = values.containsKey("default") ? String.valueOf(values.get("default")) : null;
		yamlFile.checkExists(name);
		Param param = new Param(name, description, ParamType.from(type), defaultValue);
		yamlFile.getParameters().put(name, param);
		log.debug("Added parameter '{}' : {}", name, param);

	}

	private void parseDefaults(Map<String, ?> values) {
		log.trace("Parsing 'defaults' : {}", values);
		SecurityGroup securityGroup = parseSecurityGroup("defaults", new SecurityGroup("defaults"), values);
		yamlFile.setDefaults(securityGroup);
		log.debug("Set defaults: {}", securityGroup);
	}

	private void parseSecurityGroup(Map<String, ?> values) {
		log.trace("Parsing 'securitygroup' : {}", values);
		String name = (String) values.get("name");
		yamlFile.checkExists(name);
		SecurityGroup securityGroup = parseSecurityGroup(name, yamlFile.getDefaults(), values);
		if (securityGroup.getDescription() == null) {
			throw new IllegalArgumentException("Securitygroup '" + name + "' is missing a description");
		}
		if (securityGroup.getVpc() == null) {
			throw new IllegalArgumentException("Securitygroup '" + name + "' is missing a vpc");
		}
		yamlFile.getSecurityGroups().put(securityGroup.getName(), securityGroup);
		log.debug("Added securitygroup '{}': {}", securityGroup.getName(), securityGroup);
	}

	@SuppressWarnings("unchecked")
	private SecurityGroup parseSecurityGroup(String name, SecurityGroup defaults, Map<String, ?> values) {
		log.trace("  Creating sg from default: {}", defaults);
		log.trace("  Creating sg from values: {}", values);
		SecurityGroup securityGroup = defaults.copy(name);
		if (values != null) {
			if (values.containsKey("description")) {
				securityGroup.setDescription((String) values.get("description"));
			}
			if (values.containsKey("inbound")) {
				// log.debug("  Parsing inbound: {}", values.get("inbound"));
				parseConnections(securityGroup.getInbound(), (List<?>) values.get("inbound"), false);
			}
			if (values.containsKey("outbound")) {
				// log.debug("  Parsing outbound: {}", values.get("outbound"));
				parseConnections(securityGroup.getOutbound(), (List<?>) values.get("outbound"), true);
			}
			if (values.containsKey("vpc")) {
				String vpc = (String) values.get("vpc");
				securityGroup.setVpc(vpc);
			}
			if (values.containsKey("tags")) {
				parseTags(securityGroup.getTags(), (List<Map<String, ?>>) values.get("tags"));
			}
		}
		return securityGroup;
	}

	@SuppressWarnings("unchecked")
	private void parseConnections(List<UnresolvedConnection> connections, List<?> list, boolean allowNamesOnly) {
		list.forEach(e -> {
			if (e instanceof String) {
				if (!allowNamesOnly) {
					throw new IllegalArgumentException("inbound rules must specify type and ports");
				}
				connections.add(new UnresolvedConnection.Ref((String) e));
			} else if (e instanceof Map) {
				Map<String, String> m = (Map<String, String>) e;
				String ref = m.get("ref");
				String type = m.get("type");
				String ports = m.get("ports");
				parsePorts(connections, ref, Protocol.from(type), ports);
			}
		});
	}

	private void parsePorts(List<UnresolvedConnection> connections, String ref, Protocol protocol, String ports) {
		log.trace("Parsing ports: '{}'", ports);
		String[] pa = ports.split(",");
		for (String p : pa) {
			String trimmed = p.trim();
			PortSpec portspec = parseNumericPortsRange(trimmed).orElseGet(() -> PortSpec.commonPort(trimmed));

			if (portspec != null) {
				log.trace("Resolved '{}' = {}", trimmed, portspec);
				connections.add(new UnresolvedConnection.RefWithPort(ref, protocol, portspec));
			} else {
				Alias alias = yamlFile.alias(trimmed);
				if (alias == null) {
					throw new IllegalArgumentException("Unknown port " + trimmed);
				} else if (alias.getType() != AliasType.PORTS) {
					throw new IllegalArgumentException("'" + trimmed + "' is not a ports alias");
				}

				String[] aliasParts = alias.getValue().split(",");
				for (String aliasPart : aliasParts) {
					portspec = parseNumericPortsRange(aliasPart.trim())
							.orElseThrow(() -> new IllegalArgumentException("Unable to parse port alias '"+ alias.getName() + "': " + aliasPart));
					log.trace("Resolved '{}' = {}", trimmed, portspec);
					connections.add(new UnresolvedConnection.RefWithPort(ref, protocol, portspec));
				}
			}
		}
	}

	private static final Pattern numericPorts = Pattern.compile("^(\\d+)(-(\\d+))?$");
	private Optional<PortSpec> parseNumericPortsRange(String range) {
		Matcher matcher = numericPorts.matcher(range);
		if (matcher.matches()) {
			String fromPort = matcher.group(1);
			String toPort = matcher.group(3);
			return Optional.of(toPort != null ? new PortSpec(Integer.parseInt(fromPort), Integer.parseInt(toPort)) : new PortSpec(Integer.parseInt(fromPort)));
		}
		return Optional.empty();
	}


	@SuppressWarnings("unchecked")
	private void parseTags(List<Tag> tags, List<Map<String, ?>> values) {
		log.trace("  Parsing tags: {}", values);
		values.forEach(m -> {
			String key = (String) m.get("key");
			Object value = m.get("value");
			if (value instanceof String) {
				tags.add(new Tag(key, ValueHolder.of((String) value)));
			} else if (value instanceof Map) {
				Map<String, ?> func = (Map<String, String>) value;
				String fn = (String) func.get("fn");
				if (!"join".equals(fn)) {
					throw new IllegalArgumentException("Unknown function: '" + fn + "'");
				}
				String delimiter = (String) func.get("delimiter");
				List<String> fvalues = (List<String>) func.get("values");
				tags.add(new Tag(key, ValueHolder.of(delimiter, fvalues)));
			}
		});
	}
}
