package net.bluewizardhat.yamlcfn.sg;

import org.junit.Test;

public class ConverterTest {

	@Test
	public void test() throws Exception {
		Converter.main(new String[]	{
					"src/test/resources/sg.yml",
					"build/sg.json"
			});
	}
}
