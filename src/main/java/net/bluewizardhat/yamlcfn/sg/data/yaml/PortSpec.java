package net.bluewizardhat.yamlcfn.sg.data.yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PortSpec {
	private final int fromPort;
	private final int toPort;

	private static Map<String, PortSpec> COMMON_PORTS = new HashMap<>();
	static {
		COMMON_PORTS.put("ssh", new PortSpec(22));
		COMMON_PORTS.put("smtp", new PortSpec(25));
		COMMON_PORTS.put("http", new PortSpec(80));
		COMMON_PORTS.put("https", new PortSpec(443));
		COMMON_PORTS.put("http-alt", new PortSpec(8080));
		COMMON_PORTS.put("mysql", new PortSpec(3306));
		COMMON_PORTS.put("postgresl", new PortSpec(5432));
		COMMON_PORTS.put("echo-request", new PortSpec(8, -1));
		COMMON_PORTS.put("echo-reply", new PortSpec(0, -1));
	}

	public PortSpec(int port) {
		this(port, port);
	}

	public static PortSpec commonPort(String port) {
		return COMMON_PORTS.get(port);
	}

	public static void addAllNames(Set<String> names) {
		names.addAll(COMMON_PORTS.keySet());
	}
}
