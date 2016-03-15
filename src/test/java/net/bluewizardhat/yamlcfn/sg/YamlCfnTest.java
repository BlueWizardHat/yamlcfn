package net.bluewizardhat.yamlcfn.sg;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class YamlCfnTest {
	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testSimple() throws Exception {
		performTest("simple");
	}

	@Test
	public void testSamples() throws Exception {
		performTest("samples");
	}

	@Test
	public void testAdvanced() throws Exception {
		performTest("advanced");
	}

	private void performTest(String fileName) throws Exception {
		// First check that no exceptions are thrown during yaml reading, persing, transforming and
		// json writing.
		try (FileInputStream in = new FileInputStream("samples/" + fileName +".yml");
				FileOutputStream out = new FileOutputStream("build/" + fileName +".json")) {
			YamlCfn.convert(in, out);
		}

		// Read it back to ensure the generated file is valid JSON
		mapper.readValue(new FileInputStream("build/" + fileName +".json"), Map.class);
	}

}
