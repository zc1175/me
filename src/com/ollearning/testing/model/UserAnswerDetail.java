package com.ollearning.testing.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.StringUtil;

public class UserAnswerDetail extends Model<UserAnswerDetail> implements
		Comparable<UserAnswerDetail> {

	private static final long serialVersionUID = 5435494869671349308L;

	public static UserAnswerDetail dao = new UserAnswerDetail();
	private static final String cache = "userAnswerDetail";
	private static final String cacheList = "useranswerdetail-list";

	public UserAnswerDetail() {
		super(cache);
	}

	public Map<String, Object> saveUserAnswer(Integer userAnswerId,
			Integer userId, Integer examId, Integer questionId, Integer subId,
			String answer, int num) {
		Map<String, Object> ret = new HashMap<String, Object>();
		// 判断是否存在答案
		UserAnswerDetail obj = findFirst(
				"select * from useranswerdetail where userAnswerId=? and userId=? and examId=? and questionId=? and questionSubId=?",
				userAnswerId, userId, examId, questionId, subId);
		if (null == obj) {
			obj = new UserAnswerDetail();
			obj.set("userAnswerId", userAnswerId)
					.set("userId", userId)
					.set("examId", examId)
					.set("questionId", questionId)
					.set("questionSubId", subId)
					.set("answer", answer)
					.set("questionTypeId",
							Question.dao.loadModel(questionId).getInt(
									"questionTypeId"));
			obj.save();

		} else {
			obj.set("answer", answer);
		}
		// 判题
		// TODO 保存答案时自动判题，更新正确题数和错题数，可写至trigger
		try {
			UserAnswer uax = UserAnswer.dao.findById(userAnswerId);
			// ua.set("lastAnswerTime", new Date()).set("lastAnswerQuestionNum",
			// num).update();
			QuestionSub sub = QuestionSub.dao.loadModel(subId);
			// 保存韪的答案和说明
			ret.put("answer", sub.getStr("answer"));
			ret.put("description", sub.getStr("description"));

			if (StringUtil.isEqualsIgnoreCase(sub.getStr("answer"), answer)) {
				obj.set("correct", true).update();
				ret.put("correct", 1);
			} else {
				obj.set("correct", false).update();
				ret.put("correct", 0);
			}
			// Db.update("update useranswer set wrongNum=(select count(id) from useranswerdetail where userAnswerId="+userAnswerId+" and correct=0),correctNum=(select count(id) from useranswerdetail where userAnswerId="+userAnswerId+" and correct=1) where id="+userAnswerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != obj.getInt("id"))
			obj.update();
		else
			obj.save();
		obj.updateCache();
		return ret;
	}

	public String loadAnswer(UserAnswer ua, Integer questionId, Integer subId) {
		UserAnswerDetail obj = findFirst(
				"select * from useranswerdetail where userAnswerId=? and questionId=? and questionSubId=?",
				ua.getInt("id"), questionId, subId);
		if (null != obj) {
			return obj.getStr("answer");
		}
		return "";
	}

	public List<UserAnswerDetail> find(UserAnswer ua) {
		return find(ua.getInt("id"));
	}

	public List<UserAnswerDetail> find(Integer userAnswerId) {
		String key = "useranswerdetail-" + userAnswerId;
		List<UserAnswerDetail> list = findByCache(cacheList, key,
				"select * from useranswerdetail where userAnswerId=?",
				userAnswerId);
		if (null == list || list.size() == 0) {
			list = find("select * from useranswerdetail where userAnswerId=?",
					userAnswerId);
			CacheKit.put(cacheList, key, list);
		}
		return list;
	}

	public Page<UserAnswerDetail> findPage(Integer pageNumber, Integer userId,
			Integer examId) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from useranswerdetail where userId=? and examId=?", userId,
				examId);
	}

	public List<UserAnswerDetail> findByQuestionTypeId(Integer userId,
			Integer examId, Integer questionTypeId) {
		List<UserAnswerDetail> list = find(
				"select * from useranswerdetail where userId=? and examId=? and questionTypeId=?",
				userId, examId, questionTypeId);
		Collections.sort(list);
		return list;
	}

	// 查询用户的错题
	public Page<UserAnswerDetail> findUserWrongPage(Integer pageNumber,
			Integer userId, Integer userAnswerId) {
		String sql = "from useranswerdetail where 1=1 ";
		sql += " and userId=" + userId;
		if (null != userAnswerId)
			sql += " and userAnswerId=" + userAnswerId;
		sql += "  and correct=0 group by userAnswerId,questionId";
		sql += " order by id desc";
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *", sql);
	}

	public List<UserAnswerDetail> findUserWrong(Integer userId,
			Integer userAnswerId) {
		String sql = "select * from useranswerdetail where 1=1 ";
		sql += " and userId=" + userId;
		if (null != userAnswerId)
			sql += " and userAnswerId=" + userAnswerId;
		sql += "  and correct=0 group by userAnswerId,questionId";
		return find(sql);
	}

	public UserAnswerDetail find(UserAnswer ua, int questionTypeId,
			Integer index) {
		try {
			String key = ua.getInt("id") + "-" + questionTypeId;
			List<UserAnswerDetail> list = findByCache(
					cacheList,
					key,
					"select * from useranswerdetail where userAnswerId=? and questionTypeId=?",
					ua.getInt("id"), questionTypeId);
			if (list.size() >= index) {
				return list.get(index - 1);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getCorrectedDes() {
		Boolean corrected = getBoolean("correct");
		return (null != corrected && corrected) ? "<font color=green>正确</font>"
				: "<font color=red>错误</font>";
	}

	public Question getQuestion() {
		return Question.dao.loadModel(getInt("questionId"));
	}

	public Exam getExam() {
		return Exam.dao.loadModel(getInt("examId"));
	}

	public QuestionType getQuestionType() {
		return QuestionType.dao.findById(getInt("questionTypeId"));
	}

	/**
	 * 更新缓存
	 */
	public void updateCache() {
		try {
			CacheKit.put(cache, getInt("id"), this);
			List<UserAnswerDetail> details = find(getInt("userAnswerId"));
			int length = details.size();
			for (int i = 0; i < length; i++) {
				UserAnswerDetail detail = details.get(i);
				if (null != detail
						&& detail.getInt("id").intValue() == getInt("id")
								.intValue()) {
					details.set(i, this);
					break;
				}
			}
			String key = getInt("userAnswerId") + "-"
					+ getInt("questionTypeId");
			List<UserAnswerDetail> list = CacheKit.get(cacheList, key);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).compareTo(this) == 0) {
					list.set(i, this);
					break;
				}
			}
		} catch (Exception ex) {
			;
		}
	}

	@Override
	public int compareTo(UserAnswerDetail o) {
		if (getInt("id") > o.getInt("id"))
			return 1;
		else if (getInt("id") < o.getInt("id"))
			return -1;
		return 0;
	}

}
