package fi.markoa.proto.jruby.liquid;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jruby.CompatVersion;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class LiquidRenderer {

	public static void main(String ... cmdArgs) throws IOException {
		// setup Ruby runtime
		String jrubyHome = System.getProperty("jruby.home");
		System.out.println(jrubyHome);
		if(jrubyHome==null || jrubyHome.length()==0)
			throw new IllegalArgumentException("please set jruby.home system property");
		ScriptingContainer ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
		ruby.setLoadPaths(Arrays.asList( new String[]{
				jrubyHome+"/lib/ruby/gems/shared/gems/liquid-2.4.1/lib",
				jrubyHome+"/lib/ruby/gems/shared/gems/json-1.7.5-java/lib"
		}));
		System.out.println("load paths: "+ruby.getLoadPaths());
		ruby.setCompatVersion(CompatVersion.RUBY1_8);

		// parse input data
		String eventData = FileUtils.readFileToString(new File("sample1.json"));
		Object data = null;
		if("ruby_json".equals(cmdArgs[0])) {
			System.out.println("ruby_json");
			ruby.runScriptlet("require 'json'");
			Object jsonClass = ruby.runScriptlet("JSON");
			data = ruby.callMethod(jsonClass, "parse", new String(eventData), IRubyObject.class);
		} else if("jackson".equals(cmdArgs[0])) {
			System.out.println("jackson");
			ObjectMapper mapper = new ObjectMapper();
			data = mapper.readValue(eventData, Map.class);
		} else {
			System.out.println("gson");
			data = new Gson().fromJson(eventData, Map.class);
		}
		
		Object[] args = new Object[] { data };

		// make callout to Ruby code
		ruby.runScriptlet("require 'tpl_renderer.rb'");
		Object srv = ruby.runScriptlet("TemplateRenderer");
		IRubyObject service = ruby.callMethod(srv, "new", args, IRubyObject.class);
		System.out.println("srv: "+service);
		String content = (String)ruby.callMethod(service, "render_template", Object.class);
		System.out.println("--------------------------------");
		System.out.println("content: "+content);
		System.out.println("--------------------------------");
	}

}
