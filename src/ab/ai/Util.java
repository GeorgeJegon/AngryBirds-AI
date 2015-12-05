package ab.ai;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import org.apache.commons.codec.Charsets;

import ab.demo.other.Shot;
import ab.vision.ABType;

public class Util {
  
  public static <E> E getRandom(List<E> list){
    Random randomGenerator = new Random(System.currentTimeMillis());
    return list.get(randomGenerator.nextInt(list.size()));
  }
  
  public static Color getRandomColor() {
    int r, g, b;
    Random randomGenerator = new Random();
    
    r = randomGenerator.nextInt(255);
    g = randomGenerator.nextInt(255);
    b = randomGenerator.nextInt(255);
    
    return new Color(r,g,b);
  }
  
  public static void savePlainTextFile(String content, String fileNamePath) {
    File file = Util.createFile(fileNamePath);
    FileWriter writer;
    try {
      writer = new FileWriter(file);
      writer.write(content);
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static FastVector createShotHeaders() {
    FastVector attributes = new FastVector();
    FastVector labelValues = new FastVector();
    FastVector birdTypes = new FastVector();
    FastVector targetTypes = new FastVector();
    
    labelValues.addElement("WON");
    labelValues.addElement("LOST");
    
    birdTypes.addElement(ABType.RedBird.toString());
    birdTypes.addElement(ABType.BlackBird.toString());
    birdTypes.addElement(ABType.BlueBird.toString());
    birdTypes.addElement(ABType.YellowBird.toString());
    birdTypes.addElement(ABType.WhiteBird.toString());
    
    targetTypes.addElement(ABType.Pig.toString());
    targetTypes.addElement(ABType.Stone.toString());
    targetTypes.addElement(ABType.Ice.toString());
    targetTypes.addElement(ABType.Wood.toString());

    attributes.addElement(new Attribute("angle"));
    attributes.addElement(new Attribute("velocity"));
    attributes.addElement(new Attribute("level"));
    attributes.addElement(new Attribute("score"));
    attributes.addElement(new Attribute("heuristic", (FastVector) null));
    attributes.addElement(new Attribute("birdOnSling", birdTypes));
    attributes.addElement(new Attribute("targetType", targetTypes));
    attributes.addElement(new Attribute("label", labelValues));

    return attributes;
  }

  public static Instances createShotDataSet() {
    FastVector attributes = Util.createShotHeaders();
    Instances dataSet = new Instances("Shots", attributes, 0);

    return dataSet;
  }

  public static Instances createMatchInstanceArff(Match match) {
    double[] values;
    Instances dataSet = Util.createShotDataSet();

    for (Shot shot : match.getShots()) {
      values = new double[dataSet.numAttributes()];
      values[0] = shot.getThetaDegrees();
      values[1] = shot.getVelocity();
      values[2] = match.getLevel();
      values[3] = shot.getScore();
      values[4] = dataSet.attribute("heuristic").addStringValue(match.getHeuristic());
      values[5] = dataSet.attribute("birdOnSling").indexOfValue(shot.getBirdOnSling());
      values[6] = dataSet.attribute("targetType").indexOfValue(shot.getTargetType());
      values[7] = dataSet.attribute("label").indexOfValue(match.getType());

      dataSet.add(new Instance(1.0, values));
    }
    return dataSet;
  }

  public static Instances mergeInstancesArff(Instances newDataSet,
      Instances oldDataSet) {
    Instances mergedDataset;

    if (oldDataSet.numInstances() > 0) {
      mergedDataset = new Instances(oldDataSet);
      for (int i = 0; i < newDataSet.numInstances(); i++) {
        Instance currentInstance = newDataSet.instance(i);
        mergedDataset.add(currentInstance);
      }
    } else {
      mergedDataset = newDataSet;
    }

    return mergedDataset;
  }

  public static Instances mergeInstancesArff(Instances newDataSet,
      String fileNamePath) {
    Instances oldDataSet = loadArff(fileNamePath);
    return mergeInstancesArff(newDataSet, oldDataSet);
  }

  public static Instances loadArff(String fileNamePath) {
    ArffLoader arffLoader = new ArffLoader();
    File file = Util.createFile(fileNamePath);
    Instances dataSet = null;

    try {
      if (file.length() > 0) {
        arffLoader.setFile(file);
        dataSet = arffLoader.getDataSet();
      } else {
        dataSet = Util.createShotDataSet();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return dataSet;
  }

  public static void saveArff(Instances dataSet, String fileNamePath) {
    Util.savePlainTextFile(dataSet.toString(), fileNamePath);
    // File file = createFile(fileNamePath);
    // ArffSaver saver = new ArffSaver();
    // saver.setInstances(dataSet);
    // try {
    // saver.setFile(file);
    // saver.writeBatch();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
  }

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
