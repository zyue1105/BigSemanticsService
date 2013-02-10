package ecologylab.bigsemantics.service.analytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.generic.Debug;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

public class ServicePerfLogAnalyzer extends Debug
{
	Date												beginDate, endDate;

	ArrayList<File>							logFiles;

	ArrayList<ServiceLogRecord>	logRecords;

	public ServicePerfLogAnalyzer(ArrayList<File> logFiles, Date beginDate, Date endDate)
	{
		this.logFiles = logFiles;
		this.beginDate = beginDate;
		this.endDate = endDate;
	}

	private void readPerfLogs() throws IOException
	{
		SimplTypesScope tscope = SimplTypesScope.get("perf-log-analysis", ServiceLogRecord.class);
		if (logFiles != null)
		{
			for (File logFile : logFiles)
			{
				BufferedReader reader = null;
				try
				{
					reader = new BufferedReader(new FileReader(logFile));
					String rec;
					while ((rec = reader.readLine()) != null)
					{
						try
						{
							ServiceLogRecord logRecord = (ServiceLogRecord) tscope.deserialize(rec,
									StringFormat.JSON);
							if (logRecord != null) {
								if (logRecord.getBeginTime().compareTo(beginDate) < 0)
									continue;
								if (logRecord.getBeginTime().compareTo(endDate) > 0)
									continue;
							}
							logRecords.add(logRecord);
						}
						catch (SIMPLTranslationException e)
						{
							debug("Couldn't deserialize " + rec);
							e.printStackTrace();
						}
					}
				}
				catch (FileNotFoundException e)
				{
					debug("Should not have occurred. Already checked if exists().");
					e.printStackTrace();
				}
				finally
				{
					if (reader != null)
						reader.close();
				}
			}
		}
	}

	private void getPerformanceMetrics() throws IOException
	{
		readPerfLogs();
		if (logRecords != null)
		{
			for (ServiceLogRecord logRecord : logRecords)
			{
				
			}
		}
	}

	public static void main(String[] args) throws IOException
	{
		String usage = "Usage: java ServicePerfLogAnalyzer logFile [numFiles] [beginDate] [endDate]\n"
				+ "ex: java ServicePerfLogAnalyzer metadataPerf.log 3 \"Wed Feb 06 18:00:57 CST 2013\" \"Wed Feb 06 18:01:57 CST 2013\"\n\n"
				+ ">1 log files: other files should have same name but followed by period(.) and #(1,2,3...)";

		Date beginDate = null;
		Date endDate = null;
		ArrayList<File> logFiles = new ArrayList<File>();

		if (args.length == 0)
		{
			System.out.println(usage);
			return;
		}

		File logFile = new File(args[0]);
		if (!logFile.exists())
		{
			System.out.println("Invalid logFile param <" + args[0] + ">. File not found");
			return;
		}
		logFiles.add(logFile);

		if (args[1] != null)
		{
			int num = Integer.parseInt(args[1]);
			while (--num > 0)
			{
				logFile = new File(args[0] + num);
				if (!logFile.exists())
				{
					System.out
							.println("Invalid numFile param. <" + args[0] + "." + num + "> file not found>");
					return;
				}
				logFiles.add(logFile);
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		if (args[2] != null)
		{
			try
			{
				beginDate = sdf.parse(args[2]);
			}
			catch (ParseException e)
			{
				System.out.println("Invalid beginDate param. Use \"EEE MMM dd HH:mm:ss z yyyy\" format.");
				e.printStackTrace();
			}
		}

		if (args[3] != null)
		{
			try
			{
				beginDate = sdf.parse(args[3]);
			}
			catch (ParseException e)
			{
				System.out.println("Invalid endDate param. Use \"EEE MMM dd HH:mm:ss z yyyy\" format.");
				e.printStackTrace();
			}
		}

		ServicePerfLogAnalyzer s1 = new ServicePerfLogAnalyzer(logFiles, beginDate, endDate);
		s1.getPerformanceMetrics();
	}
}
