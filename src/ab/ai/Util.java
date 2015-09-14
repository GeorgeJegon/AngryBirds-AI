package ab.ai;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Util {
	// Save XML
	public static void saveXML(Object object, String fileNamePath, Boolean append) {
		Marshaller marshaller = createMarshaller(object);
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(fileNamePath, append);
			fileWriter.append(System.getProperty("line.separator"));
			marshaller.marshal(object, fileWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveXML(Object object, String fileNamePath) {
		saveXML(object, fileNamePath, false);
	}

	public static void saveXML(Object object) {
		Marshaller marshaller = createMarshaller(object);
		try {
			marshaller.marshal(object, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Object loadXML(Object object, String fileNamePath) {
		Unmarshaller unmarshaller = createUnmarshaller(object);
		Object loaded = null;
		try {
			loaded = unmarshaller.unmarshal(new FileReader(fileNamePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return loaded;
	}

	@SuppressWarnings("rawtypes")
	private static Unmarshaller createUnmarshaller(Object object) {
		Class objectClass = object.getClass();

		if (objectClass.getSimpleName().equals("Class")) {
			objectClass = (Class) object;
		}

		JAXBContext context;
		Unmarshaller unmarshaller = null;
		try {
			context = JAXBContext.newInstance(objectClass);
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return unmarshaller;
	}

	private static Marshaller createMarshaller(Object object) {
		JAXBContext context;
		Marshaller marshaller = null;
		try {
			context = JAXBContext.newInstance(object.getClass());
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return marshaller;
	}
}
