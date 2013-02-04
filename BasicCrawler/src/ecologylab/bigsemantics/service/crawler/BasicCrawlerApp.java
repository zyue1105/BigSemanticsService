package ecologylab.bigsemantics.service.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.net.ParsedURL;

/**
 * 
 * @author quyin
 *
 */
public class BasicCrawlerApp
{

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    if (args.length != 4)
    {
      System.err.println("args: <url-list-file> <span> <max-count> <output-file>");
      System.exit(-1);
    }

    String urlListFileName = args[0];
    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new FileReader(urlListFileName));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      System.err.println("cannot read file " + urlListFileName);
      System.exit(-2);
    }
    int span = Integer.parseInt(args[1]);
    int maxCount = Integer.parseInt(args[2]);
    String outputFileName = args[3];
    BufferedWriter bw = null;
    try
    {
      bw = new BufferedWriter(new FileWriter(outputFileName));
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.err.println("cannot write file " + outputFileName);
      System.exit(-3);
    }

    ParsedURL serviceBaseUrl = ParsedURL.getAbsolute("http://ecoarray0.cs.tamu.edu:2080/BigSemanticsService/metadata.xml");
    BasicCrawler crawler = new BasicCrawler(serviceBaseUrl);

    System.out.println("seeding ...");
    while (true)
    {
      String line = null;
      try
      {
        line = br.readLine();
      }
      catch (IOException e)
      {
        e.printStackTrace();
        System.err.println("error occurs while reading from " + urlListFileName);
      }
      if (line == null)
        break;
      line = line.trim();
      if (line.length() > 0)
      {
        crawler.queueDoc(ParsedURL.getAbsolute(line), true, span);
      }
    }

    for (int i = 0; i < maxCount && crawler.hasNextDoc(); ++i)
    {
      if (crawler.hasNextDoc())
      {
        Document doc = crawler.nextDoc();
        if (doc != null && doc.getLocation() != null)
        {
          String locString = doc.getLocation().toString();
          System.out.format("%d: [%s]\n", i + 1, locString);
          try
          {
            bw.write(locString);
            bw.newLine();
            bw.flush();
          }
          catch (IOException e)
          {
            e.printStackTrace();
            System.err.println("error occurs while writing to" + outputFileName);
          }
        }
      }
    }

    System.out.println("done.");
  }

}
