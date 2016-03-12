package net.bluewizardhat.yamlcfn.sg;

import java.io.FileInputStream;

import org.junit.Test;

public class ParserTest {

	@Test
	public void test() throws Exception {
		new YamlParser().parse(new FileInputStream("src/test/resources/sg.yml"));
	}
}
