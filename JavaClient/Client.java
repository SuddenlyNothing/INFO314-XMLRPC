import java.io.*;
import java.net.*;
import java.net.http.*;
import java.rmi.ServerException;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
	public static String server;
	public static String port;

	public static void main(String... args) throws Exception {
		if (args.length < 2) {
			System.out.println("Must pass server and port arguments respectively");
			return;
		}
		server = args[0];
		try {
			Integer.valueOf(args[1]);
			port = args[1];
		} catch (NumberFormatException e) {
			System.out.println("Port must be an integer");
			return;
		}

		System.out.println(add() == 0);
		System.out.println(add(1, 2, 3, 4, 5) == 15);
		System.out.println(add(2, 4) == 6);
		System.out.println(subtract(12, 6) == 6);
		System.out.println(multiply(3, 4) == 12);
		System.out.println(multiply(1, 2, 3, 4, 5) == 120);
		System.out.println(divide(10, 5) == 2);
		System.out.println(modulo(10, 5) == 0);

		System.out.println(add(Integer.MAX_VALUE, Integer.MAX_VALUE) == -1);
		System.out.println(multiply(Integer.MAX_VALUE, Integer.MAX_VALUE) == -1);
		System.out.println(sendIncorrectStringRequest() == -1);
		System.out.println(divide(1, 0) == -1);
	}

	public static int add(Integer... params) throws Exception {
		return sendRequest("add", params);
	}

	public static int subtract(int lhs, int rhs) throws Exception {
		return sendRequest("subtract", lhs, rhs);
	}
	
	public static int multiply(Integer... params) throws Exception {
		return sendRequest("multiply", params);
	}

	public static int divide(int lhs, int rhs) throws Exception {
		return sendRequest("divide", lhs, rhs);

	}

	public static int modulo(int lhs, int rhs) throws Exception {
		return sendRequest("modulo", lhs, rhs);

	}

	public static int sendIncorrectStringRequest() {
		return sendRequest(true, "subtract", 0);
	}

	public static String generateBody(boolean incorrect, String methodName, Integer... args) {
		XMLNode root = new XMLNode("methodCall", "", true);
		root.addChild("methodName", methodName);
		String valueName = incorrect ? "string" : "i4";
		XMLNode paramsParent = root.addChild("params");
		for (int i : args) {
			paramsParent.addChild("param").addChild("value").addChild(valueName, Integer.toString(i));
		}
		return root.toString();
	}

	public static int sendRequest(String methodName, Integer... args) {
		return sendRequest(false, methodName, args);
	}

	public static int sendRequest(boolean incorrect, String methodName, Integer... args) {
		try {
			HttpClient httpClient = HttpClient.newBuilder().build();

			String requestBody = generateBody(incorrect, methodName, args);

			HttpRequest request = HttpRequest.newBuilder()
							.uri(new URI("http://" + server + ":" + port + "/RPC"))
							.header("Content-Type", "text/xml")
							.POST(HttpRequest.BodyPublishers.ofString(requestBody))
							.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			int statusCode = response.statusCode();
			// System.out.println("Response Code: " + statusCode);

			HttpHeaders headers = response.headers();
			// headers.map().forEach((key, value) -> System.out.println(key + ": " + value));

			String responseBody = response.body();
			// System.out.println("Response Body: " + responseBody);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document document = builder.parse(new ByteArrayInputStream(responseBody.getBytes()));
			
			Element root = document.getDocumentElement();

			NodeList faults = root.getElementsByTagName("fault");
			if (faults.getLength() > 0) {
				return -1;
			}

			NodeList i4 = root.getElementsByTagName("i4");
			if (i4.getLength() == 0) {
				return -1;
			}
			return Integer.valueOf(i4.item(0).getTextContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static class XMLNode {
		String elementName;
		String textContent;
		Boolean isRoot;
		List<XMLNode> children;
	
		public XMLNode() {
			this("", "", false);
		}
	
		public XMLNode(String elementName) {
			this(elementName, "", false);
		}
	
		public XMLNode(String elementName, String textContent) {
			this(elementName, textContent, false);
		}
	
		public XMLNode(String elementName, String textContent, Boolean isRoot) {
			this.elementName = elementName;
			this.textContent = textContent;
			this.isRoot = isRoot;
			children = new ArrayList<>();
		}
	
		public XMLNode addChild(XMLNode node) {
			children.add(node);
			return node;
		}
	
		public XMLNode addChild(String elementName) {
			return addChild(elementName, "");
		}
	
		public XMLNode addChild(String elementName, String textContent) {
			XMLNode newNode = new XMLNode(elementName, textContent);
			children.add(newNode);
			return newNode;
		}
	
		public String toString() {
			StringBuilder childrenContent = new StringBuilder();
			String header = "";
			if (isRoot) {
				header = "<?xml version=\"1.0\"?>";
			}
			for (XMLNode child : children) {
				childrenContent.append(child.toString());
			}
			return header + "<" + elementName + ">" + textContent + childrenContent + "</" + elementName + ">";
		}
	}
}
