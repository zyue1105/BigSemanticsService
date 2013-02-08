package ecologylab.bigsemantics.service.analytics;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.serialization.SimplTypesScope;

public class ServicePerfLogAnalyzer
{
	Date beginDate, endDate;
	ArrayList<File> logFiles;
	ArrayList<ServiceLogRecord> logRecords;
	
	public ServicePerfLogAnalyzer(ArrayList<File> logFiles, Date beginDate, Date endDate)
	{
		this.logFiles = logFiles;
		this.beginDate = beginDate;
		this.endDate = endDate;
	}
	
	private void readPerformanceMetrics()
	{
		SimplTypesScope	tscope	= SimplTypesScope.get("perf-log-analysis", ServiceLogRecord.class);
		if (logFiles != null)
		{
			for (File logFile : logFiles)
			{
				
			}
		}
	}
	
	private void getPerformanceMetrics()
	{
		readPerformanceMetrics();
		if (logRecords != null)
		{
			
		}
	}
	
	public static void main(String[] args)
	{
		String usage = "Usage: java ServicePerfLogAnalyzer logFile [numFiles] [beginDate] [endDate]\n"
				+ "ex: java ServicePerfLogAnalyzer metadataPerf.log 3 \"Wed Feb 06 18:00:57 CST 2013\" \"Wed Feb 06 18:01:57 CST 2013\"\n\n"
				+ ">1 log files: other files should be name followed by period(.) and # (1,2,3...)";

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
					System.out.println("Invalid numFile param. <" + args[0] + num + "> file not found>");
					return;
				}
				logFiles.add(logFile);
			}
		}
		
		if (args[2] != null)
		{
			
		}
		
		if (args[3] != null)
		{
		
		}
		
		ServicePerfLogAnalyzer s1 = new ServicePerfLogAnalyzer(logFiles, beginDate, endDate);
		s1.getPerformanceMetrics();
	}
}
