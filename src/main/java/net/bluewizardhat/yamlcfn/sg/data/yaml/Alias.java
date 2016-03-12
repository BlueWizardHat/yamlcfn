package net.bluewizardhat.yamlcfn.sg.data.yaml;

import java.util.Map;

import lombok.Value;

@Value
public class Alias {
	public enum AliasType {
		CIDR,
		PORTS,
		SECURITYGROUP;

		public static AliasType from(String str) {
			return AliasType.valueOf(str.toUpperCase());
		}
	}

	public final static Alias world = new Alias("world", AliasType.CIDR, "0.0.0.0/0");

	private String name;
	private AliasType type;
	private String value;

	public static void addAllAliases(Map<String, Alias> aliases) {
		aliases.put(world.getName(), world);
	}

}
