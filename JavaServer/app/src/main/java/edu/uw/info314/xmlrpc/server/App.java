package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

import static spark.Spark.*;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args) {
        port(8080);

        notFound((req, res) -> {
            res.status(404);
            return "Not Found";
        });

        before((request, response) -> {
            if (!request.requestMethod().equalsIgnoreCase("POST")) {
                halt(405, "Method Not Allowed");
            }
        });

        // This is the mapping for POST requests to "/RPC";
        // this is where you will want to handle incoming XML-RPC requests
        post("/RPC", (request, response) -> {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(new ByteArrayInputStream(request.body().getBytes()));
            
            Element root = document.getDocumentElement();
            
            boolean failed = false;
            String faultCode = "";
            String faultString = "";

            String result = "";

            NodeList methodNameList = root.getElementsByTagName("methodName");
            String methodName = "";
            if (methodNameList.getLength() > 0) {
                methodName = methodNameList.item(0).getTextContent();
            } else {
                response.status(405);
                return "Method Not Supported";
            }
            
            NodeList paramList = root.getElementsByTagName("i4");
            List<Integer> params = new ArrayList<>();
            if (paramList.getLength() > 0) {
                for (int i = 0; i < paramList.getLength(); i++) {
                    params.add(Integer.valueOf(paramList.item(i).getTextContent()));
                }
            }
            NodeList paramParentsList = root.getElementsByTagName("value");
            for (int i = 0; i < paramParentsList.getLength(); i++) {
                if (failed) {
                    break;
                }
                NodeList paramChildren = paramParentsList.item(i).getChildNodes();
                for (int j = 0; j < paramChildren.getLength(); j++) {
                    if (!paramChildren.item(j).getNodeName().equals("i4")) {
                        failed = true;
                        faultCode = "3";
                        faultString = "illegal argument type";
                        break;
                    }
                }
            }

            if (!failed) {
                try {
                    switch(methodName) {
                        case "add":
                            if (hasAdditionOverflow(params.toArray(new Integer[0]))) {
                                failed = true;
                                faultCode = "1";
                                faultString = "integer overflow";
                                break;
                            }
                            result = Integer.toString(Calc.add(params.toArray(new Integer[0])));
                            break;
                        case "subtract":
                            if (hasSubtractionOverflow(params.get(0), params.get(1))) {
                                failed = true;
                                faultCode = "1";
                                faultString = "integer overflow";
                                break;
                            }
                            result = Integer.toString(Calc.subtract(params.get(0), params.get(1)));
                            break;
                        case "multiply":
                            if (hasMultiplyOverflow(params.toArray(new Integer[0]))) {
                                failed = true;
                                faultCode = "1";
                                faultString = "integer overflow";
                                break;
                            }
                            result = Integer.toString(Calc.multiply(params.toArray(new Integer[0])));
                            break;
                        case "divide":
                            result = Integer.toString(Calc.divide(params.get(0), params.get(1)));
                            break;
                        case "modulo":
                            result = Integer.toString(Calc.modulo(params.get(0), params.get(1)));
                            break;
                        default:
                            response.status(405);
                            return "Method Not Supported";
                    }
                } catch (ArithmeticException e) {
                    failed = true;
                    faultString = "divide by zero";
                    faultCode = "1";
                }
            }
            
            response.status(200);
            XMLNode responseRoot = new XMLNode("methodResponse", "", true);
            if (failed) {
                XMLNode memberParent = responseRoot.addChild("fault").addChild("value").addChild("struct");
                XMLNode faultCodeNode = memberParent.addChild("member");
                faultCodeNode.addChild("name", "faultCode");
                faultCodeNode.addChild("value").addChild("int", faultCode);

                XMLNode faultStringNode = memberParent.addChild("member");
                faultStringNode.addChild("name", "faultString");
                faultStringNode.addChild("value").addChild("string", faultString);
            } else {
                responseRoot.addChild("params").addChild("param").addChild("value").addChild("i4", result);
            }
            response.type("text/xml");
            String host = request.host();
            response.header("Host", host);
            return responseRoot;
        });
    }

    public static boolean hasMultiplyOverflow(Integer... args) {
        long product = 1L;

        for (int num : args) {
            product *= num;
            if (product > Integer.MAX_VALUE || product < Integer.MIN_VALUE) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasAdditionOverflow(Integer... args) {
        long sum = 1L;

        for (int num : args) {
            sum += num;
            if (sum > Integer.MAX_VALUE || sum < Integer.MIN_VALUE) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSubtractionOverflow(int a, int b) {
        long difference = a;

        difference -= b;
        if (difference > Integer.MAX_VALUE || difference < Integer.MIN_VALUE) {
            return true;
        }
        return false;
    }
}
