package net.bluewizardhat.yamlcfn.sg.data.yaml;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Protocol {
	TCP("Tcp"),
	UDP("Udp"),
	ICMP("Icmp");

	@Getter
	private String name;

	public static Protocol from(String str) {
		return Protocol.valueOf(str.toUpperCase());
	}
}
