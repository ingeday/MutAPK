package uniandes.tsdl.mutapk.helper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Helper {

	public static Helper instance = null;
	public static String currDirectory = "";
	public static List<String> actNames = new ArrayList<String>();
	public static String mainActivity = "";
	public final static String MANIFEST = "AndroidManifest.xml";
	public final static String MAIN_ACTION = "android.intent.action.MAIN";

	public static Helper getInstance() {
		if (instance == null) {
			instance = new Helper();
		}
		return instance;
	}

	public String getCurrentDirectory() throws UnsupportedEncodingException {
		if (currDirectory.equals("")) {
			String currentDirectory = APKToolWrapper.class.getProtectionDomain().getCodeSource().getLocation()
					.getPath();
			String decodedPath = URLDecoder.decode(currentDirectory, "UTF-8");
			// decodedPath = "C:\\Users\\caev0\\Documents\\2018-10\\Tesis1\\MAPK\\";
			String os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("win") >= 0) {
				if (decodedPath.matches("/.:/.*")) {
					decodedPath = decodedPath.replaceAll("/", "\\\\").substring(1);
				}
				int pos = decodedPath.substring(1).indexOf("\\") + 1;
				decodedPath = decodedPath.replace(decodedPath.substring(0, pos),
						decodedPath.substring(0, pos - 1).toLowerCase());
			}
			if (decodedPath.endsWith(".jar")) {
				decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/") + 1);
			}
			currDirectory = decodedPath;
		}
		return currDirectory;
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("win") >= 0) {
			return true;
		}
		return false;
	}

	public List<String> getActivities() throws ParserConfigurationException, SAXException, IOException {
		if (actNames.isEmpty()) {
			List<String> activityNames = new ArrayList<String>();
			File fXmlFile = new File(getCurrentDirectory() + "temp" + File.separator + MANIFEST);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			// Get all activities
			NodeList nodeList = doc.getElementsByTagName("activity");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				Node nameAttr = node.getAttributes().getNamedItem("android:name");
				
				if (nameAttr != null) {
					String activityName = nameAttr.getNodeValue();
					activityNames.add(activityName);
					
					Element activityElement = (Element) node;
					NodeList actions = activityElement.getElementsByTagName("action");
					if (isMainActivity(actions)) {
						mainActivity = activityName;
					}
				}
			}
			actNames = activityNames;
		}
		return actNames;
	}
	
	public String getMainActivity() throws ParserConfigurationException, SAXException, IOException{
		if(mainActivity==""){
			getActivities();
		}
		return mainActivity;
	}

	private static boolean isMainActivity(NodeList actions){

		for (int i = 0; i < actions.getLength(); i++) {
			Node node = actions.item(i);
			Node nameAttr = node.getAttributes().getNamedItem("android:name");

			if(nameAttr != null){
				String actionValue = nameAttr.getNodeValue();
				if(actionValue.equals(MAIN_ACTION)){
					return true;
				}
			}
		}

		return false;
	}
}
