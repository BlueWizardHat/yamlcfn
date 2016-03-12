package net.bluewizardhat.yamlcfn.sg.data.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

public abstract class UnresolvedConnection {
	@Getter
	@ToString
	@AllArgsConstructor
	public static class Ref extends UnresolvedConnection {
		private final String ref;
	}

	@Getter
	@ToString
	public static class RefWithPort extends Ref {
		private final Protocol protocol;
		private final PortSpec portSpec;

		public RefWithPort(String ref, Protocol protocol, PortSpec portSpec) {
			super(ref);
			this.protocol = protocol;
			this.portSpec = portSpec;
		}
	}

	public abstract String getRef();
}
