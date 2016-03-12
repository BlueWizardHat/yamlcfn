package net.bluewizardhat.yamlcfn.sg.data.yaml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 */
@Getter
@ToString
public class YamlFile {
	private Set<String> reservedNames = new HashSet<>();
	@Setter
	private String description;
	private Map<String, Alias> aliases = new HashMap<>();
	private Map<String, Param> parameters = new LinkedHashMap<>();
	@Setter
	private SecurityGroup defaults = new SecurityGroup("defaults");
	private Map<String, SecurityGroup> securityGroups = new LinkedHashMap<>();

	public Alias alias(String alias) {
		return aliases.get(alias);
	}

	public YamlFile() {
		reservedNames.add("self");
		reservedNames.add("defaults");
		Alias.addAllAliases(aliases);
		PortSpec.addAllNames(reservedNames);
	}

	public void checkExists(String name) {
		if (reservedNames.contains(name)
				|| aliases.containsKey(name)
				|| parameters.containsKey(name)) {
			throw new IllegalArgumentException("'" + name +"' is already defined");
		}

	}
}
