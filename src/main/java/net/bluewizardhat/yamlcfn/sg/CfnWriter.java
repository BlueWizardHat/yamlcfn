package net.bluewizardhat.yamlcfn.sg;

import java.io.IOException;
import java.io.Writer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import net.bluewizardhat.yamlcfn.sg.data.cfn.CfnFile;

public class CfnWriter {

	public static void writeCfnFile(CfnFile cfnFile, Writer writer) throws IOException, TemplateException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setTemplateLoader(new ClassTemplateLoader(CfnWriter.class, "/cfn-snippets"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		Template template = cfg.getTemplate("cfnfile.ftl");
		template.process(cfnFile, writer);
	}
}
