package net.bluewizardhat.yamlcfn.sg;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.bluewizardhat.yamlcfn.sg.data.Transformer;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnFile;
import net.bluewizardhat.yamlcfn.sg.data.yaml.YamlFile;

public class Converter {
	public static void main(String[] args) {
		try {
			String inputFile = args[0];
			String outputFile = args[1];
			InputStream in = new FileInputStream(inputFile);
			OutputStream out = new FileOutputStream(outputFile);
			YamlFile yamlFile = new YamlParser().parse(in);
			CfnFile cfnFile = Transformer.transform(yamlFile);
			Writer writer = new OutputStreamWriter(out);
			CfnWriter.writeCfnFile(cfnFile, writer);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
