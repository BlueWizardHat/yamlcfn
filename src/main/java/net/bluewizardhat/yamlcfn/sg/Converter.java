/*
 * Copyright (C) 2016 BlueWizardHat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
