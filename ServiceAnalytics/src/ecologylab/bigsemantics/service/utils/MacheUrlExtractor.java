/**
 *
 */
package ecologylab.bigsemantics.service.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

import ecologylab.bigsemantics.filestorage.SHA256FileNameGenerator;
import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * utility class to extract clipping source urls from the maches maches can be added one-by-one,
 * through a list, or a file.
 * 
 * @author ajit
 * 
 */

public class MacheUrlExtractor
{
	ArrayList<String>	macheUrls;

	ArrayList<String>	clippingSourceUrls;

	OutputStream			outputStream	= System.out;

	static String			userAgent			= "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.60 Safari/537.17";

	MacheUrlExtractor(String macheUrl, OutputStream outputStream)
	{
		this(macheUrl);
		this.outputStream = outputStream;
	}

	MacheUrlExtractor(List<String> macheUrls, OutputStream outputStream)
	{
		this(macheUrls);
		this.outputStream = outputStream;
	}

	MacheUrlExtractor(File macheUrlFile, OutputStream outputStream) throws IOException
	{
		this(macheUrlFile);
		this.outputStream = outputStream;
	}

	MacheUrlExtractor(String macheUrl) // ParsedUrl?
	{
		macheUrls.add(macheUrl);
	}

	MacheUrlExtractor(List<String> macheUrls)
	{
		for (String macheUrl : macheUrls)
			macheUrls.add(macheUrl);
	}

	MacheUrlExtractor(File macheUrlFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(macheUrlFile));
		String macheUrl;
		while ((macheUrl = reader.readLine()) != null)
		{
			macheUrls.add(macheUrl);
		}
		if (reader != null)
			reader.close();
	}

	List<String> getMacheSourceUrls()
	{
		if (macheUrls != null)
		{
			for (String macheUrl : macheUrls)
			{
				// get the mache metadata into a local file
				String fn	= "/tmp/cache/" + SHA256FileNameGenerator.getName(ParsedURL.getAbsolute(macheUrl)) + ".html";
				ProcessBuilder pb = new ProcessBuilder();
				pb.command("/usr/local/bin/wget", "-O", fn, "-U", userAgent, macheUrl);
				Process p = null;
				try
				{
					p = pb.start();
					p.waitFor();
					if (p.exitValue() == 0)
					{
						File f = new File(fn);
						if (f.exists() && f.length() > 0)
						{
							// map json response to get clipping source_doc
							ObjectMapper m = new ObjectMapper();
							JsonNode rootNode = m.readTree(f);
							
							f.delete();
						}
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	void outputMacheSourceUrls()
	{
		if (clippingSourceUrls != null)
		{
			PrintWriter pw = new PrintWriter(outputStream);
			for (String clippingSourceUrl : clippingSourceUrls)
				pw.println(clippingSourceUrl);
			pw.close();
		}
	}
}
