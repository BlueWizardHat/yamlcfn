package net.bluewizardhat.yamlcfn.sg.data.yaml;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Param extends NamedElement {
	public enum ParamType {
		CIDR,
		SECURITYGROUP,
		STRING;

		public static ParamType from(String str) {
			return ParamType.valueOf(str.toUpperCase());
		}
	}

	private String description;
	private ParamType type;
	private String defaultValue;

	public Param(String name, String description, ParamType type, String defaultValue) {
		super(name);
		this.description = description;
		this.type = type;
		this.defaultValue = defaultValue;
	}
}
