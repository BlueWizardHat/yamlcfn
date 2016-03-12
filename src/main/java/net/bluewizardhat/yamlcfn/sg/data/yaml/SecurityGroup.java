package net.bluewizardhat.yamlcfn.sg.data.yaml;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class SecurityGroup extends NamedElement  {
	@Setter
	private String description;
	private List<UnresolvedConnection> inbound = new ArrayList<>();
	private List<UnresolvedConnection> outbound = new ArrayList<>();
	@Setter
	private String vpc;
	private List<Tag> tags = new ArrayList<>();

	public SecurityGroup(String name) {
		super(name);
	}

	public SecurityGroup copy(String newName) {
		SecurityGroup clone = new SecurityGroup(newName);
		clone.description = description;
		clone.inbound.addAll(inbound);
		clone.outbound.addAll(outbound);
		clone.vpc = vpc;
		clone.tags.addAll(tags);
		return clone;
	}

	@Override
	protected String postfix(String cfnName) {
		if (cfnName.matches(".*([Ss][Gg]|[Ss]ecurity[Gg]roup)$")) {
			return cfnName;
		}
		return cfnName + "SecurityGroup";
	}

}
