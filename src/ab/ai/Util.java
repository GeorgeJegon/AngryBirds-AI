package ab.ai;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.Charsets;

public class Util {
  public static void saveImage(BufferedImage image, String fileNamePath) {
    try {
      ImageIO.write(image, "png", createFile(fileNamePath));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // Save XML
  public static void saveXML(Object object, String fileNamePath, Boolean append) {
    Marshaller marshaller = createMarshaller(object);
    File file = createFile(fileNamePath);
    FileWriter fileWriter;
    try {
      fileWriter = new FileWriter(file, append);
      if (append) {
        fileWriter.append(System.getProperty("line.separator"));
      }
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

  // Load XML
  public static List<?> loadXMLFileByLine(Object object, String fileNamePath) {
    List<Object> loaded = new ArrayList<Object>();
    Object loadedObject = null;
    try {
      List<String> lines = Files.readAllLines(Paths.get(fileNamePath),
          Charsets.UTF_8);

      for (String line : lines) {
        loadedObject = Util.loadXMLFromString(object, line);
        if (loadedObject != null) {
          loaded.add(loadedObject);
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return loaded;
  }

  public static Object loadXMLFromString(Object object, String line) {
    Unmarshaller unmarshaller = createUnmarshaller(object);
    Object loaded = null;
    StringReader stringReader = new StringReader(line);
    if (!line.isEmpty()) {
      try {
        loaded = unmarshaller.unmarshal(stringReader);
      } catch (JAXBException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return loaded;
  }

  public static Object loadXML(Object object, String fileNamePath) {
    Unmarshaller unmarshaller = createUnmarshaller(object);
    Object loaded = null;
    File fileXML = createFile(fileNamePath);

    if (fileXML.length() > 0) {
      try {
        loaded = unmarshaller.unmarshal(new FileReader(fileNamePath));
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (JAXBException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return loaded;
  }

  private static File createFile(String fileNamePath) {
    File file = new File(fileNamePath);

    if (!file.exists()) {
      file.getParentFile().mkdirs();
      try {
        file.createNewFile();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return file;
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
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return marshaller;
  }
}
