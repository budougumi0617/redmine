package jp.co.budougumi0617.forWeeklyStatusReport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;

public class updateRedmineLastWeekDone {
	private static String redmineHost = "http://localhost:3000/";
	private static String apiAccessKey = "PleaseInputRESTKey";
	private static String projectKey = "targetProjectName";
	private static Integer queryId = 55; // any

	public static void main(String[] args) {
		RedmineManager mgr = new RedmineManager(redmineHost, apiAccessKey);
		try {
			if (args[0].equals("LastWeekDoneRatio")) {
				tryCalculateLastWeekDoneRatio(mgr, args[1], args[2]);
			} else if (args[0].equals("Tardiness")) {
				tryCalculateTardiness(mgr, args[1], args[2]);
			} else {
				tryGetIssues(mgr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void tryGetIssues(RedmineManager mgr) throws Exception {
		List<Issue> issues = mgr.getIssues(projectKey, queryId);
		for (Issue issue : issues) {
			System.out.println(issue.toString());
		}
	}

	private static void tryCalculateLastWeekDoneRatio(RedmineManager mgr, String inputProjectKey, String inputQueryId)
			throws Exception {
		//http://www.redmine.org/projects/redmine/wiki/Rest_IssuesをみてKeyは見つけておく。
		Map<String, String> pParamaters = new HashMap<String, String>();

		pParamaters.put("project_id", inputProjectKey);
		pParamaters.put("query_id", inputQueryId);
		pParamaters.put("limit", "100");

		//List<Issue> issues = mgr.getIssues(projectKey, queryId, INCLUDE.changesets, INCLUDE.journals);
		List<Issue> issues = mgr.getIssues(pParamaters);
		for (Issue issue : issues) {
			System.out.println(issue.toString());
			String doneRatio = "0";
			System.out.println(issue.toString());
			doneRatio = issue.getDoneRatio().toString();
			System.out.println("doneRatio : " + doneRatio);
			System.out.println("before 先週までの進捗 : " + issue.getCustomField("先週までの進捗"));
			List<CustomField> customFields = new ArrayList<CustomField>();
			customFields.add(new CustomField(4, "開始時間", issue.getCustomField("開始時間")));
			customFields.add(new CustomField(5, "終了時間", issue.getCustomField("終了時間")));
			customFields.add(new CustomField(6, "今週計画した進捗", issue.getCustomField("今週計画した進捗")));
			customFields.add(new CustomField(8, "進捗遅れ(人日)", issue.getCustomField("進捗遅れ(人日)")));
			CustomField lastWeekDoneRatio = new CustomField(9, "先週までの進捗", doneRatio);
			customFields.add(lastWeekDoneRatio);
			issue.setCustomFields(customFields);
			System.out.println("after 先週までの進捗 : " + issue.getCustomField("先週までの進捗"));
			System.out.println("進捗遅れ(人日) : " + issue.getCustomField("進捗遅れ(人日)"));
			mgr.update(issue);
		}
		System.out.println("inputProjectKey : " + inputProjectKey);
	}

	private static void tryCalculateTardiness(RedmineManager mgr, String inputProjectKey, String inputQueryId)
			throws Exception {
		//http://www.redmine.org/projects/redmine/wiki/Rest_IssuesをみてKeyは見つけておく。
		Map<String, String> pParamaters = new HashMap<String, String>();
		Calendar todayDate = java.util.Calendar.getInstance();
		Calendar cal = java.util.Calendar.getInstance();

		pParamaters.put("project_id", inputProjectKey);
		pParamaters.put("query_id", inputQueryId);
		pParamaters.put("limit", "100");
		todayDate.set(Calendar.HOUR_OF_DAY, 0);
		todayDate.set(Calendar.MINUTE, 0);
		todayDate.set(Calendar.SECOND, 0);
		todayDate.set(Calendar.MILLISECOND, 0);
		List<Issue> issues = mgr.getIssues(pParamaters);
		for (Issue issue : issues) {
			Date dueDate = null;
			System.out.println(issue.toString());
			dueDate = issue.getDueDate();
			if (dueDate == null) {
				continue;
			}
			cal.setTime(dueDate);
			System.out.println("期日 : " + cal.getTime().toString());
			System.out.println("todayDate : " + todayDate.getTime().toString());
			int tardinessDate = 0;
			while (cal.before(todayDate)) {
				cal.add(Calendar.DAY_OF_MONTH, +1);
				if ((cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
						&& (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)) {
					System.out.println("曜日 : " + cal.get(Calendar.DAY_OF_WEEK));
					tardinessDate++;
				}
			}
			List<CustomField> customFields = new ArrayList<CustomField>();
			customFields.add(new CustomField(4, "開始時間", issue.getCustomField("開始時間")));
			customFields.add(new CustomField(5, "終了時間", issue.getCustomField("終了時間")));
			customFields.add(new CustomField(6, "今週計画した進捗", issue.getCustomField("今週計画した進捗")));
			customFields.add(new CustomField(9, "先週までの進捗", issue.getCustomField("先週までの進捗")));
			customFields.add(new CustomField(8, "進捗遅れ(人日)", Integer.toString(tardinessDate)));
			issue.setCustomFields(customFields);
			System.out.println("進捗遅れ(人日) : " + issue.getCustomField("進捗遅れ(人日)"));
			mgr.update(issue);
		}

	}
}
