package app_test_files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Server {

	private List<String> servers;
	Properties props, settings;

	public Server() {
		super();
		servers = new ArrayList<>();
		props = new Properties();
		// Load the servers from the Server config File.
		try {
			if (!new File("config/app_settings.xml").exists()) {
				JOptionPane
						.showMessageDialog(null,
								"The Settings File is missing. please add it to the config folder to proceed.");
				System.exit(0);
			}
			props.loadFromXML(new FileInputStream("config/app_settings.xml"));
			for (String key : props.stringPropertyNames()) {
				if (key.equals("BackendUsername"))
					continue;
				else if (key.equals("BackendPassword"))
					continue;
				else
					servers.add(key + ";" + props.getProperty(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getServers() {
		return servers;
	}

	private final String FILE_NAME = "app_settings.xml";
	private final String APP_DIR = "config";
	File settings_file;

	public void setup_server() {
		// TODO Auto-generated method stub
		if (!new File(APP_DIR).exists()) {
			new File(APP_DIR).mkdir();
			settings_file = new File(FILE_NAME.concat("/").concat(FILE_NAME));
		}
	}
}

